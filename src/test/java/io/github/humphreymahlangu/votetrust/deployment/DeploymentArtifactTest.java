package io.github.humphreymahlangu.votetrust.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DeploymentArtifactTest {

    @Test
    void dockerfileBuildsRuntimeImageAsNonRootWithReadinessHealthcheck() throws IOException {
        String dockerfile = read("Dockerfile");

        assertThat(dockerfile).contains("FROM eclipse-temurin:21-jdk-jammy AS build");
        assertThat(dockerfile).contains("FROM eclipse-temurin:21-jre-jammy");
        assertThat(dockerfile).contains("USER votetrust");
        assertThat(dockerfile).contains("HEALTHCHECK");
        assertThat(dockerfile).contains("/actuator/health/readiness");
        assertThat(dockerfile).contains("org.opencontainers.image.title");
    }

    @Test
    void productionPackagingExcludesTestConfigurationAndRepositoryOnlyAssets() throws IOException {
        assertThat(Path.of("src/main/resources/application-test.properties")).doesNotExist();
        assertThat(Path.of("src/test/resources/application-test.properties")).exists();
        assertThat(Files.readAllLines(Path.of(".dockerignore"))).containsExactly(
                "**",
                "!Dockerfile",
                "!.mvn/",
                "!.mvn/**",
                "!mvnw",
                "!pom.xml",
                "!src/",
                "!src/main/",
                "!src/main/**");
    }

    @Test
    void composeRequiresSecretsAndRunsApiWithContainerHardening() throws IOException {
        String compose = read("compose.yaml");

        assertThat(compose).contains("POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:?POSTGRES_PASSWORD is required}");
        assertThat(compose).contains("VOTETRUST_JWT_SECRET: ${VOTETRUST_JWT_SECRET:?VOTETRUST_JWT_SECRET is required}");
        assertThat(compose).contains("VOTETRUST_ID_HASH_PEPPER: ${VOTETRUST_ID_HASH_PEPPER:?VOTETRUST_ID_HASH_PEPPER is required}");
        assertThat(compose).contains("VOTETRUST_VOTE_CREDENTIAL_PEPPER: ${VOTETRUST_VOTE_CREDENTIAL_PEPPER:?VOTETRUST_VOTE_CREDENTIAL_PEPPER is required}");
        assertThat(compose).contains("read_only: true");
        assertThat(compose).contains("no-new-privileges:true");
        assertThat(compose).contains("cap_drop:");
        assertThat(compose).contains("/actuator/health/readiness");
    }

    @Test
    void applicationConfigurationHasNoHardcodedDatasourceSecretsAndSupportsGracefulShutdown() throws IOException {
        String applicationProperties = read("src/main/resources/application.properties");

        assertThat(applicationProperties).contains("spring.datasource.url=${SPRING_DATASOURCE_URL}");
        assertThat(applicationProperties).contains("spring.datasource.username=${SPRING_DATASOURCE_USERNAME}");
        assertThat(applicationProperties).contains("spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}");
        assertThat(applicationProperties).doesNotContain("SPRING_DATASOURCE_PASSWORD:votetrust");
        assertThat(applicationProperties).contains("server.shutdown=graceful");
        assertThat(applicationProperties).contains("management.endpoint.health.group.readiness.include=readinessState,db");
    }

    @Test
    void ciVerifiesTestsPackagingComposeAndContainerBuild() throws IOException {
        String ci = read(".github/workflows/ci.yml");

        assertThat(ci).contains("uses: actions/checkout@v7");
        assertThat(ci).contains("uses: actions/setup-java@v5");
        assertThat(ci).contains("postgres:16-alpine");
        assertThat(ci).contains("VOTETRUST_TEST_DATASOURCE_URL: jdbc:postgresql://localhost:5432/votetrust_test");
        assertThat(ci).contains("chmod +x ./mvnw");
        assertThat(ci).contains("./mvnw -B clean verify");
        assertThat(ci).contains("actions/upload-artifact@v7");
        assertThat(ci).contains("docker compose --env-file .env.example config");
        assertThat(ci).contains("docker build -t votetrust-api:ci .");
    }

    @Test
    void deploymentChecklistDocumentsRequiredInputsAndSmokeChecks() throws IOException {
        String deployment = read("DEPLOYMENT.md");

        assertThat(deployment).contains("SPRING_DATASOURCE_URL");
        assertThat(deployment).contains("VOTETRUST_JWT_SECRET");
        assertThat(deployment).contains("docker compose --env-file .env config");
        assertThat(deployment).contains("Azure ACR Tasks Deployment");
        assertThat(deployment).contains("provision-acr-tasks-deployment.ps1");
        assertThat(deployment).contains("/actuator/health/readiness");
        assertThat(deployment).contains("CI Deployment Gates");
    }

    @Test
    void acrTaskBuildsPushesAndDeploysCommitTaggedContainerAppImage() throws IOException {
        String acrTask = read("infra/azure/acr-task.yaml");

        assertThat(acrTask).contains("version: v1.1.0");
        assertThat(acrTask).contains("-t $Registry/{{.Values.imageRepository}}:{{.Run.Commit}}");
        assertThat(acrTask).contains("-t $Registry/{{.Values.imageRepository}}:main");
        assertThat(acrTask).contains("push:");
        assertThat(acrTask).contains("az login --identity");
        assertThat(acrTask).contains("az containerapp update");
        assertThat(acrTask).contains("--image $Registry/{{.Values.imageRepository}}:{{.Run.Commit}}");
        assertThat(acrTask).doesNotContain("SPRING_DATASOURCE_PASSWORD");
        assertThat(acrTask).doesNotContain("VOTETRUST_JWT_SECRET");
        assertThat(acrTask).doesNotContain("keyvaultref:");
    }

    @Test
    void azureProvisioningUsesPrivateNetworkManagedIdentityAndKeyVaultReferences() throws IOException {
        String script = read("infra/azure/provision-acr-tasks-deployment.ps1");

        assertThat(script).contains("$Location = \"spaincentral\"");
        assertThat(script).contains("type: Microsoft.App/containerApps");
        assertThat(script).contains("Microsoft.App/environments");
        assertThat(script).contains("Microsoft.DBforPostgreSQL/flexibleServers");
        assertThat(script).contains("--public-access\", \"Disabled\"");
        assertThat(script).contains("--admin-enabled\", \"false\"");
        assertThat(script).contains("Key Vault Secrets User");
        assertThat(script).contains("AcrPull");
        assertThat(script).contains("Contributor");
        assertThat(script).contains("keyVaultUrl:");
        assertThat(script).contains("identity: \"$IdentityId\"");
        assertThat(script).contains("secretRef: db-url");
        assertThat(script).contains("/actuator/health/readiness");
        assertThat(script).contains("/actuator/health/liveness");
        assertThat(script).contains("--git-access-token\", $GitToken");
        assertThat(script).contains("public_repo and repo:status");
    }

    @Test
    void azureRunbookDocumentsValidationBootstrapOperationsAndRollback() throws IOException {
        String runbook = read("infra/azure/README.md");

        assertThat(runbook).contains("spaincentral");
        assertThat(runbook).contains("az acr task list-runs");
        assertThat(runbook).contains("az acr task logs");
        assertThat(runbook).contains("az acr task run");
        assertThat(runbook).contains("VOTETRUST_ADMIN_BOOTSTRAP_ENABLED=true");
        assertThat(runbook).contains("VOTETRUST_ADMIN_BOOTSTRAP_ENABLED=false");
        assertThat(runbook).contains("/actuator/health/readiness");
        assertThat(runbook).contains("swagger-ui.html");
        assertThat(runbook).contains("<previous-commit-sha>");
    }

    private String read(String relativePath) throws IOException {
        return Files.readString(Path.of(relativePath));
    }
}
