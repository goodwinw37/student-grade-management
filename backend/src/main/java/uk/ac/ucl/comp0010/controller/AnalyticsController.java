package uk.ac.ucl.comp0010.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ucl.comp0010.dto.ModuleAverageResponse;
import uk.ac.ucl.comp0010.dto.StudentAverageResponse;
import uk.ac.ucl.comp0010.model.Grade;
import uk.ac.ucl.comp0010.model.Module;
import uk.ac.ucl.comp0010.model.Student;
import uk.ac.ucl.comp0010.repository.GradeRepository;
import uk.ac.ucl.comp0010.repository.ModuleAverageProjection;
import uk.ac.ucl.comp0010.repository.ModuleRepository;
import uk.ac.ucl.comp0010.repository.StudentAverageProjection;
import uk.ac.ucl.comp0010.repository.StudentRepository;

/**
 * Provides analytics endpoints required by the coursework brief.
 */
@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

  private final ModuleRepository moduleRepository;
  private final StudentRepository studentRepository;
  private final GradeRepository gradeRepository;

  /**
   * Creates the controller with the required repositories for aggregation.
   */
  public AnalyticsController(ModuleRepository moduleRepository,
      StudentRepository studentRepository,
      GradeRepository gradeRepository) {
    this.moduleRepository = moduleRepository;
    this.studentRepository = studentRepository;
    this.gradeRepository = gradeRepository;
  }

  /**
   * Returns the average grade per module, including modules that have no grades yet.
   */
  @GetMapping("/module-averages")
  public List<ModuleAverageResponse> listModuleAverages() {
    Map<String, Double> averages = gradeRepository.findModuleAverageScores().stream()
        .collect(Collectors.toMap(ModuleAverageProjection::getCode,
            ModuleAverageProjection::getAverage));
    return StreamSupport.stream(moduleRepository.findAll().spliterator(), false)
      .map(module -> new ModuleAverageResponse(module.getCode(), module.getName(),
        averages.get(module.getCode())))
      .sorted(Comparator.comparing(ModuleAverageResponse::code))
      .collect(Collectors.toList());
  }

  /**
   * Returns the average grade per student, including students without grades.
   */
  @GetMapping("/student-averages")
  public List<StudentAverageResponse> listStudentAverages() {
    Map<Long, Double> averages = gradeRepository.findStudentAverageScores().stream()
        .collect(Collectors.toMap(StudentAverageProjection::getStudentId,
            StudentAverageProjection::getAverage));
    return StreamSupport.stream(studentRepository.findAll().spliterator(), false)
        .map(student -> new StudentAverageResponse(student.getId(), student.getFirstName(),
            student.getLastName(), averages.get(student.getId())))
        .sorted(Comparator.comparing(StudentAverageResponse::id))
        .collect(Collectors.toList());
  }

  /**
   * Returns the top-performing students optionally filtered by semester or academic year.
   */
  @GetMapping("/top-students")
  public List<StudentAverageResponse> topStudents(
      @RequestParam(defaultValue = "3") int limit,
      @RequestParam(value = "semester", required = false) String semester,
      @RequestParam(value = "academicYear", required = false) String academicYear) {
    int resolvedLimit = validateLimit(limit);
    Map<Long, Student> students = StreamSupport
        .stream(studentRepository.findAll().spliterator(), false)
        .collect(Collectors.toMap(Student::getId, student -> student));
    Map<Long, Double> averages = StreamSupport
        .stream(gradeRepository.findAll().spliterator(), false)
        .filter(grade -> semester == null || semester.equals(grade.getSemester()))
        .filter(grade -> academicYear == null || academicYear.equals(grade.getAcademicYear()))
        .collect(Collectors.groupingBy(grade -> grade.getStudent().getId(),
            Collectors.averagingInt(Grade::getScore)));
    return averages.entrySet().stream()
        .sorted(Map.Entry.<Long, Double>comparingByValue(
            Comparator.reverseOrder()))
        .limit(resolvedLimit)
        .map(entry -> {
          Student student = students.get(entry.getKey());
          if (student == null) {
            return null;
          }
          return new StudentAverageResponse(student.getId(), student.getFirstName(),
              student.getLastName(), entry.getValue());
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  /**
   * Returns the strongest modules using the same transient data source.
   */
  @GetMapping("/top-modules")
  public List<ModuleAverageResponse> topModules(
      @RequestParam(defaultValue = "3") int limit,
      @RequestParam(value = "semester", required = false) String semester) {
    int resolvedLimit = validateLimit(limit);
    Map<String, Module> modules = StreamSupport
        .stream(moduleRepository.findAll().spliterator(), false)
        .collect(Collectors.toMap(Module::getCode, module -> module));
    Map<String, Double> averages = StreamSupport
        .stream(gradeRepository.findAll().spliterator(), false)
        .filter(grade -> semester == null || semester.equals(grade.getSemester()))
        .collect(Collectors.groupingBy(grade -> grade.getModule().getCode(),
            Collectors.averagingInt(Grade::getScore)));
    return averages.entrySet().stream()
        .sorted(Map.Entry.<String, Double>comparingByValue(
            Comparator.reverseOrder()))
        .limit(resolvedLimit)
        .map(entry -> {
          Module module = modules.get(entry.getKey());
          if (module == null) {
            return null;
          }
          return new ModuleAverageResponse(module.getCode(), module.getName(), entry.getValue());
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private int validateLimit(int limit) {
    if (limit < 1 || limit > 50) {
      throw new IllegalArgumentException("Limit must be between 1 and 50");
    }
    return limit;
  }
}
