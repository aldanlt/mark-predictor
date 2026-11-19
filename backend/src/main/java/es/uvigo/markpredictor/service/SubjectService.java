package es.uvigo.markpredictor.service;

import es.uvigo.markpredictor.exception.BadRequestException;
import es.uvigo.markpredictor.exception.ConflictException;
import es.uvigo.markpredictor.exception.NotFoundException;
import es.uvigo.markpredictor.model.Subject;
import es.uvigo.markpredictor.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** Lógica de las asignaturas (P6 · HU-01). */
@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }

    public Subject findById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe la asignatura " + id));
    }

    public Subject create(Subject subject) {
        if (subject.getName() == null || subject.getName().isBlank()) {
            throw new BadRequestException("El nombre de la asignatura es obligatorio");
        }
        if (subject.getDifficulty() == null) {
            throw new BadRequestException("La dificultad es obligatoria: FACIL, MEDIA o DIFICIL");
        }
        subject.setName(subject.getName().trim());
        if (subjectRepository.existsByName(subject.getName())) {
            throw new ConflictException("Ya existe una asignatura llamada " + subject.getName());
        }
        return subjectRepository.save(subject);
    }
}
