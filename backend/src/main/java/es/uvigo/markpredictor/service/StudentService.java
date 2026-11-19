package es.uvigo.markpredictor.service;

import es.uvigo.markpredictor.dto.StudentRequest;
import es.uvigo.markpredictor.exception.BadRequestException;
import es.uvigo.markpredictor.exception.NotFoundException;
import es.uvigo.markpredictor.model.Enrollment;
import es.uvigo.markpredictor.model.Student;
import es.uvigo.markpredictor.model.Subject;
import es.uvigo.markpredictor.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Registro y consulta de estudiantes con sus asignaturas (P6 · HU-04). */
@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final SubjectService subjectService;

    public StudentService(StudentRepository studentRepository, SubjectService subjectService) {
        this.studentRepository = studentRepository;
        this.subjectService = subjectService;
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el estudiante " + id));
    }

    /**
     * Guarda el estudiante y lo matricula en las asignaturas marcadas.
     * @Transactional: si algo falla, no se guarda nada.
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

        Set<Long> alreadySelected = new HashSet<>();
        if (request.subjects() != null) {
            for (StudentRequest.SelectedSubject selected : request.subjects()) {
                if (selected.studyHours() < 0) {
                    throw new BadRequestException("Las horas de estudio no pueden ser negativas");
                }
                if (!alreadySelected.add(selected.subjectId())) {
                    throw new BadRequestException("Una asignatura está seleccionada dos veces");
                }
                Subject subject = subjectService.findById(selected.subjectId());

                Enrollment enrollment = new Enrollment();
                enrollment.setStudent(student);
                enrollment.setSubject(subject);
                enrollment.setStudyHours(selected.studyHours());
                student.getEnrollments().add(enrollment);
            }
        }
        // Al guardar el estudiante se guardan también sus matrículas (cascade = ALL)
        return studentRepository.save(student);
    }
}
