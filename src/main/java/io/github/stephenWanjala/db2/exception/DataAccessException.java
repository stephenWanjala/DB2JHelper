package io.github.stephenWanjala.db2.exception;

/**
 * Custom exception for database access errors
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
