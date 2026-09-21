package uk.ac.ucl.comp0010.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.ac.ucl.comp0010.exception.NoGradeAvailableException;
import uk.ac.ucl.comp0010.exception.NoRegistrationException;

/**
 * Centralised exception handling for REST endpoints.
 */
@RestControllerAdvice
public class RestExceptionHandler {

  /**
   * Handles cases where a student attempts to add a grade without registration.
   */
  @ExceptionHandler(NoRegistrationException.class)
  public ResponseEntity<Map<String, String>> handleNoRegistration(
      NoRegistrationException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("message", exception.getMessage()));
  }

  /**
   * Handles scenarios where the referenced grade context cannot be found.
   */
  @ExceptionHandler(NoGradeAvailableException.class)
  public ResponseEntity<Map<String, String>> handleNoGrade(
      NoGradeAvailableException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Map.of("message", exception.getMessage()));
  }

  /**
   * Handles general validation errors raised by the controller helpers.
   */
  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<Map<String, String>> handleIllegalArgument(
      RuntimeException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("message", exception.getMessage()));
  }
}
