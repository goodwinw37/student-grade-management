package uk.ac.ucl.comp0010.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.ac.ucl.comp0010.model.Grade;

/**
 * Repository exposure for grade entities, also exported over Spring Data REST.
 */
@RepositoryRestResource(collectionResourceRel = "grades", path = "grades")
public interface GradeRepository extends CrudRepository<Grade, Long> {

  List<Grade> findByStudent_Id(Long studentId);

  List<Grade> findByModule_Code(String moduleCode);

  Optional<Grade> findByStudent_IdAndModule_CodeAndAcademicYearAndSemester(Long studentId,
      String moduleCode, String academicYear, String semester);

  @Query("SELECT g.module.code AS code, AVG(g.score) AS average "
      + "FROM Grade g GROUP BY g.module.code")
  List<ModuleAverageProjection> findModuleAverageScores();

  @Query("SELECT g.student.id AS studentId, AVG(g.score) AS average "
      + "FROM Grade g GROUP BY g.student.id")
  List<StudentAverageProjection> findStudentAverageScores();
}
