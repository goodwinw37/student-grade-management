package uk.ac.ucl.comp0010.dto;

import uk.ac.ucl.comp0010.model.Module;
import uk.ac.ucl.comp0010.model.Registration;
import uk.ac.ucl.comp0010.model.Student;

/**
 * Lightweight projection describing a registration record returned by the API.
 */
public record RegistrationResponse(
    Long id,
    Long studentRecordId,
    String username,
    String moduleCode,
    String moduleName
) {
  /**
   * Maps an entity to its lightweight REST representation.
   */
  public static RegistrationResponse fromEntity(Registration registration) {
    Student student = registration.getStudent();
    Module module = registration.getModule();
    return new RegistrationResponse(registration.getId(), student.getId(), student.getUsername(),
        module.getCode(), module.getName());
  }
}
