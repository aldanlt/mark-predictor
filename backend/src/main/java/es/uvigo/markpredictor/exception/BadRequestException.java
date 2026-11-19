package es.uvigo.markpredictor.exception;

/** Error 400 - los datos enviados no son válidos. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
