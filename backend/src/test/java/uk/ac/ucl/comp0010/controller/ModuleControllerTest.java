package uk.ac.ucl.comp0010.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ucl.comp0010.TestUsers;
import uk.ac.ucl.comp0010.model.Module;
import uk.ac.ucl.comp0010.repository.GradeRepository;
import uk.ac.ucl.comp0010.repository.ModuleRepository;
import uk.ac.ucl.comp0010.repository.RegistrationRepository;
import uk.ac.ucl.comp0010.repository.StudentRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ModuleControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ModuleRepository moduleRepository;

  @Autowired
  private RegistrationRepository registrationRepository;

  @Autowired
  private GradeRepository gradeRepository;

  @Autowired
  private StudentRepository studentRepository;

  @BeforeEach
  void cleanDatabase() {
    gradeRepository.deleteAll();
    registrationRepository.deleteAll();
    moduleRepository.deleteAll();
    studentRepository.deleteAll();
  }

  @Test
  void createModuleUpperCasesCode() throws Exception {
    mockMvc.perform(post("/management/modules")
            .with(httpBasic(TestUsers.ADMIN_USERNAME, TestUsers.ADMIN_PASSWORD))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                "\"code\":\"comp9999\"," +
                "\"name\":\"Demo Module\"," +
                "\"mnc\":false}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code").value("COMP9999"));
  }

  @Test
  void createModuleRejectsDuplicateCode() throws Exception {
    moduleRepository.save(new Module("COMP1000", "Existing", false));

    mockMvc.perform(post("/management/modules")
            .with(httpBasic(TestUsers.ADMIN_USERNAME, TestUsers.ADMIN_PASSWORD))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                "\"code\":\"COMP1000\"," +
                "\"name\":\"Duplicate\"," +
                "\"mnc\":true}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Module code 'COMP1000' already exists"));
  }

  @Test
  void listModulesReturnsSortedCodes() throws Exception {
    moduleRepository.save(new Module("COMP2001", "Second", false));
    moduleRepository.save(new Module("COMP2000", "First", false));

    mockMvc.perform(get("/management/modules")
        .with(httpBasic(TestUsers.TEACHER_USERNAME, TestUsers.TEACHER_PASSWORD)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].code").value("COMP2000"))
        .andExpect(jsonPath("$[1].code").value("COMP2001"));
  }

  @Test
  void teacherCannotCreateModule() throws Exception {
    mockMvc.perform(post("/management/modules")
            .with(httpBasic(TestUsers.TEACHER_USERNAME, TestUsers.TEACHER_PASSWORD))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                "\"code\":\"COMP3000\"," +
                "\"name\":\"Unauthorized\"," +
                "\"mnc\":true}"))
        .andExpect(status().isForbidden());
  }
}
