package es.uvigo.markpredictor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Matrícula (P6 · HU-04): un estudiante cursa una asignatura y dedica unas horas de estudio.
 * La restricción única impide matricular dos veces al mismo estudiante en la misma asignatura.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "subject_id"}))
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JsonIgnore // evita un bucle infinito al convertir a JSON (estudiante -> matrícula -> estudiante...)
    private Student student;

    @ManyToOne(optional = false)
    private Subject subject;

    private double studyHours;

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public double getStudyHours() { return studyHours; }
    public void setStudyHours(double studyHours) { this.studyHours = studyHours; }
}
