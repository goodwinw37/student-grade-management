package uk.ac.ucl.comp0010.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating module records.
 */
public record ModuleRequest(
    @NotBlank @Size(max = 10) String code,
    @NotBlank @Size(max = 100) String name,
    boolean mnc
) {
}
