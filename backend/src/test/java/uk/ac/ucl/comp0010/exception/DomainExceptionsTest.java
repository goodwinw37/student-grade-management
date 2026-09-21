package uk.ac.ucl.comp0010.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Simple coverage-focused tests for custom runtime exceptions.
 */
class DomainExceptionsTest {

  @Test
  void noGradeAvailableExceptionStoresMessage() {
    NoGradeAvailableException exception = new NoGradeAvailableException("missing grade");
    assertThat(exception).hasMessage("missing grade");
  }

  @Test
  void noRegistrationExceptionStoresMessage() {
    NoRegistrationException exception = new NoRegistrationException("missing registration");
    assertThat(exception).hasMessage("missing registration");
  }
}
