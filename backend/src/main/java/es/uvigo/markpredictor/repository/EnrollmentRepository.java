package es.uvigo.markpredictor.repository;

import es.uvigo.markpredictor.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
}
