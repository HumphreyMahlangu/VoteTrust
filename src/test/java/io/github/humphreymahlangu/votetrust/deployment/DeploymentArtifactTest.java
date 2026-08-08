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

        assertThat(ci).contains("./mvnw -B test");
        assertThat(ci).contains("./mvnw -B -DskipTests package");
        assertThat(ci).contains("docker compose --env-file .env.example config");
        assertThat(ci).contains("docker build -t votetrust-api:ci .");
    }

    @Test
    void deploymentChecklistDocumentsRequiredInputsAndSmokeChecks() throws IOException {
        String deployment = read("DEPLOYMENT.md");

        assertThat(deployment).contains("SPRING_DATASOURCE_URL");
        assertThat(deployment).contains("VOTETRUST_JWT_SECRET");
        assertThat(deployment).contains("docker compose --env-file .env config");
        assertThat(deployment).contains("/actuator/health/readiness");
        assertThat(deployment).contains("CI Deployment Gates");
    }

    private String read(String relativePath) throws IOException {
        return Files.readString(Path.of(relativePath));
    }
}
