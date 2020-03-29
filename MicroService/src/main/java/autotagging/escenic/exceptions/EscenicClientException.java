package de.funkedigital.autotagging.escenic.exceptions;

import de.funkedigital.autotagging.exceptions.AutoTaggingException;
import org.springframework.http.HttpStatus;

public class EscenicClientException extends AutoTaggingException {

    /**
     * {@inheritDoc}
     */
    public EscenicClientException(String message, HttpStatus status) {
        super(message, status);
    }

    /**
     * {@inheritDoc}
     */
    public EscenicClientException(String message, Throwable cause, HttpStatus status) {
        super(message, cause, status);
    }
}
