package demo.exception;

public class MissmatchAnnotationException extends RuntimeException {
    public MissmatchAnnotationException(String message) {
        super(message);
    }
}
