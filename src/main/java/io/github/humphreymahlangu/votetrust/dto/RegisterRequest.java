package io.github.humphreymahlangu.votetrust.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for creating a voter platform account")
public record RegisterRequest(
        @NotBlank
        @Email
        @Size(max = 320)
        @Schema(description = "Voter email address used for authentication", example = "voter@example.com")
        String email,

        @NotBlank
        @Size(min = 12, max = 128)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "must contain at least one uppercase letter, one lowercase letter, and one digit"
        )
        @Schema(description = "Strong account password", example = "StrongPass123", format = "password")
        String password
) {
}
