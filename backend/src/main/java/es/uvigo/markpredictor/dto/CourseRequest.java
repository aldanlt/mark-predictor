package es.uvigo.markpredictor.dto;

import java.time.LocalDate;
import java.util.List;

/** Datos para crear o modificar un curso académico. */
public record CourseRequest(String name, LocalDate startDate, LocalDate endDate, List<Long> subjectIds) {
}
