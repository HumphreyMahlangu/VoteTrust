#requires -Version 7.2

[CmdletBinding()]
param(
    [string] $ResourceGroup = "rg-votetrust-prod",
    [string] $Location = "spaincentral",
    [string] $RepositoryUrl = "https://github.com/HumphreyMahlangu/VoteTrust.git",
    [string] $RepositoryBranch = "main",
    [string] $ImageRepository = "votetrust-api",
    [string] $AcrTaskName = "votetrust-main-deploy",
    [string] $ContainerAppName = "ca-votetrust-api",
    [string] $ContainerAppEnvironmentName = "cae-votetrust-prod",
    [string] $DatabaseName = "votetrust",
    [string] $PostgresAdminUser = "votetrustadmin",
    [string] $GitAccessTokenEnvironmentVariable = "GITHUB_PAT",
    [switch] $SkipInitialBuild,
    [switch] $SkipInitialTaskRun
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$AcrTaskFile = Join-Path $RepoRoot "infra\azure\acr-task.yaml"

function Assert-Command {
    param([Parameter(Mandatory = $true)][string] $Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Required command '$Name' was not found. Install it before running this script."
    }
}

function Invoke-Az {
    param([Parameter(Mandatory = $true)][string[]] $Arguments)

    & az @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Azure CLI command failed."
    }
}

function Invoke-AzTsv {
    param([Parameter(Mandatory = $true)][string[]] $Arguments)

    $output = & az @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Azure CLI command failed."
    }

    return (($output | Out-String).Trim())
}

function Try-Az {
    param([Parameter(Mandatory = $true)][string[]] $Arguments)

    & az @Arguments 1>$null 2>$null
    return $LASTEXITCODE -eq 0
}

function Try-AzTsv {
    param([Parameter(Mandatory = $true)][string[]] $Arguments)

    $output = & az @Arguments 2>$null
    if ($LASTEXITCODE -ne 0) {
        return $null
    }

    return (($output | Out-String).Trim())
}

function Get-NormalizedAzureLocation {
    param([Parameter(Mandatory = $true)][string] $Value)

    return ($Value.ToLowerInvariant() -replace "\s", "")
}

function Get-DeterministicSuffix {
    param([Parameter(Mandatory = $true)][string] $Seed)

    $bytes = [System.Text.Encoding]::UTF8.GetBytes($Seed)
    $hash = [System.Security.Cryptography.SHA256]::HashData($bytes)
    return ([Convert]::ToHexString($hash).Substring(0, 8).ToLowerInvariant())
}

