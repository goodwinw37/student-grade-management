package uk.ac.ucl.comp0010.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uk.ac.ucl.comp0010.model.Grade;
import uk.ac.ucl.comp0010.model.Module;
import uk.ac.ucl.comp0010.model.Registration;
import uk.ac.ucl.comp0010.model.Student;

/**
 * Ensures Spring Data queries declared in the repositories operate as expected.
 */
@DataJpaTest
class RepositoryIntegrationTest {

  @Autowired
  private StudentRepository studentRepository;

  @Autowired
  private ModuleRepository moduleRepository;

  @Autowired
  private RegistrationRepository registrationRepository;

  @Autowired
  private GradeRepository gradeRepository;

  @Test
  void repositoryFindersReturnPersistedData() {
    Student student = studentRepository.save(new Student(20L, "Repo", "Tester", "repo", "repo@example.com"));
    Module module = moduleRepository.save(new Module("COMP0090", "Persistence", false));
    registrationRepository.save(new Registration(student, module));
    gradeRepository.save(new Grade(78, "2024/25", "Semester 1", student, module));

    assertThat(studentRepository.findByUsername("repo")).isPresent();
    assertThat(moduleRepository.findByName("Persistence")).contains(module);

    assertThat(registrationRepository.findByStudentAndModule(student, module)).isPresent();

    assertThat(gradeRepository.findByStudent_Id(student.getId())).hasSize(1);
    assertThat(gradeRepository.findByModule_Code(module.getCode())).hasSize(1);
  }
}
