package uk.ac.ucl.comp0010.exception;

/**
 * Indicates that a student attempted to interact with a module without registration.
 */
public class NoRegistrationException extends RuntimeException {

  /**
   * Creates the exception with a descriptive message.
   *
   * @param message explanation of the registration issue
   */
  public NoRegistrationException(String message) {
    super(message);
  }
}
