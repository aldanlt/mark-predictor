package es.uvigo.markpredictor.client;

import es.uvigo.markpredictor.config.AppConfig;
import es.uvigo.markpredictor.exception.ServiceUnavailableException;
import es.uvigo.markpredictor.model.Difficulty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Llama al microservicio Python de predicciones (POST /predict).
 * Java no calcula nada: solo envía los datos y recibe el resultado.
 */
@Component
public class PredictionClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String url = AppConfig.getInstance().getPredictionServiceUrl() + "/predict";

    /** difficulty == null -> predicción general (sin coeficiente). */
    public PredictionResult predict(double studyHours, double attendance, double previousGrade,
                                    Difficulty difficulty) {
        Map<String, Object> body = new HashMap<>();
        body.put("study_hours", studyHours);
        body.put("attendance", attendance);
        body.put("previous_grade", previousGrade);
        if (difficulty != null) {
            body.put("difficulty", difficulty.name());
        }
        try {
            return restTemplate.postForObject(url, body, PredictionResult.class);
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("El servicio de predicciones no está disponible");
        }
    }
}
