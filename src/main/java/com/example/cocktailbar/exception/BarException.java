package com.example.cocktailbar.exception;

/**
 * Base class of every business-rule error raised by the bar application.
 *
 * It is unchecked on purpose: these errors mean the request itself is wrong
 * (unknown drink, not enough stock...), and forcing every method in between to
 * declare them would add noise without helping the caller recover.
 * Parent of the following three exceptions.
 */
public class BarException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BarException(String message) {
        super(message);
    }
}
