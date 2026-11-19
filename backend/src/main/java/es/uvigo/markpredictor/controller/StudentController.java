package es.uvigo.markpredictor.controller;

import es.uvigo.markpredictor.client.PredictionResult;
import es.uvigo.markpredictor.dto.EnrollmentPrediction;
import es.uvigo.markpredictor.dto.StudentRequest;
import es.uvigo.markpredictor.model.Student;
import es.uvigo.markpredictor.service.PredictionService;
import es.uvigo.markpredictor.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final PredictionService predictionService;

    public StudentController(StudentService studentService, PredictionService predictionService) {
        this.studentService = studentService;
        this.predictionService = predictionService;
    }

    @GetMapping
    public List<Student> findAll() {
        return studentService.findAll();
    }

    @GetMapping("/{id}")
    public Student findById(@PathVariable Long id) {
        return studentService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Student create(@RequestBody StudentRequest request) {
        return studentService.create(request);
    }

    /** Predicción general. */
    @GetMapping("/{id}/prediction")
    public PredictionResult prediction(@PathVariable Long id) {
        return predictionService.predictStudent(id);
    }

    /** Predicción de cada asignatura cursada (P6 · HU-02). */
    @GetMapping("/{id}/predictions")
    public List<EnrollmentPrediction> predictions(@PathVariable Long id) {
        return predictionService.predictEnrollments(id);
    }
}
