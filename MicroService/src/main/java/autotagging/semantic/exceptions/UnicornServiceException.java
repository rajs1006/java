package de.funkedigital.autotagging.semantic.exceptions;

import de.funkedigital.autotagging.exceptions.AutoTaggingException;
import org.springframework.http.HttpStatus;

public class UnicornServiceException extends AutoTaggingException {

    /**
     * {@inheritDoc}
     */
    public UnicornServiceException(String message, HttpStatus status) {
        super(message, status);
    }

    /**
     * {@inheritDoc}
     */
    public UnicornServiceException(String message, Throwable cause, HttpStatus status) {
        super(message, cause, status);
    }
}
