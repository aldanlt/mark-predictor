package es.uvigo.markpredictor.report;

import es.uvigo.markpredictor.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportFactoryTest {

    private final ReportFactory factory = new ReportFactory();
    private final List<EnrollmentRecord> enrollments =
            List.of(new EnrollmentRecord("Algoritmos", "2025/2026", 6, 6.2));

    @Test
    void creaUnInformeCsv() {
        AcademicReport report = factory.createReport("csv", 1L, "Ana", enrollments);
        assertInstanceOf(CsvReport.class, report);
        assertEquals("informe_estudiante_1.csv", report.getFileName());
    }

    @Test
    void creaUnInformeJsonSinImportarMayusculas() {
        assertInstanceOf(JsonReport.class, factory.createReport("JSON", 1L, "Ana", enrollments));
    }

    @Test
    void rechazaFormatosNoAdmitidos() {
        assertThrows(BadRequestException.class, () -> factory.createReport("pdf", 1L, "Ana", enrollments));
    }
}
