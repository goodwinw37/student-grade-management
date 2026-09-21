package uk.ac.ucl.comp0010.controller;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import uk.ac.ucl.comp0010.TestUsers;
import uk.ac.ucl.comp0010.model.Grade;
import uk.ac.ucl.comp0010.model.Module;
import uk.ac.ucl.comp0010.model.Registration;
import uk.ac.ucl.comp0010.model.Student;
import uk.ac.ucl.comp0010.repository.GradeRepository;
import uk.ac.ucl.comp0010.repository.ModuleRepository;
import uk.ac.ucl.comp0010.repository.RegistrationRepository;
import uk.ac.ucl.comp0010.repository.StudentRepository;

@SpringBootTest
@AutoConfigureMockMvc
class GradeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private StudentRepository studentRepository;

  @Autowired
  private ModuleRepository moduleRepository;

  @Autowired
  private RegistrationRepository registrationRepository;

  @Autowired
  private GradeRepository gradeRepository;

  @BeforeEach
  void cleanDatabase() {
    gradeRepository.deleteAll();
    registrationRepository.deleteAll();
    moduleRepository.deleteAll();
    studentRepository.deleteAll();
  }

  @Test
  void addGradeReturnsCreatedGrade() throws Exception {
    Student student = new Student(1L, "Test", "Student", "test", "test@example.com");
    studentRepository.save(student);
    Module module = new Module("CS101", "Software Engineering", true);
    moduleRepository.save(module);
    registrationRepository.save(new Registration(student, module));

    mockMvc.perform(post("/grades/addGrade")
        .with(httpBasic(TestUsers.TEACHER_USERNAME, TestUsers.TEACHER_PASSWORD))
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            "{\"student_id\":\"1\",\"module_code\":\"CS101\",\"score\":\"82\",\"academic_year\":\"2024/25\",\"semester\":\"Semester 1\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.score").value(82))
        .andExpect(jsonPath("$.academicYear").value("2024/25"))
        .andExpect(jsonPath("$.semester").value("Semester 1"));
  }

  @Test
  void addGradeFailsWhenNotRegistered() throws Exception {
    Student student = new Student(2L, "Another", "Student", "another", "another@example.com");
    studentRepository.save(student);
    Module module = new Module("COMP0020", "Advanced", false);
    moduleRepository.save(module);

    mockMvc.perform(post("/grades/addGrade")
        .with(httpBasic(TestUsers.TEACHER_USERNAME, TestUsers.TEACHER_PASSWORD))
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            "{\"student_id\":\"2\",\"module_code\":\"COMP0020\",\"score\":\"70\",\"academic_year\":\"2024/25\",\"semester\":\"Semester 1\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Student 2 is not registered for module COMP0020"));
  }

  @Test
  void addGradeFailsWhenStudentMissing() throws Exception {
    moduleRepository.save(new Module("COMP0030", "Missing Student", false));

    mockMvc.perform(post("/grades/addGrade")
        .with(httpBasic(TestUsers.TEACHER_USERNAME, TestUsers.TEACHER_PASSWORD))
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            "{\"student_id\":\"99\",\"module_code\":\"COMP0030\",\"score\":\"60\",\"academic_year\":\"2024/25\",\"semester\":\"Semester 1\"}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Student not found: 99"));
  }

  @Test
  void addGradeFailsWhenScoreNotNumeric() throws Exception {
    Student student = new Student(4L, "Numeric", "Test", "numeric", "numeric@example.com");
    studentRepository.save(student);
    Module module = new Module("COMP0035", "Numbers", false);
    moduleRepository.save(module);
    registrationRepository.save(new Registration(student, module));

    mockMvc.perform(post("/grades/addGrade")
        .with(httpBasic(TestUsers.TEACHER_USERNAME, TestUsers.TEACHER_PASSWORD))
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            "{\"student_id\":\"4\",\"module_code\":\"COMP0035\",\"score\":\"abc\",\"academic_year\":\"2024/25\",\"semester\":\"Semester 1\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid numeric value for score"));
  }

  @Test
  void addGradeFailsWhenAcademicYearInvalid() throws Exception {
    Student student = new Student(5L, "Year", "Test", "year", "year@example.com");
    studentRepository.save(student);
    Module module = new Module("COMP0045", "Validation", false);
    moduleRepository.save(module);
    registrationRepository.save(new Registration(student, module));

    mockMvc.perform(post("/grades/addGrade")
        .with(httpBasic(TestUsers.TEACHER_USERNAME, TestUsers.TEACHER_PASSWORD))
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            "{\"student_id\":\"5\",\"module_code\":\"COMP0045\",\"score\":\"75\",\"academic_year\":\"2024-25\",\"semester\":\"Semester 1\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Academic year must match pattern YYYY/YY"));
  }

  @Test
  void addGradeFailsWhenSemesterInvalid() throws Exception {
    Student student = new Student(6L, "Semester", "Test", "semester", "semester@example.com");
    studentRepository.save(student);
    Module module = new Module("COMP0050", "Semester Validation", false);
    moduleRepository.save(module);
    registrationRepository.save(new Registration(student, module));

    mockMvc.perform(post("/grades/addGrade")
        .with(httpBasic(TestUsers.TEACHER_USERNAME, TestUsers.TEACHER_PASSWORD))
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            "{\"student_id\":\"6\",\"module_code\":\"COMP0050\",\"score\":\"78\",\"academic_year\":\"2024/25\",\"semester\":\"Q4\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.message").value("Semester must be one of Semester 1, Semester 2, Summer"));
  }

  @Test
  void addGradeFailsWhenDuplicateExists() throws Exception {
    Student student = new Student(11L, "Duplicate", "Check", "duplicate",
        "duplicate@example.com");
    studentRepository.save(student);
    Module module = new Module("COMP0100", "Duplicate Module", false);
    moduleRepository.save(module);
    registrationRepository.save(new Registration(student, module));
    gradeRepository.save(new Grade(88, "2024/25", "Semester 1", student, module));

    mockMvc.perform(post("/grades/addGrade")
        .with(httpBasic(TestUsers.TEACHER_USERNAME, TestUsers.TEACHER_PASSWORD))
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            "{\"student_id\":\"11\",\"module_code\":\"COMP0100\",\"score\":\"90\",\"academic_year\":\"2024/25\",\"semester\":\"Semester 1\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(
            "Grade already exists for student 11 in module COMP0100 (2024/25, Semester 1)"));
  }

  @Test
  void deleteGradeRemovesRecord() throws Exception {
    Student student = new Student(12L, "Delete", "Test", "delete", "delete@example.com");
    studentRepository.save(student);
    Module module = new Module("COMP0110", "Delete Grade", false);
    moduleRepository.save(module);
    registrationRepository.save(new Registration(student, module));
    Grade grade = gradeRepository.save(new Grade(70, "2024/25", "Semester 1", student, module));

    mockMvc.perform(delete("/grades/{id}", grade.getId())
        .with(httpBasic(TestUsers.TEACHER_USERNAME, TestUsers.TEACHER_PASSWORD)))
        .andExpect(status().isNoContent());

    assertThat(gradeRepository.existsById(grade.getId())).isFalse();
  }

  @Test
  void deleteGradeForbiddenForStudents() throws Exception {
    Student student = new Student(13L, "Forbidden", "Delete", "delete-student",
        "delete-student@example.com");
    studentRepository.save(student);
    Module module = new Module("COMP0115", "Protected", false);
    moduleRepository.save(module);
    registrationRepository.save(new Registration(student, module));
    Grade grade = gradeRepository.save(new Grade(82, "2024/25", "Semester 2", student, module));

    mockMvc.perform(delete("/grades/{id}", grade.getId())
        .with(httpBasic(TestUsers.STUDENT_USERNAME, TestUsers.STUDENT_PASSWORD)))
        .andExpect(status().isForbidden());
  }

  @Test
  void deleteGradeReturnsNotFoundForMissingRecord() throws Exception {
    mockMvc.perform(delete("/grades/{id}", 9999)
        .with(httpBasic(TestUsers.TEACHER_USERNAME, TestUsers.TEACHER_PASSWORD)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Grade not found: 9999"));
  }

  @Test
  void listGradesReturnsStudentAndModuleSummaries() throws Exception {
    Student student = new Student(3L, "List", "Tester", "list", "list@example.com");
    studentRepository.save(student);
    Module module = new Module("COMP0040", "Interaction Design", false);
    moduleRepository.save(module);
    registrationRepository.save(new Registration(student, module));
    Grade grade = new Grade(71, "2024/25", "Semester 1", student, module);
    gradeRepository.save(grade);

    mockMvc.perform(get("/grades")
        .with(httpBasic(TestUsers.STUDENT_USERNAME, TestUsers.STUDENT_PASSWORD)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].student.username").value("list"))
        .andExpect(jsonPath("$[0].module.code").value("COMP0040"))
        .andExpect(jsonPath("$[0].score").value(71))
        .andExpect(jsonPath("$[0].academicYear").value("2024/25"))
        .andExpect(jsonPath("$[0].semester").value("Semester 1"));
  }

  @Test
  void studentCannotAddGrade() throws Exception {
    Student student = new Student(7L, "Forbidden", "Student", "forbidden",
        "forbidden@example.com");
    studentRepository.save(student);
    Module module = new Module("COMP0055", "Secured", false);
    moduleRepository.save(module);
    registrationRepository.save(new Registration(student, module));

    mockMvc.perform(post("/grades/addGrade")
        .with(httpBasic(TestUsers.STUDENT_USERNAME, TestUsers.STUDENT_PASSWORD))
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            "{\"student_id\":\"7\",\"module_code\":\"COMP0055\",\"score\":\"80\",\"academic_year\":\"2024/25\",\"semester\":\"Semester 1\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void listMyGradesReturnsOnlyAuthenticatedStudentsRecords() throws Exception {
    Student ada = new Student(8L, "Ada", "Lovelace", "ada", "ada@example.com");
    studentRepository.save(ada);
    Student alan = new Student(9L, "Alan", "Turing", "alan", "alan@example.com");
    studentRepository.save(alan);
    Module module = new Module("COMP0099", "Security", false);
    moduleRepository.save(module);
    registrationRepository.save(new Registration(ada, module));
    registrationRepository.save(new Registration(alan, module));
    gradeRepository.save(new Grade(90, "2024/25", "Semester 1", ada, module));
    gradeRepository.save(new Grade(55, "2024/25", "Semester 1", alan, module));

    mockMvc.perform(get("/grades/my")
        .with(httpBasic(TestUsers.STUDENT_USERNAME, TestUsers.STUDENT_PASSWORD)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].student.username").value("ada"))
        .andExpect(jsonPath("$[0].score").value(90));
  }
}