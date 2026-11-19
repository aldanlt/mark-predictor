package es.uvigo.markpredictor.exception;

/** Error 409 - la operación choca con datos ya guardados. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
