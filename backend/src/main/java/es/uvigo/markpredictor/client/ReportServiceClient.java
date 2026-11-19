package es.uvigo.markpredictor.client;

import es.uvigo.markpredictor.config.AppConfig;
import es.uvigo.markpredictor.exception.ServiceUnavailableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Cliente del microservicio Python de informes (POST /reports/{formato}).
 * Trabaja con el formato de datos de Python (student, records, name, academic_year, score).
 * El resto del backend no lo usa directamente: lo usa a través de ReportServiceAdapter.
 */
@Component
public class ReportServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String url = AppConfig.getInstance().getReportServiceUrl() + "/reports/";

    public String createReport(String format, Map<String, Object> pythonData) {
        try {
            return restTemplate.postForObject(url + format, pythonData, String.class);
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("El servicio de informes no está disponible");
        }
    }
}
