package de.funkedigital.autotagging.exceptions;

import org.springframework.http.HttpStatus;

public class SemanticServiceException extends AutoTaggingException {

    /**
     * {@inheritDoc}
     */
    public SemanticServiceException(String message, HttpStatus status) {
        super(message, status);
    }

    /**
     * {@inheritDoc}
     */
    public SemanticServiceException(String message, Throwable cause, HttpStatus status) {
        super(message, cause, status);
    }

}
