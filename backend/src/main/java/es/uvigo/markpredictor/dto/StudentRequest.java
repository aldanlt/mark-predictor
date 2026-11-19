package es.uvigo.markpredictor.dto;

import java.util.List;

/**
 * Datos del formulario de registro de estudiantes.
 * Si se marcan asignaturas, se matricula al estudiante en ellas dentro del curso courseId.
 */
public record StudentRequest(String name, double studyHours, double attendance, double previousGrade,
                             Long courseId, List<SelectedSubject> subjects) {

    /** Una casilla marcada del formulario: asignatura + horas de estudio. */
    public record SelectedSubject(Long subjectId, double studyHours) {
    }
}
