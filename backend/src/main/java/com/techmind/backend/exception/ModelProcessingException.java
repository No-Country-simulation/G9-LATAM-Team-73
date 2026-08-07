package com.techmind.backend.exception;

/**
 * Se lanza cuando falla el procesamiento del contenido (modelo, mock, etc).
 * Se traduce a un 500 en GlobalExceptionHandler.
 */
public class ModelProcessingException extends RuntimeException {

    public ModelProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelProcessingException(String message) {
        super(message);
    }
}
