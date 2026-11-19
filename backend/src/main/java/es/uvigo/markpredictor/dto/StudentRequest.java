package es.uvigo.markpredictor.dto;

import java.util.List;

/** Datos del formulario de registro de estudiantes, con las asignaturas marcadas (P6 · HU-04). */
public record StudentRequest(String name, double studyHours, double attendance, double previousGrade,
                             List<SelectedSubject> subjects) {

    /** Una casilla marcada del formulario: asignatura + horas de estudio. */
    public record SelectedSubject(Long subjectId, double studyHours) {
    }
}
