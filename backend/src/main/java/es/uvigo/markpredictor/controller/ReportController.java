package es.uvigo.markpredictor.controller;

import es.uvigo.markpredictor.report.AcademicReportFacade;
import es.uvigo.markpredictor.report.GeneratedReport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** El controlador solo llama a la fachada: no sabe nada de repositorios, factoría ni adaptador. */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final AcademicReportFacade reportFacade;

    public ReportController(AcademicReportFacade reportFacade) {
        this.reportFacade = reportFacade;
    }

    /** Ejemplo: GET /api/reports/students/1?format=csv */
    @GetMapping("/students/{studentId}")
    public ResponseEntity<String> download(@PathVariable Long studentId,
                                           @RequestParam(required = false) String format) {
        GeneratedReport report = reportFacade.generateReport(studentId, format);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(report.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.fileName() + "\"")
                .body(report.content());
    }
}
