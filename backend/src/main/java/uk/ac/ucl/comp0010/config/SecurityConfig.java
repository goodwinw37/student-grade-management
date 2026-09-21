package uk.ac.ucl.comp0010.config;

import static org.springframework.security.config.Customizer.withDefaults;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security configuration that exposes a permissive CORS policy for the frontend.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /** Role with full administrative privileges. */
  public static final String ADMIN = "ADMIN";

  /** Role for teachers, permitted to add grades. */
  public static final String TEACHER = "TEACHER";

  /** Role for students, permitted to view data. */
  public static final String STUDENT = "STUDENT";

  /**
   * Builds the Spring Security filter chain with CSRF disabled for the API endpoints.
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/grades/addGrade")
                .hasAnyRole(ADMIN, TEACHER)
            .requestMatchers(HttpMethod.GET, "/grades/my")
                .hasAnyRole(ADMIN, STUDENT)
            .requestMatchers(HttpMethod.POST, "/management/**").hasRole(ADMIN)
            .requestMatchers(HttpMethod.PUT, "/management/**").hasRole(ADMIN)
            .requestMatchers(HttpMethod.PATCH, "/management/**").hasRole(ADMIN)
            .requestMatchers(HttpMethod.DELETE, "/management/**").hasRole(ADMIN)
            .requestMatchers(HttpMethod.GET, "/management/**")
                .hasAnyRole(ADMIN, TEACHER)
            .requestMatchers(HttpMethod.POST, "/students/**", "/modules/**",
                "/registrations/**", "/grades/**").hasRole(ADMIN)
            .requestMatchers(HttpMethod.PUT, "/students/**", "/modules/**",
                "/registrations/**", "/grades/**").hasRole(ADMIN)
            .requestMatchers(HttpMethod.PATCH, "/students/**", "/modules/**",
                "/registrations/**", "/grades/**").hasRole(ADMIN)
            .requestMatchers(HttpMethod.DELETE, "/grades/**").hasAnyRole(ADMIN, TEACHER)
            .requestMatchers(HttpMethod.DELETE, "/students/**", "/modules/**",
                "/registrations/**").hasRole(ADMIN)
            .requestMatchers(HttpMethod.GET, "/grades/**", "/analytics/**", "/students/**",
                "/modules/**", "/registrations/**", "/swagger-ui/**", "/v3/api-docs/**")
                .hasAnyRole(ADMIN, TEACHER, STUDENT)
            .anyRequest().hasRole(ADMIN))
        .httpBasic(withDefaults())
        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint((request, response, exception) ->
                writeJsonError(response, HttpStatus.UNAUTHORIZED, "Authentication required"))
            .accessDeniedHandler((request, response, exception) ->
                writeJsonError(response, HttpStatus.FORBIDDEN, "Access denied")));
    return http.build();
  }

  /**
   * Provides a CorsConfigurationSource that allows the Vite dev server to call the backend.
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(List.of("*"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowedMethods(
        List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    configuration.setAllowCredentials(false);
    configuration.applyPermitDefaultValues();
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  /**
   * Exposes demo credentials for the admin/teacher/student roles using in-memory accounts.
   */
  @Bean
  public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    UserDetails adminUser = User.withUsername("admin")
        .password(passwordEncoder.encode(adminPassword))
        .roles(ADMIN)
        .build();
    UserDetails teacherUser = User.withUsername("teacher")
        .password(passwordEncoder.encode(teacherPassword))
        .roles(TEACHER)
        .build();
    UserDetails ada = User.withUsername("ada")
        .password(passwordEncoder.encode(studentPassword))
        .roles(STUDENT)
        .build();
    UserDetails alan = User.withUsername("alan")
        .password(passwordEncoder.encode(studentPassword))
        .roles(STUDENT)
        .build();
    UserDetails grace = User.withUsername("grace")
        .password(passwordEncoder.encode(studentPassword))
        .roles(STUDENT)
        .build();
    return new InMemoryUserDetailsManager(adminUser, teacherUser, ada, alan, grace);
  }

  @Value("${app.demo.admin-password:dev-admin}")
    private String adminPassword = "dev-admin";

  @Value("${app.demo.teacher-password:dev-teacher}")
    private String teacherPassword = "dev-teacher";

  @Value("${app.demo.student-password:dev-student}")
    private String studentPassword = "dev-student";

  /**
   * Leverages Spring Security's delegating encoder so passwords are always hashed.
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  private void writeJsonError(HttpServletResponse response, HttpStatus status, String message)
      throws IOException {
    response.setStatus(status.value());
    response.setContentType("application/json");
    response.getWriter().write("{\"message\":\"" + message + "\"}");
  }
}