package es.uvigo.markpredictor.client;

/** Respuesta del microservicio de predicciones: {"base": 70, "coefficient": 1.2, "score": 84}. */
public record PredictionResult(double base, double coefficient, double score) {
}
