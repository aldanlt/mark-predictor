package es.uvigo.markpredictor.exception;

/** Error 503 - un microservicio Python no responde. */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }
}
