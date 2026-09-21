package uk.ac.ucl.comp0010.controller;

import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ucl.comp0010.dto.RegistrationRequest;
import uk.ac.ucl.comp0010.dto.RegistrationResponse;
import uk.ac.ucl.comp0010.model.Module;
import uk.ac.ucl.comp0010.model.Registration;
import uk.ac.ucl.comp0010.model.Student;
import uk.ac.ucl.comp0010.repository.ModuleRepository;
import uk.ac.ucl.comp0010.repository.RegistrationRepository;
import uk.ac.ucl.comp0010.repository.StudentRepository;

/**
 * Endpoints for creating and listing registrations without editing SQL seed files.
 */
@RestController
@RequestMapping("/management/registrations")
@Validated
public class RegistrationController {

  private final RegistrationRepository registrationRepository;
  private final StudentRepository studentRepository;
  private final ModuleRepository moduleRepository;

  /**
   * Builds the controller with the repositories used for validation and persistence.
   */
  public RegistrationController(RegistrationRepository registrationRepository,
      StudentRepository studentRepository,
      ModuleRepository moduleRepository) {
    this.registrationRepository = registrationRepository;
    this.studentRepository = studentRepository;
    this.moduleRepository = moduleRepository;
  }

  /**
   * Lists current registrations ordered by ID for easier inspection.
   */
  @GetMapping
  public List<RegistrationResponse> listRegistrations() {
    return StreamSupport.stream(registrationRepository.findAll().spliterator(), false)
        .sorted(Comparator.comparing(Registration::getId))
        .map(RegistrationResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Registers a student to a module after validating all prerequisites.
   */
  @PostMapping
  public ResponseEntity<RegistrationResponse> register(
      @Valid @RequestBody RegistrationRequest request) {
    Student student = studentRepository.findById(request.studentId())
        .orElseThrow(() -> new IllegalArgumentException(
            "Student not found: %d".formatted(request.studentId())));
    String moduleCode = request.moduleCode().trim().toUpperCase();
    Module module = moduleRepository.findById(moduleCode)
        .orElseThrow(() -> new IllegalArgumentException(
            "Module not found: %s".formatted(moduleCode)));
    registrationRepository.findByStudentAndModule(student, module).ifPresent(existing -> {
      throw new IllegalStateException("Student %d already registered for %s"
          .formatted(student.getId(), module.getCode()));
    });

    Registration registration = new Registration(student, module);
    Registration saved = registrationRepository.save(registration);
    return ResponseEntity.status(HttpStatus.CREATED).body(RegistrationResponse.fromEntity(saved));
  }
}
