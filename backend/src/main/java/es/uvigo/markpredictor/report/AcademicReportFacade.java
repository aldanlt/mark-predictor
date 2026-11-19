package es.uvigo.markpredictor.report;

import es.uvigo.markpredictor.model.Enrollment;
import es.uvigo.markpredictor.model.Student;
import es.uvigo.markpredictor.service.EnrollmentService;
import es.uvigo.markpredictor.service.StudentService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * PATRÓN FACADE (P7 · HU-06).
 *
 * Generar un informe requiere varios pasos y varios componentes. La fachada los esconde
 * detrás de un único método: generateReport(studentId, format). Así el controlador REST
 * no necesita conocer ninguno de esos detalles.
 */
@Service
public class AcademicReportFacade {

    private final StudentService studentService;
    private final EnrollmentService enrollmentService;
    private final ReportFactory reportFactory;
    private final ReportGenerator reportGenerator;

    public AcademicReportFacade(StudentService studentService, EnrollmentService enrollmentService,
                                ReportFactory reportFactory, ReportGenerator reportGenerator) {
        this.studentService = studentService;
        this.enrollmentService = enrollmentService;
        this.reportFactory = reportFactory;
        this.reportGenerator = reportGenerator;
    }

    public GeneratedReport generateReport(Long studentId, String format) {
        // 1. Datos del estudiante (lanza 404 si no existe)
        Student student = studentService.findById(studentId);

        // 2-4. Matrículas con asignatura, curso, horas y nota
        List<EnrollmentRecord> records = new ArrayList<>();
        for (Enrollment e : enrollmentService.history(studentId)) {
            records.add(new EnrollmentRecord(e.getSubject().getName(), e.getCourse().getName(),
                    e.getStudyHours(), e.getGrade()));
        }

        // 5-6. Objeto del formato pedido, creado por la factoría
        AcademicReport report = reportFactory.createReport(format, student.getId(), student.getName(), records);

        // 7. El adaptador se comunica con el microservicio Python
        String content = reportGenerator.generate(report);

        // 8. Se devuelve el informe listo para descargar
        return new GeneratedReport(report.getFileName(), report.getContentType(), content);
    }
}
