package es.uvigo.markpredictor.report;

/** Una matrícula dentro de un informe, con los nombres de campo que usa Java. */
public record EnrollmentRecord(String subject, String course, double studyHours, Double grade) {
}
