package es.uvigo.markpredictor.report;

import es.uvigo.markpredictor.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PATRÓN FACTORY (P7 · HU-04).
 *
 * Recibe el formato como texto ("csv" o "json") y devuelve el objeto adecuado.
 * Así, quien pide un informe no necesita saber que existen CsvReport o JsonReport.
 */
@Component
public class ReportFactory {

    public AcademicReport createReport(String format, Long studentId, String studentName,
                                       List<EnrollmentRecord> enrollments) {
        if (format == null) {
            throw new BadRequestException("Hay que indicar el formato del informe (csv o json)");
        }
        switch (format.toLowerCase()) {
            case "csv":
                return new CsvReport(studentId, studentName, enrollments);
            case "json":
                return new JsonReport(studentId, studentName, enrollments);
            default:
                throw new BadRequestException("Formato '" + format + "' no admitido. Usa csv o json");
        }
    }
}
