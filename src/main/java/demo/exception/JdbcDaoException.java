package demo.exception;

public class JdbcDaoException extends RuntimeException {

    public JdbcDaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
