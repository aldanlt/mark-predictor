package es.uvigo.markpredictor.report;

import es.uvigo.markpredictor.client.ReportServiceClient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportServiceAdapterTest {

    /** Cliente falso: en vez de llamar a Python, guarda lo que recibe. */
    static class FakeClient extends ReportServiceClient {
        String format;
        Map<String, Object> data;

        @Override
        public String createReport(String format, Map<String, Object> pythonData) {
            this.format = format;
            this.data = pythonData;
            return "contenido generado";
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void traduceLosCamposDeJavaALosDePython() {
        FakeClient client = new FakeClient();
        ReportServiceAdapter adapter = new ReportServiceAdapter(client);
        AcademicReport report = new CsvReport(1L, "Ana",
                List.of(new EnrollmentRecord("Algoritmos", "2024/2025", 3, 3.5)));

        String result = adapter.generate(report);

        assertEquals("contenido generado", result);
        assertEquals("csv", client.format);
        Map<String, Object> student = (Map<String, Object>) client.data.get("student");
        assertEquals("Ana", student.get("name"));
        Map<String, Object> record = ((List<Map<String, Object>>) client.data.get("records")).get(0);
        assertEquals("Algoritmos", record.get("name"));         // subject -> name
        assertEquals("2024/2025", record.get("academic_year")); // course  -> academic_year
        assertEquals(3.5, record.get("score"));                 // grade   -> score
    }
}
