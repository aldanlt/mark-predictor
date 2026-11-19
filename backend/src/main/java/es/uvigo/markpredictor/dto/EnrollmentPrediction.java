package es.uvigo.markpredictor.dto;

/** Resultado de la predicción de una matrícula, tal y como se muestra en la web. */
public record EnrollmentPrediction(Long enrollmentId, String subject, String difficulty, String course,
                                   double studyHours, Double grade,
                                   double base, double coefficient, double score) {
}
