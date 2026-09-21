package uk.ac.ucl.comp0010.dto;

/**
 * Response payload summarising the average grade for a student.
 */
public record StudentAverageResponse(Long id, String firstName, String lastName,
    Double averageScore) {
}
