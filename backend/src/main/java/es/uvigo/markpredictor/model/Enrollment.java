package es.uvigo.markpredictor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Matrícula: un estudiante cursa una asignatura en un curso académico.
 * - P6 · HU-04: horas de estudio de esa asignatura.
 * - P7 · HU-02: curso académico y calificación (puede estar vacía).
 *
 * La restricción única impide matricular dos veces al mismo estudiante en la misma
 * asignatura y curso, pero sí permite repetir la asignatura en otro curso.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "subject_id", "course_id"}))
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JsonIgnore // evita un bucle infinito al convertir a JSON (estudiante -> matrícula -> estudiante...)
    private Student student;

    @ManyToOne(optional = false)
    private Subject subject;

    @ManyToOne(optional = false)
    @JsonIgnoreProperties("subjects") // en el JSON de la matrícula basta con el nombre del curso
    private Course course;

    private double studyHours;

    private Double grade; // null = todavía sin calificación

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public double getStudyHours() { return studyHours; }
    public void setStudyHours(double studyHours) { this.studyHours = studyHours; }
    public Double getGrade() { return grade; }
    public void setGrade(Double grade) { this.grade = grade; }
}
