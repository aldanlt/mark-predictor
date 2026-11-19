package es.uvigo.markpredictor.service;

import es.uvigo.markpredictor.dto.CourseRequest;
import es.uvigo.markpredictor.exception.BadRequestException;
import es.uvigo.markpredictor.exception.ConflictException;
import es.uvigo.markpredictor.exception.NotFoundException;
import es.uvigo.markpredictor.model.Course;
import es.uvigo.markpredictor.model.Subject;
import es.uvigo.markpredictor.repository.CourseRepository;
import es.uvigo.markpredictor.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Lógica de los cursos académicos: registrar, consultar, modificar y eliminar (P7 · HU-01). */
@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SubjectService subjectService;

    public CourseService(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository,
                         SubjectService subjectService) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.subjectService = subjectService;
    }

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el curso académico " + id));
    }

    public Course create(CourseRequest request) {
        return save(new Course(), request);
    }

    public Course update(Long id, CourseRequest request) {
        return save(findById(id), request);
    }

    public void delete(Long id) {
        Course course = findById(id);
        if (enrollmentRepository.existsByCourseId(id)) {
            throw new ConflictException("No se puede eliminar el curso " + course.getName()
                    + " porque tiene matrículas asociadas");
        }
        courseRepository.delete(course);
    }

    private Course save(Course course, CourseRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("El nombre del curso es obligatorio (p. ej. 2025/2026)");
        }
        if (request.startDate() == null || request.endDate() == null) {
            throw new BadRequestException("Las fechas de inicio y finalización son obligatorias");
        }
        if (!request.endDate().isAfter(request.startDate())) {
            throw new BadRequestException("La fecha de finalización debe ser posterior a la de inicio");
        }

        // No se permiten dos cursos con el mismo nombre
        String name = request.name().trim();
        Optional<Course> sameName = courseRepository.findByName(name);
        if (sameName.isPresent() && !sameName.get().getId().equals(course.getId())) {
            throw new ConflictException("Ya existe el curso académico " + name);
        }

        List<Subject> subjects = new ArrayList<>();
        if (request.subjectIds() != null) {
            for (Long subjectId : request.subjectIds()) {
                subjects.add(subjectService.findById(subjectId));
            }
        }

        course.setName(name);
        course.setStartDate(request.startDate());
        course.setEndDate(request.endDate());
        course.setSubjects(subjects);
        return courseRepository.save(course);
    }
}
