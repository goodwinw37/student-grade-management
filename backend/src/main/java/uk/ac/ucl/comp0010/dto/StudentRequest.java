package uk.ac.ucl.comp0010.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a student record via the API.
 */
public record StudentRequest(
    Long id,
    @NotBlank @Size(max = 30) String firstName,
    @NotBlank @Size(max = 30) String lastName,
    @NotBlank @Size(max = 30) String username,
    @NotBlank @Email @Size(max = 50) String email
) {
}
