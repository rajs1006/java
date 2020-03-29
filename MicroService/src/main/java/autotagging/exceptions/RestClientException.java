package de.funkedigital.autotagging.exceptions;

import org.springframework.http.HttpStatus;

public class RestClientException extends AutoTaggingException {

    /**
     * {@inheritDoc}
     */
    public RestClientException(String message, HttpStatus status) {
        super(message, status);
    }

    /**
     * {@inheritDoc}
     */
    public RestClientException(String message, Throwable cause, HttpStatus status) {
        super(message, cause, status);
    }

}
