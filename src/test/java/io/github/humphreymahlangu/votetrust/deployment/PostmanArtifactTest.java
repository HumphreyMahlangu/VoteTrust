package io.github.humphreymahlangu.votetrust.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.humphreymahlangu.votetrust.entity.IdDocumentType;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class PostmanArtifactTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void postmanCollectionIsValidAndCoversCoreVotingFlow() throws IOException {
        JsonNode collection = objectMapper.readTree(Path.of("postman/VoteTrust.postman_collection.json").toFile());
        String collectionJson = collection.toString();

        assertThat(collection.at("/info/schema").asText())
                .isEqualTo("https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
        assertThat(collection.at("/info/name").asText()).isEqualTo("VoteTrust API");
        assertThat(collectionJson).contains(
                "/api/v1/admin/bootstrap",
                "/api/v1/admin/voting-districts",
                "/api/v1/elections/{{electionId}}/registrations",
                "/api/v1/elections/{{electionId}}/contests/{{contestId}}/credentials",
                "/api/v1/ballots",
                "/api/v1/elections/{{electionId}}/contests/{{contestId}}/audit",
                "/api/v1/admin/security-audit-events?limit=25"
        );
        assertThat(collectionJson).contains(
                "BLANK_BALLOT",
                "SPOILT_BALLOT",
                IdDocumentType.SMART_ID_CARD.name(),
                "{{adminToken}}",
                "{{voterToken}}",
                "{{votingCredential}}"
        );
        assertThat(collectionJson).doesNotContain("SOUTH_AFRICAN_ID");
    }

    @Test
    void postmanEnvironmentContainsRequiredVariables() throws IOException {
        JsonNode environment = objectMapper.readTree(Path.of("postman/VoteTrust.local.postman_environment.json").toFile());
        Set<String> keys = new HashSet<>();
        environment.get("values").forEach(value -> keys.add(value.get("key").asText()));

        assertThat(environment.get("name").asText()).isEqualTo("VoteTrust Local");
        assertThat(keys).contains(
                "baseUrl",
                "adminBootstrapToken",
                "adminEmail",
                "adminPassword",
                "voterEmail",
                "voterPassword",
                "voterSouthAfricanIdNumber",
                "votingDistrictId",
                "electionId",
                "contestId",
                "optionAId",
                "blankOptionId",
                "spoiltOptionId",
                "adminToken",
                "voterToken",
                "votingCredential"
        );
    }

    @Test
    void postmanCollectionVariablesExistInEnvironment() throws IOException {
        String collection = read("postman/VoteTrust.postman_collection.json");
        JsonNode environment = objectMapper.readTree(Path.of("postman/VoteTrust.local.postman_environment.json").toFile());
        Set<String> environmentKeys = new HashSet<>();
        environment.get("values").forEach(value -> environmentKeys.add(value.get("key").asText()));

        Set<String> referencedVariables = new HashSet<>();
        Matcher matcher = Pattern.compile("\\{\\{([^}]+)}}").matcher(collection);
        while (matcher.find()) {
            referencedVariables.add(matcher.group(1));
        }

        assertThat(environmentKeys).containsAll(referencedVariables);
    }

    private String read(String relativePath) throws IOException {
        return java.nio.file.Files.readString(Path.of(relativePath));
    }
}
