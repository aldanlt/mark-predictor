package es.uvigo.markpredictor.report;

/**
 * Interfaz que usa la aplicación para generar informes (P7 · HU-05).
 * Solo dice QUÉ se necesita (generar el contenido de un informe), no CÓMO.
 */
public interface ReportGenerator {

    /** Devuelve el contenido final del informe (texto CSV o JSON). */
    String generate(AcademicReport report);
}