function New-RandomSecret {
    param([int] $ByteCount = 48)

    $bytes = [byte[]]::new($ByteCount)
    [System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
    return [Convert]::ToBase64String($bytes)
}

function New-PostgresPassword {
    $raw = New-RandomSecret -ByteCount 36
    return (($raw -replace "[^a-zA-Z0-9]", "") + "Aa1!")
}

function Get-CurrentPrincipal {
    $userObjectId = Try-AzTsv -Arguments @("ad", "signed-in-user", "show", "--query", "id", "-o", "tsv")
    if (-not [string]::IsNullOrWhiteSpace($userObjectId)) {
        return @{
            ObjectId = $userObjectId
            Type = "User"
        }
    }

    $accountUser = Invoke-AzTsv -Arguments @("account", "show", "--query", "user.name", "-o", "tsv")
    $servicePrincipalObjectId = Try-AzTsv -Arguments @("ad", "sp", "show", "--id", $accountUser, "--query", "id", "-o", "tsv")
    if (-not [string]::IsNullOrWhiteSpace($servicePrincipalObjectId)) {
        return @{
            ObjectId = $servicePrincipalObjectId
            Type = "ServicePrincipal"
        }
    }

    throw "Could not resolve the current Azure principal object ID. Run 'az login' with a user or service principal that can manage this subscription."
}

function Assert-ProviderLocation {
    param(
        [Parameter(Mandatory = $true)][string] $Namespace,
        [Parameter(Mandatory = $true)][string] $ResourceType,
        [Parameter(Mandatory = $true)][string] $LocationName,
        [Parameter(Mandatory = $true)][string] $LocationDisplayName
    )

    $locations = & az provider show --namespace $Namespace --query "resourceTypes[?resourceType=='$ResourceType'].locations[]" -o tsv
    if ($LASTEXITCODE -ne 0) {
        throw "Could not inspect provider '$Namespace'."
    }

    $supportedLocations = @($locations | ForEach-Object { Get-NormalizedAzureLocation -Value $_ })
    $normalizedName = Get-NormalizedAzureLocation -Value $LocationName
    $normalizedDisplayName = Get-NormalizedAzureLocation -Value $LocationDisplayName

    if (($supportedLocations -notcontains $normalizedName) -and ($supportedLocations -notcontains $normalizedDisplayName)) {
        throw "$Namespace/$ResourceType is not available in '$LocationName' for this subscription. Choose another Azure region before provisioning."
    }
}

function Ensure-Provider {
    param([Parameter(Mandatory = $true)][string] $Namespace)

    Write-Host "Ensuring provider registration: $Namespace"
    Invoke-Az -Arguments @("provider", "register", "--namespace", $Namespace, "-o", "none")
}

function Ensure-RoleAssignment {
    param(
        [Parameter(Mandatory = $true)][string] $AssigneeObjectId,
        [Parameter(Mandatory = $true)][string] $PrincipalType,
        [Parameter(Mandatory = $true)][string] $Role,
        [Parameter(Mandatory = $true)][string] $Scope
    )

    $existing = Invoke-AzTsv -Arguments @(
        "role", "assignment", "list",
        "--assignee", $AssigneeObjectId,
        "--role", $Role,
        "--scope", $Scope,
        "--query", "[0].id",
        "-o", "tsv"
    )

    if ([string]::IsNullOrWhiteSpace($existing)) {
        Write-Host "Assigning role '$Role' on scope '$Scope'."
        Invoke-Az -Arguments @(
            "role", "assignment", "create",
            "--assignee-object-id", $AssigneeObjectId,
            "--assignee-principal-type", $PrincipalType,
            "--role", $Role,
            "--scope", $Scope,
            "-o", "none"
        )
    }
}

function Test-KeyVaultSecret {
    param(
        [Parameter(Mandatory = $true)][string] $VaultName,
        [Parameter(Mandatory = $true)][string] $SecretName
    )

    return Try-Az -Arguments @("keyvault", "secret", "show", "--vault-name", $VaultName, "--name", $SecretName)
}

function Set-KeyVaultSecret {
    param(
        [Parameter(Mandatory = $true)][string] $VaultName,
        [Parameter(Mandatory = $true)][string] $SecretName,
        [Parameter(Mandatory = $true)][string] $Value
    )

    Invoke-Az -Arguments @(
        "keyvault", "secret", "set",
        "--vault-name", $VaultName,
        "--name", $SecretName,
        "--value", $Value,
        "-o", "none"
    )
}

function Ensure-KeyVaultSecret {
    param(
        [Parameter(Mandatory = $true)][string] $VaultName,
        [Parameter(Mandatory = $true)][string] $SecretName,
        [Parameter(Mandatory = $true)][scriptblock] $ValueFactory
    )

    if (-not (Test-KeyVaultSecret -VaultName $VaultName -SecretName $SecretName)) {
        Write-Host "Creating Key Vault secret '$SecretName'."
        Set-KeyVaultSecret -VaultName $VaultName -SecretName $SecretName -Value (& $ValueFactory)
    }
}

function Get-KeyVaultSecretUri {
    param(
        [Parameter(Mandatory = $true)][string] $VaultName,
        [Parameter(Mandatory = $true)][string] $SecretName
    )

    return "https://$VaultName.vault.azure.net/secrets/$SecretName"
}

function New-ContainerAppYaml {
    param(
        [Parameter(Mandatory = $true)][string] $Location,
        [Parameter(Mandatory = $true)][string] $ResourceGroup,
        [Parameter(Mandatory = $true)][string] $ContainerAppName,
        [Parameter(Mandatory = $true)][string] $EnvironmentId,
        [Parameter(Mandatory = $true)][string] $IdentityId,
        [Parameter(Mandatory = $true)][string] $AcrLoginServer,
        [Parameter(Mandatory = $true)][string] $Image,
        [Parameter(Mandatory = $true)][string] $KeyVaultName
    )

    $dbUrlSecretUri = Get-KeyVaultSecretUri -VaultName $KeyVaultName -SecretName "spring-datasource-url"
    $dbUserSecretUri = Get-KeyVaultSecretUri -VaultName $KeyVaultName -SecretName "spring-datasource-username"
    $dbPasswordSecretUri = Get-KeyVaultSecretUri -VaultName $KeyVaultName -SecretName "spring-datasource-password"
    $jwtSecretUri = Get-KeyVaultSecretUri -VaultName $KeyVaultName -SecretName "votetrust-jwt-secret"
    $identityPepperSecretUri = Get-KeyVaultSecretUri -VaultName $KeyVaultName -SecretName "votetrust-id-hash-pepper"
    $votePepperSecretUri = Get-KeyVaultSecretUri -VaultName $KeyVaultName -SecretName "votetrust-vote-credential-pepper"
    $adminBootstrapTokenSecretUri = Get-KeyVaultSecretUri -VaultName $KeyVaultName -SecretName "votetrust-admin-bootstrap-token"

    return @"
location: "$Location"
name: "$ContainerAppName"
resourceGroup: "$ResourceGroup"
type: Microsoft.App/containerApps
identity:
  type: UserAssigned
  userAssignedIdentities:
    "$IdentityId": {}
properties:
  environmentId: "$EnvironmentId"
  workloadProfileName: Consumption
  configuration:
    activeRevisionsMode: Single
    ingress:
      external: true
      targetPort: 8080
      transport: http
      allowInsecure: false
      traffic:
        - latestRevision: true
          weight: 100
    registries:
      - server: "$AcrLoginServer"
        identity: "$IdentityId"
    secrets:
      - name: db-url
        keyVaultUrl: "$dbUrlSecretUri"
        identity: "$IdentityId"
      - name: db-user
        keyVaultUrl: "$dbUserSecretUri"
        identity: "$IdentityId"
      - name: db-pass
        keyVaultUrl: "$dbPasswordSecretUri"
        identity: "$IdentityId"
      - name: jwt-secret
        keyVaultUrl: "$jwtSecretUri"
        identity: "$IdentityId"
      - name: id-pepper
        keyVaultUrl: "$identityPepperSecretUri"
        identity: "$IdentityId"
      - name: vote-pepper
        keyVaultUrl: "$votePepperSecretUri"
        identity: "$IdentityId"
      - name: admin-token
        keyVaultUrl: "$adminBootstrapTokenSecretUri"
        identity: "$IdentityId"
  template:
    containers:
      - name: votetrust-api
        image: "$Image"
        env:
          - name: SPRING_DATASOURCE_URL
            secretRef: db-url
          - name: SPRING_DATASOURCE_USERNAME
            secretRef: db-user
          - name: SPRING_DATASOURCE_PASSWORD
            secretRef: db-pass
          - name: VOTETRUST_JWT_SECRET
            secretRef: jwt-secret
          - name: VOTETRUST_ID_HASH_PEPPER
            secretRef: id-pepper
          - name: VOTETRUST_VOTE_CREDENTIAL_PEPPER
            secretRef: vote-pepper
          - name: VOTETRUST_ADMIN_BOOTSTRAP_ENABLED
            value: "false"
          - name: VOTETRUST_ADMIN_BOOTSTRAP_TOKEN
            secretRef: admin-token
        probes:
          - type: Startup
            httpGet:
              path: /actuator/health/liveness
              port: 8080
              scheme: HTTP
            initialDelaySeconds: 15
            periodSeconds: 10
            timeoutSeconds: 5
            failureThreshold: 30
          - type: Liveness
            httpGet:
              path: /actuator/health/liveness
              port: 8080
              scheme: HTTP
            initialDelaySeconds: 30
            periodSeconds: 30
            timeoutSeconds: 5
            failureThreshold: 3
          - type: Readiness
            httpGet:
              path: /actuator/health/readiness
              port: 8080
              scheme: HTTP
            initialDelaySeconds: 15
            periodSeconds: 10
            timeoutSeconds: 5
            failureThreshold: 6
        resources:
          cpu: 0.5
          memory: 1Gi
    scale:
      minReplicas: 1
      maxReplicas: 2
"@
}

Assert-Command -Name "az"

if (-not (Test-Path (Join-Path $RepoRoot "Dockerfile"))) {
    throw "Run this script from the VoteTrust repository. The Dockerfile was not found."
}

if (-not (Test-Path $AcrTaskFile)) {
    throw "ACR task file not found at '$AcrTaskFile'."
}

Invoke-Az -Arguments @("account", "show", "-o", "none")
$subscriptionId = Invoke-AzTsv -Arguments @("account", "show", "--query", "id", "-o", "tsv")
$currentPrincipal = Get-CurrentPrincipal
$suffix = Get-DeterministicSuffix -Seed $subscriptionId

$AcrName = "votetrust$suffix"
$KeyVaultName = "kv-votetrust-$suffix"
$PostgresServerName = "psql-votetrust-$suffix"
$VnetName = "vnet-votetrust-prod"
$AcaSubnetName = "snet-containerapps"
$PostgresSubnetName = "snet-postgres"
$PrivateDnsZoneName = "votetrust.private.postgres.database.azure.com"
$LogAnalyticsName = "log-votetrust-prod"
$AppIdentityName = "id-votetrust-aca"
$BootstrapImage = "$ImageRepository`:bootstrap"
$GitToken = [Environment]::GetEnvironmentVariable($GitAccessTokenEnvironmentVariable)

Write-Host "Preflight: validating Azure region '$Location'."
$locationDisplayName = Invoke-AzTsv -Arguments @("account", "list-locations", "--query", "[?name=='$Location'].displayName | [0]", "-o", "tsv")
if ([string]::IsNullOrWhiteSpace($locationDisplayName)) {
    throw "Azure location '$Location' was not found by the current Azure CLI account."
}

Ensure-Provider -Namespace "Microsoft.App"
Ensure-Provider -Namespace "Microsoft.ContainerRegistry"
Ensure-Provider -Namespace "Microsoft.DBforPostgreSQL"
Ensure-Provider -Namespace "Microsoft.KeyVault"
Ensure-Provider -Namespace "Microsoft.ManagedIdentity"
Ensure-Provider -Namespace "Microsoft.Network"
Ensure-Provider -Namespace "Microsoft.OperationalInsights"

Assert-ProviderLocation -Namespace "Microsoft.App" -ResourceType "containerApps" -LocationName $Location -LocationDisplayName $locationDisplayName
Assert-ProviderLocation -Namespace "Microsoft.App" -ResourceType "managedEnvironments" -LocationName $Location -LocationDisplayName $locationDisplayName
Assert-ProviderLocation -Namespace "Microsoft.ContainerRegistry" -ResourceType "registries" -LocationName $Location -LocationDisplayName $locationDisplayName
Assert-ProviderLocation -Namespace "Microsoft.DBforPostgreSQL" -ResourceType "flexibleServers" -LocationName $Location -LocationDisplayName $locationDisplayName
Assert-ProviderLocation -Namespace "Microsoft.KeyVault" -ResourceType "vaults" -LocationName $Location -LocationDisplayName $locationDisplayName

$commonTags = @("app=votetrust", "environment=prod", "managed-by=acr-tasks", "project=portfolio")

Write-Host "Creating or updating resource group '$ResourceGroup'."
Invoke-Az -Arguments (@("group", "create", "--name", $ResourceGroup, "--location", $Location, "--tags") + $commonTags + @("-o", "none"))

Write-Host "Creating or reusing Azure Container Registry '$AcrName'."
if (-not (Try-Az -Arguments @("acr", "show", "--name", $AcrName, "--resource-group", $ResourceGroup))) {
    Invoke-Az -Arguments (@(
        "acr", "create",
        "--name", $AcrName,
        "--resource-group", $ResourceGroup,
        "--location", $Location,
        "--sku", "Basic",
        "--admin-enabled", "false",
        "--public-network-enabled", "true",
        "--tags"
    ) + $commonTags + @("-o", "none"))
}
$acrId = Invoke-AzTsv -Arguments @("acr", "show", "--name", $AcrName, "--resource-group", $ResourceGroup, "--query", "id", "-o", "tsv")
$acrLoginServer = Invoke-AzTsv -Arguments @("acr", "show", "--name", $AcrName, "--resource-group", $ResourceGroup, "--query", "loginServer", "-o", "tsv")

Write-Host "Creating or updating virtual network '$VnetName'."
Invoke-Az -Arguments (@(
    "network", "vnet", "create",
    "--name", $VnetName,
    "--resource-group", $ResourceGroup,
    "--location", $Location,
    "--address-prefixes", "10.42.0.0/16",
    "--tags"
) + $commonTags + @("-o", "none"))

if (-not (Try-Az -Arguments @("network", "vnet", "subnet", "show", "--resource-group", $ResourceGroup, "--vnet-name", $VnetName, "--name", $AcaSubnetName))) {
    Invoke-Az -Arguments @(
        "network", "vnet", "subnet", "create",
        "--resource-group", $ResourceGroup,
        "--vnet-name", $VnetName,
        "--name", $AcaSubnetName,
        "--address-prefixes", "10.42.0.0/23",
        "--delegations", "Microsoft.App/environments",
        "-o", "none"
    )
}

if (-not (Try-Az -Arguments @("network", "vnet", "subnet", "show", "--resource-group", $ResourceGroup, "--vnet-name", $VnetName, "--name", $PostgresSubnetName))) {
    Invoke-Az -Arguments @(
        "network", "vnet", "subnet", "create",
        "--resource-group", $ResourceGroup,
        "--vnet-name", $VnetName,
        "--name", $PostgresSubnetName,
        "--address-prefixes", "10.42.2.0/24",
        "--delegations", "Microsoft.DBforPostgreSQL/flexibleServers",
        "-o", "none"
    )
}

$acaSubnetId = Invoke-AzTsv -Arguments @("network", "vnet", "subnet", "show", "--resource-group", $ResourceGroup, "--vnet-name", $VnetName, "--name", $AcaSubnetName, "--query", "id", "-o", "tsv")
$postgresSubnetId = Invoke-AzTsv -Arguments @("network", "vnet", "subnet", "show", "--resource-group", $ResourceGroup, "--vnet-name", $VnetName, "--name", $PostgresSubnetName, "--query", "id", "-o", "tsv")
$vnetId = Invoke-AzTsv -Arguments @("network", "vnet", "show", "--resource-group", $ResourceGroup, "--name", $VnetName, "--query", "id", "-o", "tsv")

Write-Host "Creating or reusing private DNS zone '$PrivateDnsZoneName'."
if (-not (Try-Az -Arguments @("network", "private-dns", "zone", "show", "--resource-group", $ResourceGroup, "--name", $PrivateDnsZoneName))) {
    Invoke-Az -Arguments @("network", "private-dns", "zone", "create", "--resource-group", $ResourceGroup, "--name", $PrivateDnsZoneName, "-o", "none")
}
if (-not (Try-Az -Arguments @("network", "private-dns", "link", "vnet", "show", "--resource-group", $ResourceGroup, "--zone-name", $PrivateDnsZoneName, "--name", "$VnetName-postgres-link"))) {
    Invoke-Az -Arguments @(
        "network", "private-dns", "link", "vnet", "create",
        "--resource-group", $ResourceGroup,
        "--zone-name", $PrivateDnsZoneName,
        "--name", "$VnetName-postgres-link",
        "--virtual-network", $vnetId,
        "--registration-enabled", "false",
        "-o", "none"
    )
}
$privateDnsZoneId = Invoke-AzTsv -Arguments @("network", "private-dns", "zone", "show", "--resource-group", $ResourceGroup, "--name", $PrivateDnsZoneName, "--query", "id", "-o", "tsv")

Write-Host "Creating or reusing Key Vault '$KeyVaultName'."
if (-not (Try-Az -Arguments @("keyvault", "show", "--name", $KeyVaultName, "--resource-group", $ResourceGroup))) {
    Invoke-Az -Arguments (@(
        "keyvault", "create",
        "--name", $KeyVaultName,
        "--resource-group", $ResourceGroup,
        "--location", $Location,
        "--enable-rbac-authorization", "true",
        "--enable-purge-protection", "true",
        "--retention-days", "90",
        "--tags"
    ) + $commonTags + @("-o", "none"))
}
$keyVaultId = Invoke-AzTsv -Arguments @("keyvault", "show", "--name", $KeyVaultName, "--resource-group", $ResourceGroup, "--query", "id", "-o", "tsv")
Ensure-RoleAssignment -AssigneeObjectId $currentPrincipal.ObjectId -PrincipalType $currentPrincipal.Type -Role "Key Vault Secrets Officer" -Scope $keyVaultId

Write-Host "Waiting briefly for Key Vault RBAC propagation."
Start-Sleep -Seconds 30

Write-Host "Creating or reusing PostgreSQL Flexible Server '$PostgresServerName'."
$postgresExists = Try-Az -Arguments @("postgres", "flexible-server", "show", "--name", $PostgresServerName, "--resource-group", $ResourceGroup)
if (-not $postgresExists) {
    $postgresAdminPassword = New-PostgresPassword
    Set-KeyVaultSecret -VaultName $KeyVaultName -SecretName "spring-datasource-password" -Value $postgresAdminPassword

    Invoke-Az -Arguments (@(
        "postgres", "flexible-server", "create",
        "--name", $PostgresServerName,
        "--resource-group", $ResourceGroup,
        "--location", $Location,
        "--version", "16",
        "--admin-user", $PostgresAdminUser,
        "--admin-password", $postgresAdminPassword,
        "--tier", "Burstable",
        "--sku-name", "Standard_B1ms",
        "--storage-size", "32",
        "--storage-auto-grow", "Enabled",
        "--backup-retention", "7",
        "--public-access", "Disabled",
        "--subnet", $postgresSubnetId,
        "--private-dns-zone", $privateDnsZoneId,
        "--yes",
        "--tags"
    ) + $commonTags + @("-o", "none"))
} elseif (-not (Test-KeyVaultSecret -VaultName $KeyVaultName -SecretName "spring-datasource-password")) {
    throw "PostgreSQL server already exists, but Key Vault secret 'spring-datasource-password' is missing. Add the existing password to Key Vault before rerunning."
}

if (-not (Try-Az -Arguments @("postgres", "flexible-server", "db", "show", "--name", $DatabaseName, "--server-name", $PostgresServerName, "--resource-group", $ResourceGroup))) {
    Invoke-Az -Arguments @(
        "postgres", "flexible-server", "db", "create",
        "--name", $DatabaseName,
        "--server-name", $PostgresServerName,
        "--resource-group", $ResourceGroup,
        "-o", "none"
    )
}

$postgresFqdn = Invoke-AzTsv -Arguments @("postgres", "flexible-server", "show", "--name", $PostgresServerName, "--resource-group", $ResourceGroup, "--query", "fullyQualifiedDomainName", "-o", "tsv")
$jdbcUrl = "jdbc:postgresql://$postgresFqdn`:5432/$DatabaseName`?sslmode=require"
Set-KeyVaultSecret -VaultName $KeyVaultName -SecretName "spring-datasource-url" -Value $jdbcUrl
Ensure-KeyVaultSecret -VaultName $KeyVaultName -SecretName "spring-datasource-username" -ValueFactory { $PostgresAdminUser }
Ensure-KeyVaultSecret -VaultName $KeyVaultName -SecretName "votetrust-jwt-secret" -ValueFactory { New-RandomSecret -ByteCount 64 }
Ensure-KeyVaultSecret -VaultName $KeyVaultName -SecretName "votetrust-id-hash-pepper" -ValueFactory { New-RandomSecret -ByteCount 64 }
Ensure-KeyVaultSecret -VaultName $KeyVaultName -SecretName "votetrust-vote-credential-pepper" -ValueFactory { New-RandomSecret -ByteCount 64 }
Ensure-KeyVaultSecret -VaultName $KeyVaultName -SecretName "votetrust-admin-bootstrap-token" -ValueFactory { New-RandomSecret -ByteCount 48 }

Write-Host "Creating or reusing managed identity '$AppIdentityName'."
if (-not (Try-Az -Arguments @("identity", "show", "--name", $AppIdentityName, "--resource-group", $ResourceGroup))) {
    Invoke-Az -Arguments (@(
        "identity", "create",
        "--name", $AppIdentityName,
        "--resource-group", $ResourceGroup,
        "--location", $Location,
        "--tags"
    ) + $commonTags + @("-o", "none"))
}
$appIdentityId = Invoke-AzTsv -Arguments @("identity", "show", "--name", $AppIdentityName, "--resource-group", $ResourceGroup, "--query", "id", "-o", "tsv")
$appIdentityPrincipalId = Invoke-AzTsv -Arguments @("identity", "show", "--name", $AppIdentityName, "--resource-group", $ResourceGroup, "--query", "principalId", "-o", "tsv")
Ensure-RoleAssignment -AssigneeObjectId $appIdentityPrincipalId -PrincipalType "ServicePrincipal" -Role "AcrPull" -Scope $acrId
Ensure-RoleAssignment -AssigneeObjectId $appIdentityPrincipalId -PrincipalType "ServicePrincipal" -Role "Key Vault Secrets User" -Scope $keyVaultId

Write-Host "Creating or reusing Log Analytics workspace '$LogAnalyticsName'."
if (-not (Try-Az -Arguments @("monitor", "log-analytics", "workspace", "show", "--workspace-name", $LogAnalyticsName, "--resource-group", $ResourceGroup))) {
    Invoke-Az -Arguments (@(
        "monitor", "log-analytics", "workspace", "create",
        "--workspace-name", $LogAnalyticsName,
        "--resource-group", $ResourceGroup,
        "--location", $Location,
        "--retention-time", "30",
        "--tags"
    ) + $commonTags + @("-o", "none"))
}
$workspaceCustomerId = Invoke-AzTsv -Arguments @("monitor", "log-analytics", "workspace", "show", "--workspace-name", $LogAnalyticsName, "--resource-group", $ResourceGroup, "--query", "customerId", "-o", "tsv")
$workspaceSharedKey = Invoke-AzTsv -Arguments @("monitor", "log-analytics", "workspace", "get-shared-keys", "--workspace-name", $LogAnalyticsName, "--resource-group", $ResourceGroup, "--query", "primarySharedKey", "-o", "tsv")

Write-Host "Creating or reusing Container Apps environment '$ContainerAppEnvironmentName'."
if (-not (Try-Az -Arguments @("containerapp", "env", "show", "--name", $ContainerAppEnvironmentName, "--resource-group", $ResourceGroup))) {
    Invoke-Az -Arguments (@(
        "containerapp", "env", "create",
        "--name", $ContainerAppEnvironmentName,
        "--resource-group", $ResourceGroup,
        "--location", $Location,
        "--infrastructure-subnet-resource-id", $acaSubnetId,
        "--logs-workspace-id", $workspaceCustomerId,
        "--logs-workspace-key", $workspaceSharedKey,
        "--enable-workload-profiles", "true",
        "--tags"
    ) + $commonTags + @("-o", "none"))
}
$containerAppEnvironmentId = Invoke-AzTsv -Arguments @("containerapp", "env", "show", "--name", $ContainerAppEnvironmentName, "--resource-group", $ResourceGroup, "--query", "id", "-o", "tsv")

if (-not $SkipInitialBuild) {
    Write-Host "Building bootstrap image in ACR. This avoids storing registry credentials locally."
    Push-Location $RepoRoot
    try {
        Invoke-Az -Arguments @(
            "acr", "build",
            "--registry", $AcrName,
            "--image", $BootstrapImage,
            "--platform", "linux/amd64",
            "."
        )
    } finally {
        Pop-Location
    }
}

$containerImage = "$acrLoginServer/$BootstrapImage"
$containerAppYaml = New-ContainerAppYaml -Location $Location -ResourceGroup $ResourceGroup -ContainerAppName $ContainerAppName -EnvironmentId $containerAppEnvironmentId -IdentityId $appIdentityId -AcrLoginServer $acrLoginServer -Image $containerImage -KeyVaultName $KeyVaultName
$containerAppYamlPath = Join-Path ([System.IO.Path]::GetTempPath()) "votetrust-containerapp-$suffix.yaml"
Set-Content -LiteralPath $containerAppYamlPath -Value $containerAppYaml -Encoding utf8

try {
    Write-Host "Creating or updating Container App '$ContainerAppName'."
    if (Try-Az -Arguments @("containerapp", "show", "--name", $ContainerAppName, "--resource-group", $ResourceGroup)) {
        Invoke-Az -Arguments @("containerapp", "update", "--name", $ContainerAppName, "--resource-group", $ResourceGroup, "--yaml", $containerAppYamlPath, "-o", "none")
    } else {
        Invoke-Az -Arguments @("containerapp", "create", "--name", $ContainerAppName, "--resource-group", $ResourceGroup, "--environment", $ContainerAppEnvironmentName, "--yaml", $containerAppYamlPath, "-o", "none")
    }
} finally {
    Remove-Item -LiteralPath $containerAppYamlPath -Force -ErrorAction SilentlyContinue
}

$containerAppId = Invoke-AzTsv -Arguments @("containerapp", "show", "--name", $ContainerAppName, "--resource-group", $ResourceGroup, "--query", "id", "-o", "tsv")

if (-not (Try-Az -Arguments @("acr", "task", "show", "--name", $AcrTaskName, "--registry", $AcrName))) {
    if ([string]::IsNullOrWhiteSpace($GitToken)) {
        throw "Set environment variable '$GitAccessTokenEnvironmentVariable' to a GitHub PAT with public_repo and repo:status scopes before creating the ACR task."
    }

    Write-Host "Creating ACR Task '$AcrTaskName' with main-branch commit and base-image triggers."
    Invoke-Az -Arguments @(
        "acr", "task", "create",
        "--name", $AcrTaskName,
        "--registry", $AcrName,
        "--resource-group", $ResourceGroup,
        "--context", "$RepositoryUrl#$RepositoryBranch",
        "--file", "infra/azure/acr-task.yaml",
        "--platform", "linux/amd64",
        "--commit-trigger-enabled", "true",
        "--pull-request-trigger-enabled", "false",
        "--base-image-trigger-enabled", "true",
        "--assign-identity", "[system]",
        "--set", "resourceGroup=$ResourceGroup",
        "--set", "containerAppName=$ContainerAppName",
        "--set", "imageRepository=$ImageRepository",
        "--git-access-token", $GitToken,
        "-o", "none"
    )
} else {
    Write-Host "ACR Task '$AcrTaskName' already exists. Reusing it."
}

$taskPrincipalId = Invoke-AzTsv -Arguments @("acr", "task", "show", "--name", $AcrTaskName, "--registry", $AcrName, "--query", "identity.principalId", "-o", "tsv")
if ([string]::IsNullOrWhiteSpace($taskPrincipalId)) {
    Invoke-Az -Arguments @("acr", "task", "identity", "assign", "--name", $AcrTaskName, "--registry", $AcrName, "--identities", "[system]", "-o", "none")
    $taskPrincipalId = Invoke-AzTsv -Arguments @("acr", "task", "show", "--name", $AcrTaskName, "--registry", $AcrName, "--query", "identity.principalId", "-o", "tsv")
}
Ensure-RoleAssignment -AssigneeObjectId $taskPrincipalId -PrincipalType "ServicePrincipal" -Role "Contributor" -Scope $containerAppId

if (-not $SkipInitialTaskRun) {
    Write-Host "Waiting briefly for ACR Task identity RBAC propagation."
    Start-Sleep -Seconds 30
    Write-Host "Running ACR Task once to deploy the current main commit image."
    Invoke-Az -Arguments @("acr", "task", "run", "--name", $AcrTaskName, "--registry", $AcrName)
}

$fqdn = Invoke-AzTsv -Arguments @("containerapp", "show", "--name", $ContainerAppName, "--resource-group", $ResourceGroup, "--query", "properties.configuration.ingress.fqdn", "-o", "tsv")

Write-Host ""
Write-Host "VoteTrust Azure deployment is configured."
Write-Host "Resource group: $ResourceGroup"
Write-Host "ACR: $AcrName"
Write-Host "ACR task: $AcrTaskName"
Write-Host "Container App: $ContainerAppName"
Write-Host "Base URL: https://$fqdn"
Write-Host "Readiness: https://$fqdn/actuator/health/readiness"
Write-Host "Swagger UI: https://$fqdn/swagger-ui.html"
