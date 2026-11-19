package es.uvigo.markpredictor.dto;

/** Resultado de la predicción de una asignatura, tal y como se muestra en la web. */
public record EnrollmentPrediction(String subject, String difficulty, double studyHours,
                                   double base, double coefficient, double score) {
}
