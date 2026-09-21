package uk.ac.ucl.comp0010.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.ac.ucl.comp0010.model.Module;

/**
 * Repository abstraction for accessing modules via Spring Data REST.
 */
@RepositoryRestResource(collectionResourceRel = "modules", path = "modules")
public interface ModuleRepository extends CrudRepository<Module, String> {

  Optional<Module> findByName(String name);

  boolean existsByName(String name);
}
