package uk.ac.ucl.comp0010.controller;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ucl.comp0010.dto.GradeResponse;
import uk.ac.ucl.comp0010.exception.NoGradeAvailableException;
import uk.ac.ucl.comp0010.exception.NoRegistrationException;
import uk.ac.ucl.comp0010.model.Grade;
import uk.ac.ucl.comp0010.model.Module;
import uk.ac.ucl.comp0010.model.Student;
import uk.ac.ucl.comp0010.repository.GradeRepository;
import uk.ac.ucl.comp0010.repository.ModuleRepository;
import uk.ac.ucl.comp0010.repository.RegistrationRepository;
import uk.ac.ucl.comp0010.repository.StudentRepository;

/**
 * REST controller for creating and retrieving grade records.
 */
@RestController
@RequestMapping("/grades")
public class GradeController {

  private static final Pattern ACADEMIC_YEAR_PATTERN = Pattern.compile("\\d{4}/\\d{2}");
  private static final List<String> ALLOWED_SEMESTERS = List.of("Semester 1", "Semester 2",
      "Summer");

  private final StudentRepository studentRepository;
  private final ModuleRepository moduleRepository;
  private final GradeRepository gradeRepository;
  private final RegistrationRepository registrationRepository;

  /**
   * Creates a controller with the required repositories.
   */
  public GradeController(StudentRepository studentRepository,
      ModuleRepository moduleRepository,
      GradeRepository gradeRepository,
      RegistrationRepository registrationRepository) {
    this.studentRepository = studentRepository;
    this.moduleRepository = moduleRepository;
    this.gradeRepository = gradeRepository;
    this.registrationRepository = registrationRepository;
  }

  /**
   * Handles POST /grades/addGrade requests by validating the student/module and persisting the
   * grade.
   */
  @PostMapping(value = "/addGrade")
  public ResponseEntity<Grade> addGrade(@RequestBody Map<String, String> params) {
    Long studentId = parseLong(params, "student_id");
    String moduleCode = require(params, "module_code");
    Student student = studentRepository.findById(studentId)
        .orElseThrow(() -> new NoGradeAvailableException("Student not found: " + studentId));
    Module module = moduleRepository.findById(moduleCode)
        .orElseThrow(() -> new NoGradeAvailableException("Module not found: " + moduleCode));
    registrationRepository.findByStudentAndModule(student, module)
        .orElseThrow(() -> new NoRegistrationException(
            "Student %d is not registered for module %s".formatted(studentId, moduleCode)));
    String academicYear = parseAcademicYear(params);
    String semester = parseSemester(params);
    gradeRepository.findByStudent_IdAndModule_CodeAndAcademicYearAndSemester(studentId,
        moduleCode, academicYear, semester)
        .ifPresent(existing -> {
          throw new IllegalStateException(
              "Grade already exists for student %d in module %s (%s, %s)".formatted(
                  studentId, moduleCode, academicYear, semester));
        });
    Integer score = parseInteger(params, "score");
    Grade grade = new Grade();
    grade.setStudent(student);
    grade.setModule(module);
    grade.setAcademicYear(academicYear);
    grade.setSemester(semester);
    grade.setScore(score);
    Grade savedGrade = gradeRepository.save(grade);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedGrade);
  }

  /**
   * Deletes a grade if it exists.
   */
  @DeleteMapping("/{gradeId}")
  public ResponseEntity<Void> deleteGrade(@PathVariable Long gradeId) {
    Grade grade = gradeRepository.findById(gradeId)
        .orElseThrow(() -> new NoGradeAvailableException("Grade not found: " + gradeId));
    gradeRepository.delete(grade);
    return ResponseEntity.noContent().build();
  }

  /**
   * Returns the list of grades enriched with student/module summaries.
   */
  @GetMapping
  public List<GradeResponse> listGrades() {
    return StreamSupport.stream(gradeRepository.findAll().spliterator(), false)
        .map(GradeResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Lists only the grades that belong to the currently authenticated student.
   */
  @GetMapping("/my")
  public List<GradeResponse> listMyGrades(Authentication authentication) {
    String username = authentication.getName();
    Student student = studentRepository.findByUsername(username)
        .orElseThrow(() -> new NoGradeAvailableException(
            "No student profile found for user " + username));
    return gradeRepository.findByStudent_Id(student.getId()).stream()
        .map(GradeResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Ensures a request parameter exists and is not blank.
   */
  private String require(Map<String, String> params, String key) {
    String value = params.get(key);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Missing parameter: " + key);
    }
    return value;
  }

  /**
   * Parses a long value from the incoming request data.
   */
  private Long parseLong(Map<String, String> params, String key) {
    try {
      return Long.parseLong(require(params, key));
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("Invalid numeric value for " + key, ex);
    }
  }

  /**
   * Parses an integer value from the incoming request data.
   */
  private Integer parseInteger(Map<String, String> params, String key) {
    try {
      return Integer.parseInt(require(params, key));
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("Invalid numeric value for " + key, ex);
    }
  }

  /**
   * Validates that the provided academic year matches the expected format.
   */
  private String parseAcademicYear(Map<String, String> params) {
    String academicYear = require(params, "academic_year");
    if (!ACADEMIC_YEAR_PATTERN.matcher(academicYear).matches()) {
      throw new IllegalArgumentException("Academic year must match pattern YYYY/YY");
    }
    return academicYear;
  }

  /**
   * Ensures the semester is within the supported values.
   */
  private String parseSemester(Map<String, String> params) {
    String semester = require(params, "semester");
    if (!ALLOWED_SEMESTERS.contains(semester)) {
      throw new IllegalArgumentException(
          "Semester must be one of " + String.join(", ", ALLOWED_SEMESTERS));
    }
    return semester;
  }
}
