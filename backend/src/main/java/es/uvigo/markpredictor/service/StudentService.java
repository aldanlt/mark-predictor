package es.uvigo.markpredictor.service;

import es.uvigo.markpredictor.dto.StudentRequest;
import es.uvigo.markpredictor.exception.BadRequestException;
import es.uvigo.markpredictor.exception.NotFoundException;
import es.uvigo.markpredictor.model.Student;
import es.uvigo.markpredictor.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Registro y consulta de estudiantes. */
@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final EnrollmentService enrollmentService;

    public StudentService(StudentRepository studentRepository, EnrollmentService enrollmentService) {
        this.studentRepository = studentRepository;
        this.enrollmentService = enrollmentService;
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el estudiante " + id));
    }

    /**
     * Guarda el estudiante y lo matricula en las asignaturas marcadas (P6 · HU-04).
     * @Transactional: si falla una matrícula, no se guarda nada (ni siquiera el estudiante).
     */
    @Transactional
    public Student create(StudentRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("El nombre es obligatorio");
        }
        if (request.studyHours() < 0) {
            throw new BadRequestException("Las horas de estudio no pueden ser negativas");
        }
        if (request.attendance() < 0 || request.attendance() > 100) {
            throw new BadRequestException("La asistencia debe estar entre 0 y 100");
        }
        if (request.previousGrade() < 0 || request.previousGrade() > 10) {
            throw new BadRequestException("La nota previa debe estar entre 0 y 10");
        }

        Student student = new Student();
        student.setName(request.name().trim());
        student.setStudyHours(request.studyHours());
        student.setAttendance(request.attendance());
        student.setPreviousGrade(request.previousGrade());
        student = studentRepository.save(student);

        if (request.subjects() != null) {
            for (StudentRequest.SelectedSubject selected : request.subjects()) {
                enrollmentService.enroll(student, selected.subjectId(), request.courseId(),
                        selected.studyHours(), null);
            }
        }
        return student;
    }
}
