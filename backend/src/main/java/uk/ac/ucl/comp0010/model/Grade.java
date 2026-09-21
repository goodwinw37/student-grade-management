package uk.ac.ucl.comp0010.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;

/**
 * Represents a single assessment score awarded to a student for a module.
 */
@Entity
@Table(name = "grade",
    uniqueConstraints = @UniqueConstraint(name = "uk_grade_student_module_year_semester",
        columnNames = {"student_id", "module_code", "academic_year", "semester"}))
public class Grade {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Integer score;

  @Column(name = "academic_year", length = 9, nullable = false)
  private String academicYear;

  @Column(length = 15, nullable = false)
  private String semester;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "module_code", nullable = false)
  private Module module;

  /**
   * Creates an empty grade; required by JPA.
   */
  public Grade() {
    // Default constructor for JPA
  }

  /**
   * Creates a grade with the provided details.
   *
   * @param score numeric mark between 0 and 100
   * @param student student who received the mark
   * @param module module associated with the mark
   */
  public Grade(Integer score, String academicYear, String semester, Student student,
      Module module) {
    this.score = score;
    this.academicYear = academicYear;
    this.semester = semester;
    this.student = student;
    this.module = module;
  }

  public Long getId() {
    return id;
  }

  public Integer getScore() {
    return score;
  }

  public void setScore(Integer score) {
    this.score = score;
  }

  public Student getStudent() {
    return student;
  }

  public void setStudent(Student student) {
    this.student = student;
  }

  public String getAcademicYear() {
    return academicYear;
  }

  public void setAcademicYear(String academicYear) {
    this.academicYear = academicYear;
  }

  public String getSemester() {
    return semester;
  }

  public void setSemester(String semester) {
    this.semester = semester;
  }

  public Module getModule() {
    return module;
  }

  public void setModule(Module module) {
    this.module = module;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Grade grade = (Grade) o;
    return Objects.equals(id, grade.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
