package uk.ac.ucl.comp0010.dto;

/**
 * Response payload summarising the average grade across a module.
 */
public record ModuleAverageResponse(String code, String name, Double averageScore) {
}
