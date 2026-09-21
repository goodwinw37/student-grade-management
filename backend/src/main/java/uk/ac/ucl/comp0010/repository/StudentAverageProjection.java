package uk.ac.ucl.comp0010.repository;

/**
 * Projection exposing student identifier with an average grade value.
 */
public interface StudentAverageProjection {

  Long getStudentId();

  Double getAverage();
}
