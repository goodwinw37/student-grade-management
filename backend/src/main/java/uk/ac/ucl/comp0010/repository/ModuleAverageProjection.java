package uk.ac.ucl.comp0010.repository;

/**
 * Projection exposing module code with an average grade value.
 */
public interface ModuleAverageProjection {

  String getCode();

  Double getAverage();
}
