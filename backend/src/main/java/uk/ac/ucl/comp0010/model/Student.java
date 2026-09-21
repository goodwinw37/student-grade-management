package uk.ac.ucl.comp0010.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a student that can enrol on modules and receive grades.
 */
@Entity
@Table(name = "student")
public class Student {

  @Id
  private Long id;

  @Column(length = 30, nullable = false)
  private String firstName;

  @Column(length = 30, nullable = false)
  private String lastName;

  @Column(name = "username", length = 30, nullable = false, unique = true)
  private String username;

  @Column(length = 50, nullable = false, unique = true)
  private String email;

  @JsonIgnore
  @OneToMany(mappedBy = "student")
  private Set<Registration> registrations = new LinkedHashSet<>();

  @JsonIgnore
  @OneToMany(mappedBy = "student")
  private Set<Grade> grades = new LinkedHashSet<>();

  /**
   * Creates an empty student; required by JPA.
   */
  public Student() {
    // Default constructor for JPA
  }

  /**
   * Creates a student record with identifying fields populated.
   *
   * @param id primary key from the seed dataset
   * @param firstName student's given name
   * @param lastName student's family name
  * @param username externally visible identifier shown in the UI
   * @param email contact email address
   */
  public Student(Long id, String firstName, String lastName, String username, String email) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.username = username;
    this.email = email;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Set<Registration> getRegistrations() {
    return registrations;
  }

  public Set<Grade> getGrades() {
    return grades;
  }

  /**
   * Computes the average score across the student's grades, returning null when unavailable.
   *
   * @return the average score or null if no grades exist
   */
  public Float computeAverage() {
    if (grades.isEmpty()) {
      return null;
    }
    double average = grades.stream()
        .mapToInt(Grade::getScore)
        .average()
        .orElse(Double.NaN);
    if (Double.isNaN(average)) {
      return null;
    }
    return (float) average;
  }

  /**
   * Adds a grade to the student while preventing duplicate module entries.
   *
   * @param grade the new grade to associate with the student
   */
  public void addGrade(Grade grade) {
    Objects.requireNonNull(grade, "grade must be provided");
    Module module = Objects.requireNonNull(grade.getModule(), "grade must reference a module");
    if (getGrade(module) != null) {
      throw new IllegalStateException(
          "Grade already recorded for module %s".formatted(module.getCode()));
    }
    grade.setStudent(this);
    grades.add(grade);
  }

  /**
   * Retrieves the grade for the requested module, if any.
   *
   * @param module module to query
   * @return the grade or null if absent
   */
  public Grade getGrade(Module module) {
    Objects.requireNonNull(module, "module must be provided");
    return grades.stream()
        .filter(existing -> module.equals(existing.getModule()))
        .findFirst()
        .orElse(null);
  }

  /**
   * Registers the student to the supplied module, guarding against duplicates.
   *
   * @param module module to enrol on
   */
  public void registerModule(Module module) {
    Objects.requireNonNull(module, "module must be provided");
    boolean alreadyRegistered = registrations.stream()
        .anyMatch(existing -> module.equals(existing.getModule()));
    if (alreadyRegistered) {
      throw new IllegalStateException(
          "Student %d already registered for %s".formatted(id, module.getCode()));
    }
    registrations.add(new Registration(this, module));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Student student = (Student) o;
    return Objects.equals(id, student.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
