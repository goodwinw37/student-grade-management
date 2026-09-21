package uk.ac.ucl.comp0010.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.ac.ucl.comp0010.model.Grade;
import uk.ac.ucl.comp0010.model.Module;
import uk.ac.ucl.comp0010.model.Student;

/**
 * Exercises the DTO projection helper to keep coverage high for the dto package.
 */
class GradeResponseTest {

  @Test
  void buildsResponseFromEntity() {
    Student student = new Student(5L, "DTO", "Tester", "dto", "dto@example.com");
    Module module = new Module("COMP0050", "Testing", true);
    Grade grade = new Grade(95, "2024/25", "Semester 1", student, module);

    GradeResponse response = GradeResponse.fromEntity(grade);

    assertThat(response.score()).isEqualTo(95);
    assertThat(response.academicYear()).isEqualTo("2024/25");
    assertThat(response.semester()).isEqualTo("Semester 1");
    assertThat(response.student().username()).isEqualTo("dto");
    assertThat(response.module().code()).isEqualTo("COMP0050");
  }
}
