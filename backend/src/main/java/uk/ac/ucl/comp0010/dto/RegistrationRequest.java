package uk.ac.ucl.comp0010.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for linking a student to a module.
 */
public record RegistrationRequest(
    @NotNull Long studentId,
    @NotBlank @Size(max = 10) String moduleCode
) {
}
