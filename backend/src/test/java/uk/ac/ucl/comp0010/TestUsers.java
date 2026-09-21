package uk.ac.ucl.comp0010;

/**
 * Shared test credentials that mirror the in-memory demo accounts exposed by Spring Security.
 */
public final class TestUsers {

  public static final String ADMIN_USERNAME = "admin";
  public static final String ADMIN_PASSWORD =
      System.getenv().getOrDefault("DEMO_ADMIN_PASSWORD", "dev-admin");

  public static final String TEACHER_USERNAME = "teacher";
  public static final String TEACHER_PASSWORD =
      System.getenv().getOrDefault("DEMO_TEACHER_PASSWORD", "dev-teacher");

  public static final String STUDENT_USERNAME = "ada";
  public static final String STUDENT_PASSWORD =
      System.getenv().getOrDefault("DEMO_STUDENT_PASSWORD", "dev-student");

  private TestUsers() {
    // Utility class
  }
}
