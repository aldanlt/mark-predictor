package es.uvigo.markpredictor.repository;

import es.uvigo.markpredictor.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
