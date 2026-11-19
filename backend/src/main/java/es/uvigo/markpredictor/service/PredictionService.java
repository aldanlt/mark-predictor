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
    private final PredictionClient predictionClient;

    public PredictionService(StudentService studentService, PredictionClient predictionClient) {
        this.studentService = studentService;
        this.predictionClient = predictionClient;
    }

    /** Predicción general del estudiante (funcionalidad original). */
    public PredictionResult predictStudent(Long studentId) {
        Student s = studentService.findById(studentId);
        return predictionClient.predict(s.getStudyHours(), s.getAttendance(), s.getPreviousGrade(), null);
    }

    /** Una predicción por asignatura, con sus horas y su dificultad (P6 · HU-02). */
    public List<EnrollmentPrediction> predictEnrollments(Long studentId) {
        Student s = studentService.findById(studentId);
        List<EnrollmentPrediction> predictions = new ArrayList<>();
        for (Enrollment e : s.getEnrollments()) {
            PredictionResult r = predictionClient.predict(e.getStudyHours(), s.getAttendance(),
                    s.getPreviousGrade(), e.getSubject().getDifficulty());
            predictions.add(new EnrollmentPrediction(e.getSubject().getName(),
                    e.getSubject().getDifficulty().name(), e.getStudyHours(),
                    r.base(), r.coefficient(), r.score()));
        }
        return predictions;
    }
}
