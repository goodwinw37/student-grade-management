package uk.ac.ucl.comp0010.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import uk.ac.ucl.comp0010.TestUsers;
import uk.ac.ucl.comp0010.model.Student;
import uk.ac.ucl.comp0010.repository.GradeRepository;
import uk.ac.ucl.comp0010.repository.ModuleRepository;
import uk.ac.ucl.comp0010.repository.RegistrationRepository;
import uk.ac.ucl.comp0010.repository.StudentRepository;

@SpringBootTest
@AutoConfigureMockMvc
class StudentControllerTest {

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
  void createStudentGeneratesIdWhenMissing() throws Exception {
    mockMvc.perform(post("/management/students")
            .with(httpBasic(TestUsers.ADMIN_USERNAME, TestUsers.ADMIN_PASSWORD))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                "\"firstName\":\"New\"," +
                "\"lastName\":\"Student\"," +
                "\"username\":\"newstudent\"," +
                "\"email\":\"new.student@example.com\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.username").value("newstudent"));
  }

  @Test
    void createStudentRejectsDuplicateUsername() throws Exception {
    studentRepository.save(new Student(10L, "Existing", "User", "taken", "taken@example.com"));

    mockMvc.perform(post("/management/students")
            .with(httpBasic(TestUsers.ADMIN_USERNAME, TestUsers.ADMIN_PASSWORD))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                "\"firstName\":\"Dup\"," +
                "\"lastName\":\"User\"," +
      "\"username\":\"taken\"," +
                "\"email\":\"dup.user@example.com\"}"))
        .andExpect(status().isBadRequest())
    .andExpect(jsonPath("$.message").value("Username 'taken' is already in use"));
  }

  @Test
  void createStudentRejectsDuplicateEmail() throws Exception {
    studentRepository.save(new Student(11L, "Existing", "Email", "uniqueuser",
        "dup.email@example.com"));

    mockMvc.perform(post("/management/students")
            .with(httpBasic(TestUsers.ADMIN_USERNAME, TestUsers.ADMIN_PASSWORD))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                "\"firstName\":\"Dup\"," +
                "\"lastName\":\"Email\"," +
                "\"username\":\"anotheruser\"," +
                "\"email\":\"dup.email@example.com\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Email 'dup.email@example.com' is already in use"));
  }

  @Test
  void createStudentRejectsDuplicateId() throws Exception {
    studentRepository.save(new Student(12L, "Existing", "Id", "iduser", "id@example.com"));

    mockMvc.perform(post("/management/students")
            .with(httpBasic(TestUsers.ADMIN_USERNAME, TestUsers.ADMIN_PASSWORD))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                "\"id\":12," +
                "\"firstName\":\"Dup\"," +
                "\"lastName\":\"Id\"," +
                "\"username\":\"anotherid\"," +
                "\"email\":\"another.id@example.com\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Student record ID 12 already exists"));
  }

  @Test
  void listStudentsReturnsSortedCollection() throws Exception {
    studentRepository.save(new Student(2L, "Second", "Student", "second", "second@example.com"));
    studentRepository.save(new Student(1L, "First", "Student", "first", "first@example.com"));

    mockMvc.perform(get("/management/students")
        .with(httpBasic(TestUsers.TEACHER_USERNAME, TestUsers.TEACHER_PASSWORD)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[1].id").value(2));
  }

  @Test
  void teacherCannotCreateStudent() throws Exception {
    mockMvc.perform(post("/management/students")
            .with(httpBasic(TestUsers.TEACHER_USERNAME, TestUsers.TEACHER_PASSWORD))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                "\"firstName\":\"Teach\"," +
                "\"lastName\":\"Student\"," +
                "\"username\":\"teachstudent\"," +
                "\"email\":\"teach.student@example.com\"}"))
        .andExpect(status().isForbidden());
  }
}