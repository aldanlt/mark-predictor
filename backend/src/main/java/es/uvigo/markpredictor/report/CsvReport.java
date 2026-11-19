package es.uvigo.markpredictor.report;

import java.util.List;

public class CsvReport extends AcademicReport {

    public CsvReport(Long studentId, String studentName, List<EnrollmentRecord> enrollments) {
        super(studentId, studentName, enrollments);
    }

    @Override
    public String getFormat() {
        return "csv";
    }

    @Override
    public String getContentType() {
        return "text/csv; charset=UTF-8";
    }
}
