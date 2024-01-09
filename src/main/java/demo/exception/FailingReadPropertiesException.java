package demo.exception;

public class FailingReadPropertiesException extends RuntimeException {
    public FailingReadPropertiesException(String message, Throwable cause) {
        super(message, cause);
    }
}

