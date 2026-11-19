package es.uvigo.markpredictor.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

/** Estudiante con los datos que usa la predicción general. */
@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double studyHours;     // horas de estudio semanales (predicción general)
    private double attendance;     // asistencia en % (0-100)
    private double previousGrade;  // nota previa (0-10)

    /** Asignaturas en las que está matriculado, con sus horas. */
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Enrollment> enrollments = new ArrayList<>();

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getStudyHours() { return studyHours; }
    public void setStudyHours(double studyHours) { this.studyHours = studyHours; }
    public double getAttendance() { return attendance; }
    public void setAttendance(double attendance) { this.attendance = attendance; }
    public double getPreviousGrade() { return previousGrade; }
    public void setPreviousGrade(double previousGrade) { this.previousGrade = previousGrade; }
    public List<Enrollment> getEnrollments() { return enrollments; }
}
