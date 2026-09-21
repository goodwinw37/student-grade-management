package uk.ac.ucl.comp0010.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import uk.ac.ucl.comp0010.model.Grade;
import uk.ac.ucl.comp0010.model.Module;
import uk.ac.ucl.comp0010.model.Registration;
import uk.ac.ucl.comp0010.model.Student;

/**
 * Configures Spring Data REST to expose entity identifiers over the wire.
 */
@Configuration
public class RestConfiguration implements RepositoryRestConfigurer {

  /**
   * Ensures REST responses include entity identifiers so the frontend can reference records.
   */
  @Override
  public void configureRepositoryRestConfiguration(RepositoryRestConfiguration configuration,
      CorsRegistry cors) {
    configuration.exposeIdsFor(Student.class, Module.class, Grade.class, Registration.class);
  }
}
