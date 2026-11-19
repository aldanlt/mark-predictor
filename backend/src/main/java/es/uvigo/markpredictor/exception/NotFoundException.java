package es.uvigo.markpredictor.exception;

/** Error 404 - el recurso pedido no existe. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
