package uk.ac.ucl.comp0010.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import uk.ac.ucl.comp0010.model.Grade;
import uk.ac.ucl.comp0010.model.Module;
import uk.ac.ucl.comp0010.model.Registration;
import uk.ac.ucl.comp0010.model.Student;

/**
 * Verifies the customisation of Spring Data REST configuration.
 */
@SpringBootTest
class RestConfigurationTest {

  @Autowired
  private RepositoryRestConfiguration configuration;

  @Test
  void exposesIdentifiersForAllEntities() {
    assertThat(configuration.isIdExposedFor(Student.class)).isTrue();
    assertThat(configuration.isIdExposedFor(Module.class)).isTrue();
    assertThat(configuration.isIdExposedFor(Grade.class)).isTrue();
    assertThat(configuration.isIdExposedFor(Registration.class)).isTrue();
  }
}
