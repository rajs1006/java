package de.funkedigital.autotagging.controllers;

import de.funkedigital.autotagging.entities.web.Request;
import de.funkedigital.autotagging.entities.web.Response;
import de.funkedigital.autotagging.exceptions.AutoTaggingException;
import de.funkedigital.autotagging.semantic.services.SemanticService;
import de.funkedigital.autotagging.services.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

/**
 * This class is to respond to wen request.
 * <p>
 * This service can be enabled/disabled
 * ----> (At Startup) setting web.service.enabled = true/false from Application.yml
 */
@ControllerAdvice
@RestController
@ConditionalOnProperty(name = "web.service.enabled", havingValue = "true", matchIfMissing = true)
@RequestMapping("/auto-tagging")
public class WebController {

    private static final Logger LOG = LoggerFactory.getLogger(WebController.class);

    /**
     * Constructor based Autowired SemanticAutoTaggingService, using {@link SemanticService}
     */
    private final WebService service;

    @Autowired
    public WebController(WebService service) {
        this.service = service;
    }


    /**
     * This method gets keywords from Semantic service {@link WebService}
     *
     * @param request Json request in form of
     *                {
     *                "articleUrl": "https://uat.morgenpost.de/incoming/article102164565/Forscher-kommen-dem-Raetsel-von-Stonehenge-auf-die-Spur.html",
     *                "publication": "bmo"
     *                }
     * @return Json Response {@link Response} with keyword populated
     */
    @RequestMapping("/")
    @ResponseBody
    public ResponseEntity<Response> getKeywords(@RequestBody Request request) {
        LOG.debug("Running getKeywords {} : {}", request.toString(), Thread.currentThread().getName());
        // Calling Service to retrieve keyword
        String keywords = this.service.returnKeywords(request.getPublication(), request.getArticleUrl());
        // Preparing response body
        Response response = new Response(new Date(), HttpStatus.OK, keywords);
        LOG.info("Sending response : {}", Thread.currentThread().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Exceptional handler for child exceptions of {@link AutoTaggingException}
     */
    @ExceptionHandler(AutoTaggingException.class)
    public final ResponseEntity<Response> handleNotFoundException(AutoTaggingException ex, WebRequest request) {
        LOG.error("Error occurred : {}", Thread.currentThread().getName(), ex);
        // send response
        Response exceptionResponse = new Response(new Date(), ex.getMessage(),
                request.getDescription(false), ex.getStatus()
                , null);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }


    /**
     * Exceptional handler for all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Response> handleNotFoundException(Exception ex, WebRequest request) {
        LOG.error("Error occurred : {}", Thread.currentThread().getName(), ex);
        // Send response
        Response exceptionResponse = new Response(new Date(), ex.getMessage(),
                request.getDescription(false), HttpStatus.INTERNAL_SERVER_ERROR
                , null);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
