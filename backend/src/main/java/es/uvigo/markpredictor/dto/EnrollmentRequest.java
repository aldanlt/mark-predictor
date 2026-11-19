package es.uvigo.markpredictor.dto;

/** Datos para crear una matrícula o modificar sus horas y su nota (grade puede ser null). */
public record EnrollmentRequest(Long studentId, Long subjectId, Long courseId, double studyHours, Double grade) {
}
