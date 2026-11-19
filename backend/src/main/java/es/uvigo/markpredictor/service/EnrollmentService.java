package es.uvigo.markpredictor.service;

import es.uvigo.markpredictor.dto.EnrollmentRequest;
import es.uvigo.markpredictor.exception.BadRequestException;
import es.uvigo.markpredictor.exception.ConflictException;
import es.uvigo.markpredictor.exception.NotFoundException;
import es.uvigo.markpredictor.model.Course;
import es.uvigo.markpredictor.model.Enrollment;
import es.uvigo.markpredictor.model.Student;
import es.uvigo.markpredictor.model.Subject;
import es.uvigo.markpredictor.repository.EnrollmentRepository;
import es.uvigo.markpredictor.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** Matrículas, calificaciones e historial académico (P6 · HU-04 y P7 · HU-02). */
@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final SubjectService subjectService;
    private final CourseService courseService;

    public EnrollmentService(EnrollmentRepository enrollmentRepository, StudentRepository studentRepository,
                             SubjectService subjectService, CourseService courseService) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.subjectService = subjectService;
        this.courseService = courseService;
    }

    public Enrollment create(EnrollmentRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new NotFoundException("No existe el estudiante " + request.studentId()));
        return enroll(student, request.subjectId(), request.courseId(), request.studyHours(), request.grade());
    }

    /** Matricula a un estudiante comprobando todas las reglas. */
    public Enrollment enroll(Student student, Long subjectId, Long courseId, double studyHours, Double grade) {
        if (courseId == null) {
            throw new BadRequestException("Hay que indicar el curso académico de la matrícula");
        }
        Subject subject = subjectService.findById(subjectId);
        Course course = courseService.findById(courseId);
        checkHoursAndGrade(studyHours, grade);

        boolean taughtInCourse = course.getSubjects().stream().anyMatch(s -> s.getId().equals(subject.getId()));
        if (!taughtInCourse) {
            throw new BadRequestException(subject.getName() + " no se imparte en el curso " + course.getName());
        }
        if (enrollmentRepository.existsByStudentIdAndSubjectIdAndCourseId(student.getId(), subjectId, courseId)) {
            throw new ConflictException(student.getName() + " ya está matriculado/a en " + subject.getName()
                    + " en el curso " + course.getName());
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setSubject(subject);
        enrollment.setCourse(course);
        enrollment.setStudyHours(studyHours);
        enrollment.setGrade(grade);
        student.getEnrollments().add(enrollment);
        return enrollmentRepository.save(enrollment);
    }

    /** Cambiar las horas o poner/modificar la nota más tarde. */
    public Enrollment update(Long id, EnrollmentRequest request) {
        Enrollment enrollment = findById(id);
        checkHoursAndGrade(request.studyHours(), request.grade());
        enrollment.setStudyHours(request.studyHours());
        enrollment.setGrade(request.grade());
        return enrollmentRepository.save(enrollment);
    }

    public void delete(Long id) {
        Enrollment enrollment = findById(id);
        enrollment.getStudent().getEnrollments().remove(enrollment);
        enrollmentRepository.delete(enrollment);
    }

    public Enrollment findById(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe la matrícula " + id));
    }

    /** Historial completo de un estudiante, del curso más antiguo al más reciente. */
    public List<Enrollment> history(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new NotFoundException("No existe el estudiante " + studentId);
        }
        return enrollmentRepository.findByStudentIdOrderByCourseStartDateAsc(studentId);
    }

    private void checkHoursAndGrade(double studyHours, Double grade) {
        if (studyHours < 0) {
            throw new BadRequestException("Las horas de estudio no pueden ser negativas");
        }
        if (grade != null && (grade < 0 || grade > 10)) {
            throw new BadRequestException("La calificación debe estar entre 0 y 10");
        }
    }
}
