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
 * Represents a module as loaded from the synthetic seed dataset.
 */
@Entity
@Table(name = "module")
public class Module {

  @Id
  @Column(length = 10)
  private String code;

  @Column(length = 100, nullable = false)
  private String name;

  @Column(nullable = false)
  private boolean mnc;

  @JsonIgnore
  @OneToMany(mappedBy = "module")
  private Set<Registration> registrations = new LinkedHashSet<>();

  @JsonIgnore
  @OneToMany(mappedBy = "module")
  private Set<Grade> grades = new LinkedHashSet<>();

  /**
   * Creates an empty module; required by JPA.
   */
  public Module() {
    // Default constructor for JPA
  }

  /**
   * Creates a fully initialised module instance.
   *
   * @param code module code used in student registrations
   * @param name descriptive module title
   * @param mnc whether the module is a minor compulsory component
   */
  public Module(String code, String name, boolean mnc) {
    this.code = code;
    this.name = name;
    this.mnc = mnc;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isMnc() {
    return mnc;
  }

  public void setMnc(boolean mnc) {
    this.mnc = mnc;
  }

  public Set<Registration> getRegistrations() {
    return registrations;
  }

  public Set<Grade> getGrades() {
    return grades;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Module module = (Module) o;
    return Objects.equals(code, module.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code);
  }
}
