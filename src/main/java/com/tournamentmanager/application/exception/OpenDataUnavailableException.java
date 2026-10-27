package com.tournamentmanager.application.exception;

public class OpenDataUnavailableException extends RuntimeException {
    public OpenDataUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenDataUnavailableException(String message) {
        super(message);
    }
}
