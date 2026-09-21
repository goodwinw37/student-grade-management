package uk.ac.ucl.comp0010.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.ac.ucl.comp0010.model.Module;
import uk.ac.ucl.comp0010.model.Registration;
import uk.ac.ucl.comp0010.model.Student;

/**
 * Repository facade for registration records, ensuring REST exposure.
 */
@RepositoryRestResource(collectionResourceRel = "registrations", path = "registrations")
public interface RegistrationRepository extends CrudRepository<Registration, Long> {

  Optional<Registration> findByStudentAndModule(Student student, Module module);
}
