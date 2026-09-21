package uk.ac.ucl.comp0010.exception;

/**
 * Signals that no grade information exists for the requested context.
 */
public class NoGradeAvailableException extends RuntimeException {

  /**
   * Creates the exception with a descriptive message.
   *
   * @param message explanation of the missing grade scenario
   */
  public NoGradeAvailableException(String message) {
    super(message);
  }
}
