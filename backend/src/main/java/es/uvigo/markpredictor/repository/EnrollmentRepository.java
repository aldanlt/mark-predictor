package es.uvigo.markpredictor.repository;

import es.uvigo.markpredictor.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Spring Data genera las consultas a partir del nombre de cada método. */
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /** Historial de un estudiante ordenado por la fecha de inicio del curso. */
    List<Enrollment> findByStudentIdOrderByCourseStartDateAsc(Long studentId);

    boolean existsByStudentIdAndSubjectIdAndCourseId(Long studentId, Long subjectId, Long courseId);

    boolean existsByCourseId(Long courseId);
}
