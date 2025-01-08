package com.bitescout.generator.exceptions;

public class BiteException extends RuntimeException {
    public BiteException(String message) {
        super(message);
    }

    public BiteException(String message, Throwable cause) {
        super(message, cause);
    }
}
