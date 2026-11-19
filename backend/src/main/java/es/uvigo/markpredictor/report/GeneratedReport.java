package es.uvigo.markpredictor.report;

/** Informe ya generado: nombre del fichero, tipo de contenido y el propio contenido. */
public record GeneratedReport(String fileName, String contentType, String content) {
}
