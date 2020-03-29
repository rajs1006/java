package de.funkedigital.autotagging.responses;

import org.springframework.http.HttpStatus;

import java.util.Date;

public class Response {

    private Date timestamp;
    private String message;
    private String details;
    private HttpStatus status;

    /**
     * This field stores the returned keywords.
     * <p>
     * If in any case where Keywords retrieving keywords form Semantic engine fails,
     * this field would be returned emtpy.
     */
    private String keywords;

    public Response(Date timestamp, String message, String details, HttpStatus status, String keywords) {
        super();
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
        this.status = status;
        // This field is for keywords returned by the Semantic Engine
        this.keywords = keywords;
    }

    public Response(Date timestamp, HttpStatus status, String keywords) {
        super();
        this.timestamp = timestamp;
        this.status = status;
        // This field is for keywords returned by the Semantic Engine
        this.keywords = keywords;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    public String getKeywords() {
        return keywords;
    }
}
