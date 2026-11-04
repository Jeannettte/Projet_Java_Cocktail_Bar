package com.example.cocktailbar.exception;

/**
 * Raised when the data file cannot be read or contains invalid content.
 *
 * <p>This one is a <em>checked</em> exception: a missing or corrupted file is an
 * expected, recoverable situation at start-up, so the compiler forces the caller
 * to decide what to do (here: print a message and stop cleanly).
 */
public class DataLoadException extends Exception {

    private static final long serialVersionUID = 1L;

    public DataLoadException(String message) {
        super(message);
    }

    public DataLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
