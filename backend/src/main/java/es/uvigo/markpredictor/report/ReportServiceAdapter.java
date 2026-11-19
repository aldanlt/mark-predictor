package es.uvigo.markpredictor.report;

import es.uvigo.markpredictor.client.ReportServiceClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PATRÓN ADAPTER (P7 · HU-05).
 *
 * La aplicación quiere un ReportGenerator, pero lo que tenemos es ReportServiceClient,
 * que habla "en Python". Este adaptador implementa ReportGenerator y, por dentro,
 * traduce los datos y llama al cliente:
 *
 *   Java                         Python
 *   studentId / studentName  ->  student: {id, name}
 *   enrollments              ->  records
 *   subject                  ->  name
 *   course                   ->  academic_year
 *   studyHours               ->  study_hours
 *   grade                    ->  score
 */
@Component
public class ReportServiceAdapter implements ReportGenerator {

    private final ReportServiceClient client;

    public ReportServiceAdapter(ReportServiceClient client) {
        this.client = client;
    }

    @Override
    public String generate(AcademicReport report) {
        return client.createReport(report.getFormat(), toPythonFormat(report));
    }

    /** Traduce el informe de Java al formato que espera el microservicio Python. */
    public Map<String, Object> toPythonFormat(AcademicReport report) {
        Map<String, Object> student = new HashMap<>();
        student.put("id", report.getStudentId());
        student.put("name", report.getStudentName());

        List<Map<String, Object>> records = new ArrayList<>();
        for (EnrollmentRecord enrollment : report.getEnrollments()) {
            Map<String, Object> record = new HashMap<>();
            record.put("name", enrollment.subject());
            record.put("academic_year", enrollment.course());
            record.put("study_hours", enrollment.studyHours());
            record.put("score", enrollment.grade());
            records.add(record);
        }

        Map<String, Object> pythonData = new HashMap<>();
        pythonData.put("student", student);
        pythonData.put("records", records);
        return pythonData;
    }
}
