package demo.exception;

public class FailedToMatchException extends RuntimeException {
    public FailedToMatchException(String message) {
        super(message);
    }
}
