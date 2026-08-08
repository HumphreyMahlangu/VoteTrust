# VoteTrust Azure ACR Tasks Deployment

This deployment path uses Azure Container Registry Tasks to build the VoteTrust container and update Azure Container Apps. GitHub Actions remains the CI quality gate; protect `main` so only passing pull requests can trigger the ACR deployment task.

## Resources Created

The provisioning script creates a new production-style portfolio environment in `spaincentral`:

* Azure Resource Group
* Azure Container Registry with admin login disabled
* ACR Task with commit and base-image triggers
* Azure Container Apps environment on a dedicated VNet subnet
* Azure Container App with external HTTPS ingress on port `8080`
* Azure Database for PostgreSQL Flexible Server on a private subnet
* Private DNS zone for PostgreSQL
* Azure Key Vault for runtime secrets
* User-assigned managed identity for the app
* Log Analytics workspace

## Prerequisites

Install and sign in with Azure CLI:

```powershell
az login
az account set --subscription "<subscription-id-or-name>"
az extension add --name containerapp --upgrade
```

Create a GitHub personal access token for ACR source triggers. For this public repository, use the minimum practical scopes:

* `public_repo`
* `repo:status`

Set it only for the current shell session:

```powershell
$env:GITHUB_PAT = "<github-pat>"
```

Do not commit this token and rotate it if it is exposed.

## Provision And Deploy

Run from the repository root:

```powershell
pwsh .\infra\azure\provision-acr-tasks-deployment.ps1
```

The script performs a regional preflight check for `spaincentral`, creates the Azure resources, builds a bootstrap image in ACR, creates the Container App with Key Vault-backed secrets, creates the ACR Task, grants the task permission to update the Container App, and runs the task once.

The script prints the deployed base URL when it completes.

## Validate

```powershell
$baseUrl = "https://<container-app-fqdn>"
curl "$baseUrl/actuator/health/readiness"
curl "$baseUrl/actuator/health/liveness"
curl "$baseUrl/swagger-ui.html"
```

For Postman, update the verified environment `baseUrl` to the Container App URL.

## ACR Task Operations

Show recent task runs:

```powershell
az acr task list-runs --registry "<acr-name>" --name "votetrust-main-deploy" -o table
```

Show logs for the last run:

```powershell
az acr task logs --registry "<acr-name>" --name "votetrust-main-deploy"
```

Run the task manually:

```powershell
az acr task run --registry "<acr-name>" --name "votetrust-main-deploy"
```

## First Admin Bootstrap

The deployed app keeps admin bootstrap disabled by default. To create the first admin:

```powershell
$rg = "rg-votetrust-prod"
$app = "ca-votetrust-api"
$vault = "<key-vault-name>"
$token = az keyvault secret show --vault-name $vault --name votetrust-admin-bootstrap-token --query value -o tsv
$fqdn = az containerapp show --resource-group $rg --name $app --query properties.configuration.ingress.fqdn -o tsv

az containerapp update --resource-group $rg --name $app --set-env-vars VOTETRUST_ADMIN_BOOTSTRAP_ENABLED=true
```

Call `POST https://$fqdn/api/v1/admin/bootstrap` with header `X-VoteTrust-Admin-Bootstrap-Token: $token`, then immediately close the window:

```powershell
az containerapp update --resource-group $rg --name $app --set-env-vars VOTETRUST_ADMIN_BOOTSTRAP_ENABLED=false
az keyvault secret set --vault-name $vault --name votetrust-admin-bootstrap-token --value ([guid]::NewGuid().ToString("N"))
az containerapp revision restart --resource-group $rg --name $app --revision (az containerapp revision list --resource-group $rg --name $app --query "[?properties.active].name | [0]" -o tsv)
```

## Rollback

Each ACR task run pushes an immutable commit tag. Roll back by updating the Container App to a previous commit tag:

```powershell
az containerapp update `
  --resource-group "rg-votetrust-prod" `
  --name "ca-votetrust-api" `
  --image "<acr-name>.azurecr.io/votetrust-api:<previous-commit-sha>"
```

You can inspect active and inactive revisions:

```powershell
az containerapp revision list --resource-group "rg-votetrust-prod" --name "ca-votetrust-api" -o table
```
