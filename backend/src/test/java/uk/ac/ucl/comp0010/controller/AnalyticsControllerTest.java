package uk.ac.ucl.comp0010.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class AnalyticsControllerTest {

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
  void moduleAveragesIncludeModulesWithoutGrades() throws Exception {
    Module gradedModule = moduleRepository.save(new Module("COMP0200", "Graded", true));
    Module emptyModule = moduleRepository.save(new Module("COMP0201", "No Grades", false));
    Student student = studentRepository.save(new Student(200L, "Average", "Module", "avgmod",
        "avgmod@example.com"));
    registrationRepository.save(new Registration(student, gradedModule));
    gradeRepository.save(new Grade(80, "2024/25", "Semester 1", student, gradedModule));

    mockMvc.perform(get("/analytics/module-averages")
            .with(httpBasic(TestUsers.STUDENT_USERNAME, TestUsers.STUDENT_PASSWORD))
            .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].code").value("COMP0200"))
      .andExpect(jsonPath("$[0].averageScore").value(80.0))
      .andExpect(jsonPath("$[1].code").value("COMP0201"))
      .andExpect(jsonPath("$[1].averageScore").isEmpty());
  }

  @Test
  void studentAveragesIncludeStudentsWithoutGrades() throws Exception {
    Student gradedStudent = studentRepository
        .save(new Student(300L, "Average", "Student", "avgstu", "avgstu@example.com"));
    Student emptyStudent = studentRepository
        .save(new Student(301L, "Empty", "Student", "emptystu", "emptystu@example.com"));
    Module module = moduleRepository.save(new Module("COMP0202", "Analytics", false));
    registrationRepository.save(new Registration(gradedStudent, module));
    gradeRepository.save(new Grade(70, "2024/25", "Semester 1", gradedStudent, module));

    mockMvc.perform(get("/analytics/student-averages")
            .with(httpBasic(TestUsers.STUDENT_USERNAME, TestUsers.STUDENT_PASSWORD))
            .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].id").value(300))
      .andExpect(jsonPath("$[0].averageScore").value(70.0))
      .andExpect(jsonPath("$[1].id").value(301))
      .andExpect(jsonPath("$[1].averageScore").isEmpty());
  }

  @Test
  void topStudentsCanBeFilteredBySemester() throws Exception {
    Student semesterOne = studentRepository
        .save(new Student(310L, "Sem", "One", "semo", "sem1@example.com"));
    Student semesterTwo = studentRepository
        .save(new Student(311L, "Sem", "Two", "semt", "sem2@example.com"));
    Module module = moduleRepository.save(new Module("COMP0300", "Semesters", false));
    registrationRepository.save(new Registration(semesterOne, module));
    registrationRepository.save(new Registration(semesterTwo, module));
    gradeRepository.save(new Grade(95, "2024/25", "Semester 1", semesterOne, module));
    gradeRepository.save(new Grade(45, "2024/25", "Semester 2", semesterTwo, module));

    mockMvc.perform(get("/analytics/top-students")
            .with(httpBasic(TestUsers.STUDENT_USERNAME, TestUsers.STUDENT_PASSWORD))
            .param("limit", "1")
            .param("semester", "Semester 1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(310))
        .andExpect(jsonPath("$[0].averageScore").value(95.0));
  }

  @Test
  void topStudentsCanBeFilteredByAcademicYear() throws Exception {
    Student yearOne = studentRepository
        .save(new Student(320L, "Year", "One", "yearone", "year1@example.com"));
    Student yearTwo = studentRepository
        .save(new Student(321L, "Year", "Two", "yeartwo", "year2@example.com"));
    Module module = moduleRepository.save(new Module("COMP0301", "Years", false));
    registrationRepository.save(new Registration(yearOne, module));
    registrationRepository.save(new Registration(yearTwo, module));
    gradeRepository.save(new Grade(65, "2023/24", "Semester 1", yearOne, module));
    gradeRepository.save(new Grade(90, "2024/25", "Semester 1", yearTwo, module));

    mockMvc.perform(get("/analytics/top-students")
            .with(httpBasic(TestUsers.STUDENT_USERNAME, TestUsers.STUDENT_PASSWORD))
            .param("academicYear", "2023/24"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(320))
        .andExpect(jsonPath("$[0].averageScore").value(65.0));
  }

  @Test
  void topModulesRejectsInvalidLimit() throws Exception {
    mockMvc.perform(get("/analytics/top-modules")
            .with(httpBasic(TestUsers.STUDENT_USERNAME, TestUsers.STUDENT_PASSWORD))
            .param("limit", "0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Limit must be between 1 and 50"));
  }

  @Test
  void topModulesReturnsStrongestModules() throws Exception {
    Student student = studentRepository
        .save(new Student(330L, "Module", "Rank", "modrank", "rank@example.com"));
    Module strong = moduleRepository.save(new Module("COMP0400", "Strong", false));
    Module medium = moduleRepository.save(new Module("COMP0401", "Medium", false));
    Module weak = moduleRepository.save(new Module("COMP0402", "Weak", false));
    registrationRepository.save(new Registration(student, strong));
    registrationRepository.save(new Registration(student, medium));
    registrationRepository.save(new Registration(student, weak));
    gradeRepository.save(new Grade(95, "2024/25", "Semester 1", student, strong));
    gradeRepository.save(new Grade(70, "2024/25", "Semester 1", student, medium));
    gradeRepository.save(new Grade(40, "2024/25", "Semester 1", student, weak));

    mockMvc.perform(get("/analytics/top-modules")
            .with(httpBasic(TestUsers.STUDENT_USERNAME, TestUsers.STUDENT_PASSWORD))
            .param("limit", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].code").value("COMP0400"))
        .andExpect(jsonPath("$[0].averageScore").value(95.0))
        .andExpect(jsonPath("$[1].code").value("COMP0401"))
        .andExpect(jsonPath("$[1].averageScore").value(70.0));
  }

  @Test
  void topModulesCanBeFilteredBySemester() throws Exception {
    Student student = studentRepository
        .save(new Student(331L, "Module", "Filter", "modfilter", "filter@example.com"));
    Module semesterOne = moduleRepository.save(new Module("COMP0410", "Sem One", false));
    Module semesterTwo = moduleRepository.save(new Module("COMP0411", "Sem Two", false));
    registrationRepository.save(new Registration(student, semesterOne));
    registrationRepository.save(new Registration(student, semesterTwo));
    gradeRepository.save(new Grade(88, "2024/25", "Semester 1", student, semesterOne));
    gradeRepository.save(new Grade(77, "2024/25", "Semester 2", student, semesterTwo));

    mockMvc.perform(get("/analytics/top-modules")
            .with(httpBasic(TestUsers.STUDENT_USERNAME, TestUsers.STUDENT_PASSWORD))
            .param("semester", "Semester 2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].code").value("COMP0411"))
        .andExpect(jsonPath("$[0].averageScore").value(77.0));
  }
}
