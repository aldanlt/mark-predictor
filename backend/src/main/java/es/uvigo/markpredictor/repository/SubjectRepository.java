package es.uvigo.markpredictor.repository;

import es.uvigo.markpredictor.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    boolean existsByName(String name);
}
