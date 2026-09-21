package uk.ac.ucl.comp0010.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import uk.ac.ucl.comp0010.TestUsers;

/**
 * Verifies the permissive CORS policy exposed to the frontend.
 */
class SecurityConfigTest {

  private final SecurityConfig securityConfig = new SecurityConfig();

  @Test
  void corsConfigurationAllowsFrontendOriginsAndMethods() {
    CorsConfiguration configuration = securityConfig.corsConfigurationSource()
        .getCorsConfiguration(new MockHttpServletRequest());

    assertThat(configuration).isNotNull();
    assertThat(configuration.getAllowedOriginPatterns()).contains("*");
    assertThat(configuration.getAllowedMethods()).contains("GET", "POST", "OPTIONS");
  }

  @Test
  void userDetailsServiceExposesAllDemoUsers() {
    UserDetailsService userDetailsService = securityConfig
        .userDetailsService(securityConfig.passwordEncoder());

    UserDetails admin = userDetailsService.loadUserByUsername(TestUsers.ADMIN_USERNAME);
    UserDetails teacher = userDetailsService.loadUserByUsername(TestUsers.TEACHER_USERNAME);
    UserDetails student = userDetailsService.loadUserByUsername(TestUsers.STUDENT_USERNAME);

    assertThat(admin.getAuthorities()).extracting("authority").contains("ROLE_ADMIN");
    assertThat(teacher.getAuthorities()).extracting("authority").contains("ROLE_TEACHER");
    assertThat(student.getAuthorities()).extracting("authority").contains("ROLE_STUDENT");
  }
}
