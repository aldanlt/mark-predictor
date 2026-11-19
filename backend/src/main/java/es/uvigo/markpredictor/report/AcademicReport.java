package es.uvigo.markpredictor.report;

import java.util.List;

/**
 * CLASE ABSTRACTA (P7 · HU-04).
 *
 * Contiene lo que tienen en común todos los informes: el estudiante y sus matrículas,
 * y cómo se llama el fichero. Cada formato concreto (CSV, JSON) dice su formato y su tipo
 * de contenido implementando los métodos abstractos.
 */
public abstract class AcademicReport {

    private final Long studentId;
    private final String studentName;
    private final List<EnrollmentRecord> enrollments;

    protected AcademicReport(Long studentId, String studentName, List<EnrollmentRecord> enrollments) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.enrollments = enrollments;
    }

    /** "csv" o "json": es también la extensión del fichero. */
    public abstract String getFormat();

    /** Tipo MIME que se envía al navegador. */
    public abstract String getContentType();

    public String getFileName() {
        return "informe_estudiante_" + studentId + "." + getFormat();
    }

    public Long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public List<EnrollmentRecord> getEnrollments() { return enrollments; }
}
