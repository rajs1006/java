package de.funkedigital.autotagging.escenic.exceptions;

import de.funkedigital.autotagging.exceptions.AutoTaggingException;
import org.springframework.http.HttpStatus;

public class EscenicServiceException extends AutoTaggingException {

    /**
     * {@inheritDoc}
     */
    public EscenicServiceException(String message, HttpStatus status) {
        super(message, status);
    }

    /**
     * {@inheritDoc}
     */
    public EscenicServiceException(String message, Throwable cause, HttpStatus status) {
        super(message, cause, status);
    }
}
