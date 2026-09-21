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
import uk.ac.ucl.comp0010.model.Module;
import uk.ac.ucl.comp0010.model.Registration;
import uk.ac.ucl.comp0010.model.Student;
import uk.ac.ucl.comp0010.repository.GradeRepository;
import uk.ac.ucl.comp0010.repository.ModuleRepository;
import uk.ac.ucl.comp0010.repository.RegistrationRepository;
import uk.ac.ucl.comp0010.repository.StudentRepository;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private RegistrationRepository registrationRepository;

  @Autowired
  private StudentRepository studentRepository;

  @Autowired
  private ModuleRepository moduleRepository;

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
  void registerCreatesLinkBetweenStudentAndModule() throws Exception {
    Student student = studentRepository.save(new Student(50L, "Reg", "Student", "regstud",
        "reg@student.com"));
    Module module = moduleRepository.save(new Module("COMP0500", "Registration", false));

    mockMvc.perform(post("/management/registrations")
        .with(httpBasic(TestUsers.ADMIN_USERNAME, TestUsers.ADMIN_PASSWORD))
        .contentType(MediaType.APPLICATION_JSON)
        .content("{" +
          "\"studentId\":" + student.getId() + "," +
          "\"moduleCode\":\"" + module.getCode() + "\"}"))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.studentRecordId").value(50))
      .andExpect(jsonPath("$.username").value("regstud"))
      .andExpect(jsonPath("$.moduleCode").value("COMP0500"))
      .andExpect(jsonPath("$.moduleName").value("Registration"));
  }

  @Test
  void registerPreventsDuplicateCombinations() throws Exception {
    Student student = studentRepository.save(new Student(60L, "Dup", "Student", "dupstud",
        "dup@student.com"));
    Module module = moduleRepository.save(new Module("COMP0600", "Duplicates", false));
    registrationRepository.save(new Registration(student, module));

    mockMvc.perform(post("/management/registrations")
            .with(httpBasic(TestUsers.ADMIN_USERNAME, TestUsers.ADMIN_PASSWORD))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                "\"studentId\":" + student.getId() + "," +
                "\"moduleCode\":\"" + module.getCode() + "\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Student 60 already registered for COMP0600"));
  }

  @Test
  void registerFailsWhenStudentMissing() throws Exception {
    moduleRepository.save(new Module("COMP0800", "Missing Student", false));

    mockMvc.perform(post("/management/registrations")
            .with(httpBasic(TestUsers.ADMIN_USERNAME, TestUsers.ADMIN_PASSWORD))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                "\"studentId\":999," +
                "\"moduleCode\":\"COMP0800\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Student not found: 999"));
  }

  @Test
  void registerFailsWhenModuleMissing() throws Exception {
    Student student = studentRepository.save(new Student(81L, "Missing", "Module", "missingmod",
        "missing.mod@example.com"));

    mockMvc.perform(post("/management/registrations")
            .with(httpBasic(TestUsers.ADMIN_USERNAME, TestUsers.ADMIN_PASSWORD))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                "\"studentId\":81," +
                "\"moduleCode\":\" comp0999 \"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Module not found: COMP0999"));
  }

  @Test
  void listRegistrationsReturnsSummaries() throws Exception {
    Student student = studentRepository.save(new Student(70L, "List", "Student", "liststud",
        "list@student.com"));
    Module module = moduleRepository.save(new Module("COMP0700", "Listing", false));
    registrationRepository.save(new Registration(student, module));

    mockMvc.perform(get("/management/registrations")
        .with(httpBasic(TestUsers.TEACHER_USERNAME, TestUsers.TEACHER_PASSWORD)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].studentRecordId").value(70))
      .andExpect(jsonPath("$[0].username").value("liststud"))
      .andExpect(jsonPath("$[0].moduleCode").value("COMP0700"));
  }

  @Test
  void teacherCannotRegisterStudents() throws Exception {
    Student student = studentRepository.save(new Student(90L, "Blocked", "Teacher", "blocked",
        "blocked@example.com"));
    Module module = moduleRepository.save(new Module("COMP0900", "Blocked", false));

    mockMvc.perform(post("/management/registrations")
            .with(httpBasic(TestUsers.TEACHER_USERNAME, TestUsers.TEACHER_PASSWORD))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                "\"studentId\":" + student.getId() + "," +
                "\"moduleCode\":\"" + module.getCode() + "\"}"))
        .andExpect(status().isForbidden());
  }
}