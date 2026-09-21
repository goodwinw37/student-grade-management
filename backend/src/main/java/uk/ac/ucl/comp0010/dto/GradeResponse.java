package uk.ac.ucl.comp0010.dto;

import uk.ac.ucl.comp0010.model.Grade;
import uk.ac.ucl.comp0010.model.Module;
import uk.ac.ucl.comp0010.model.Student;

/**
 * Read model exposing grade metadata tailored for the frontend.
 */
public record GradeResponse(Long id, Integer score, String academicYear, String semester,
    StudentSummary student,
    ModuleSummary module) {

  /**
   * Builds a response DTO from a JPA entity.
   *
   * @param grade the persisted grade entity
   * @return a serialisable response representation
   */
  public static GradeResponse fromEntity(Grade grade) {
    Student student = grade.getStudent();
    Module module = grade.getModule();
    return new GradeResponse(
        grade.getId(),
        grade.getScore(),
        grade.getAcademicYear(),
        grade.getSemester(),
        new StudentSummary(student.getId(), student.getFirstName(), student.getLastName(),
          student.getUsername()),
        new ModuleSummary(module.getCode(), module.getName(), module.isMnc()));
  }

  /**
   * Compact representation of a student shown next to a grade.
   */
  public record StudentSummary(Long id, String firstName, String lastName, String username) {
  }

  /**
   * Compact representation of a module shown next to a grade.
   */
  public record ModuleSummary(String code, String name, boolean mnc) {
  }
}
