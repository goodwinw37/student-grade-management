package uk.ac.ucl.comp0010.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.ac.ucl.comp0010.model.Student;

/**
 * Repository abstraction for student records exposed via Spring Data REST.
 */
@RepositoryRestResource(collectionResourceRel = "students", path = "students")
public interface StudentRepository extends CrudRepository<Student, Long> {

  Optional<Student> findByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);
}
