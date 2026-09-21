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
import uk.ac.ucl.comp0010.dto.StudentRequest;
import uk.ac.ucl.comp0010.model.Student;
import uk.ac.ucl.comp0010.repository.StudentRepository;

/**
 * Allows clients to create and list student records during a demo session.
 */
@RestController
@RequestMapping("/management/students")
@Validated
public class StudentController {

  private final StudentRepository studentRepository;

  public StudentController(StudentRepository studentRepository) {
    this.studentRepository = studentRepository;
  }

  /**
   * Lists all students currently stored in the in-memory database.
   */
  @GetMapping
  public List<Student> listStudents() {
    return StreamSupport.stream(studentRepository.findAll().spliterator(), false)
        .sorted(Comparator.comparing(Student::getId))
        .collect(Collectors.toList());
  }

  /**
   * Creates a student. If no ID is supplied, the next numeric ID is generated in memory.
   */
  @PostMapping
  public ResponseEntity<Student> createStudent(@Valid @RequestBody StudentRequest request) {
    String username = normalise(request.username());
    String email = normalise(request.email());

    if (request.id() != null && studentRepository.existsById(request.id())) {
      throw new IllegalArgumentException("Student record ID %d already exists"
          .formatted(request.id()));
    }
    if (studentRepository.existsByUsername(username)) {
      throw new IllegalArgumentException("Username '%s' is already in use".formatted(username));
    }
    if (studentRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("Email '%s' is already in use".formatted(email));
    }

    Long id = resolveRecordId(request.id());
    String firstName = normalise(request.firstName());
    String lastName = normalise(request.lastName());
    Student student = new Student(id, firstName, lastName, username, email);
    Student saved = studentRepository.save(student);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  private Long resolveRecordId(Long providedId) {
    if (providedId != null) {
      return providedId;
    }
    return StreamSupport.stream(studentRepository.findAll().spliterator(), false)
        .map(Student::getId)
        .max(Comparator.naturalOrder())
        .map(id -> id + 1)
        .orElse(1L);
  }

  private String normalise(String value) {
    String trimmed = value == null ? null : value.trim();
    if (trimmed == null || trimmed.isEmpty()) {
      throw new IllegalArgumentException("Input values cannot be blank");
    }
    return trimmed;
  }
}
