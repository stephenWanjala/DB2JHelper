package io.github.stephenwanjala.dbjhelper.exception;

/**
 * Custom exception class for DB2JHelper library.
 * This exception wraps all database-related exceptions and provides additional context.
 */
public class DB2HelperException extends RuntimeException {
    
    public DB2HelperException(String message) {
        super(message);
    }

    public DB2HelperException(String message, Throwable cause) {
        super(message, cause);
    }

    public DB2HelperException(Throwable cause) {
        super(cause);
    }
} 