package es.uvigo.markpredictor.report;

import java.util.List;

public class JsonReport extends AcademicReport {

    public JsonReport(Long studentId, String studentName, List<EnrollmentRecord> enrollments) {
        super(studentId, studentName, enrollments);
    }

    @Override
    public String getFormat() {
        return "json";
    }

    @Override
    public String getContentType() {
        return "application/json; charset=UTF-8";
    }
}
