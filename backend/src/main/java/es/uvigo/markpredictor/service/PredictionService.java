package es.uvigo.markpredictor.service;

import es.uvigo.markpredictor.client.PredictionClient;
import es.uvigo.markpredictor.client.PredictionResult;
import es.uvigo.markpredictor.dto.EnrollmentPrediction;
import es.uvigo.markpredictor.model.Enrollment;
import es.uvigo.markpredictor.model.Student;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Prepara los datos para las predicciones. El cálculo lo hace siempre el microservicio Python.
 */
@Service
public class PredictionService {

    private final StudentService studentService;
    private final EnrollmentService enrollmentService;
    private final PredictionClient predictionClient;

    public PredictionService(StudentService studentService, EnrollmentService enrollmentService,
                             PredictionClient predictionClient) {
        this.studentService = studentService;
        this.enrollmentService = enrollmentService;
        this.predictionClient = predictionClient;
    }

    /** Predicción general del estudiante (funcionalidad original). */
    public PredictionResult predictStudent(Long studentId) {
        Student s = studentService.findById(studentId);
        return predictionClient.predict(s.getStudyHours(), s.getAttendance(), s.getPreviousGrade(), null);
    }

    /** Una predicción por matrícula, con las horas de esa matrícula y la dificultad de la asignatura (P6 · HU-02). */
    public List<EnrollmentPrediction> predictEnrollments(Long studentId) {
        Student s = studentService.findById(studentId);
        List<EnrollmentPrediction> predictions = new ArrayList<>();
        for (Enrollment e : enrollmentService.history(studentId)) {
            PredictionResult r = predictionClient.predict(e.getStudyHours(), s.getAttendance(),
                    s.getPreviousGrade(), e.getSubject().getDifficulty());
            predictions.add(new EnrollmentPrediction(e.getId(), e.getSubject().getName(),
                    e.getSubject().getDifficulty().name(), e.getCourse().getName(), e.getStudyHours(),
                    e.getGrade(), r.base(), r.coefficient(), r.score()));
        }
        return predictions;
    }
}
