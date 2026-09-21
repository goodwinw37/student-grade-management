package uk.ac.ucl.comp0010.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

/**
 * Covers equals/hashCode and basic mutators on the JPA entities.
 */
class DomainEntitiesTest {

  @Test
  void studentEqualityUsesIdentifier() {
    Student first = new Student(10L, "Ada", "Tester", "ada", "ada@example.com");
    Student second = new Student(10L, "Ada", "Tester", "ada", "ada@example.com");
    assertThat(first).isEqualTo(second);
    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }

  @Test
  void moduleStoresFieldsAndEquality() {
    Module module = new Module("COMP0060", "Design", true);
    assertThat(module.getName()).isEqualTo("Design");
    assertThat(module.isMnc()).isTrue();
    Module copy = new Module("COMP0060", "Design", true);
    assertThat(module).isEqualTo(copy);
  }

  @Test
  void registrationReflectsStudentAndModule() {
    Student student = new Student(11L, "Reg", "Tester", "reg", "reg@example.com");
    Module module = new Module("COMP0070", "Delivery", false);
    Registration registration = new Registration(student, module);
    assertThat(registration.getStudent()).isEqualTo(student);
    assertThat(registration.getModule()).isEqualTo(module);
  }

  @Test
  void gradeStoresScoreAndAssociations() {
    Student student = new Student(12L, "Grade", "Tester", "grade", "grade@example.com");
    Module module = new Module("COMP0080", "QA", false);
    Grade grade = new Grade(88, "2024/25", "Semester 1", student, module);
    assertThat(grade.getScore()).isEqualTo(88);
    assertThat(grade.getAcademicYear()).isEqualTo("2024/25");
    assertThat(grade.getSemester()).isEqualTo("Semester 1");
    assertThat(grade.getStudent()).isEqualTo(student);
    assertThat(grade.getModule()).isEqualTo(module);
  }

  @Test
  void studentNoArgConstructorSupportsMutators() {
    Student student = new Student();
    student.setId(30L);
    student.setFirstName("Mutate");
    student.setLastName("Tester");
    student.setUsername("mutate");
    student.setEmail("mutate@example.com");

    assertThat(student.getId()).isEqualTo(30L);
    assertThat(student.getFirstName()).isEqualTo("Mutate");
    assertThat(student.getUsername()).isEqualTo("mutate");
    assertThat(student.getRegistrations()).isEmpty();
    assertThat(student.getGrades()).isEmpty();
  }

  @Test
  void computeAverageReturnsNullWhenNoGrades() {
    Student student = new Student(31L, "Avg", "Tester", "avgtester", "avg@example.com");

    assertThat(student.computeAverage()).isNull();
  }

  @Test
  void studentGradeHelpersCalculateAndLookupScores() {
    Student student = new Student(32L, "Grade", "Helper", "gradehelper", "grade@example.com");
    Module comp100 = new Module("COMP100", "Architecture", false);
    Module comp200 = new Module("COMP200", "Algorithms", false);

    Grade grade1 = gradeWithId(1L, comp100, 80);
    Grade grade2 = gradeWithId(2L, comp200, 90);

    student.addGrade(grade1);
    student.addGrade(grade2);

    assertThat(student.computeAverage()).isEqualTo(85.0f);
    assertThat(student.getGrade(comp100)).isEqualTo(grade1);
  }

  @Test
  void addGradePreventsDuplicateModules() {
    Student student = new Student(33L, "Dup", "Grade", "dupgrade", "dup@example.com");
    Module module = new Module("COMP300", "Testing", false);

    Grade first = gradeWithId(3L, module, 70);
    student.addGrade(first);

    Grade duplicate = gradeWithId(4L, module, 75);

    assertThatThrownBy(() -> student.addGrade(duplicate))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("COMP300");
  }

  @Test
  void registerModuleAddsRegistrationAndBlocksDuplicates() {
    Student student = new Student(34L, "Reg", "Helper", "reghelper", "reg@example.com");
    Module module = new Module("COMP400", "Delivery", false);

    student.registerModule(module);

    assertThat(student.getRegistrations()).hasSize(1);
    assertThat(student.getRegistrations().iterator().next().getModule()).isEqualTo(module);

    assertThatThrownBy(() -> student.registerModule(module))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("COMP400");
  }

  private Grade gradeWithId(long id, Module module, int score) {
    Grade grade = new Grade();
    grade.setModule(module);
    grade.setScore(score);
    setGradeId(grade, id);
    return grade;
  }

  private void setGradeId(Grade grade, long id) {
    try {
      Field field = Grade.class.getDeclaredField("id");
      field.setAccessible(true);
      field.set(grade, id);
    } catch (ReflectiveOperationException ex) {
      throw new AssertionError("Unable to set grade id", ex);
    }
  }

  @Test
  void moduleNoArgConstructorSupportsMutators() {
    Module module = new Module();
    module.setCode("COMP0100");
    module.setName("Mutators");
    module.setMnc(true);

    assertThat(module.getCode()).isEqualTo("COMP0100");
    assertThat(module.getName()).isEqualTo("Mutators");
    assertThat(module.isMnc()).isTrue();
    assertThat(module.getRegistrations()).isEmpty();
    assertThat(module.getGrades()).isEmpty();
  }

  @Test
  void registrationNoArgConstructorSupportsMutators() {
    Student student = new Student(40L, "Reg", "Mutator", "regmut", "regmut@example.com");
    Module module = new Module("COMP0110", "Registrations", false);
    Registration registration = new Registration();
    registration.setStudent(student);
    registration.setModule(module);

    assertThat(registration.getStudent()).isEqualTo(student);
    assertThat(registration.getModule()).isEqualTo(module);
    assertThat(registration.getId()).isNull();
    Registration other = new Registration();
    assertThat(registration).isEqualTo(other);
    assertThat(registration.hashCode()).isEqualTo(other.hashCode());
  }

  @Test
  void gradeNoArgConstructorSupportsMutators() {
    Student student = new Student(50L, "Grade", "Mutator", "grademut", "grademut@example.com");
    Module module = new Module("COMP0120", "Grades", false);
    Grade grade = new Grade();
    grade.setStudent(student);
    grade.setModule(module);
    grade.setScore(77);
    grade.setAcademicYear("2024/25");
    grade.setSemester("Semester 1");

    assertThat(grade.getStudent()).isEqualTo(student);
    assertThat(grade.getModule()).isEqualTo(module);
    assertThat(grade.getScore()).isEqualTo(77);
    assertThat(grade.getAcademicYear()).isEqualTo("2024/25");
    assertThat(grade.getSemester()).isEqualTo("Semester 1");
    assertThat(grade.getId()).isNull();
    Grade other = new Grade();
    assertThat(grade).isEqualTo(other);
    assertThat(grade.hashCode()).isEqualTo(other.hashCode());
  }
}
