package de.funkedigital.autotagging.controllers;

import de.funkedigital.autotagging.exceptions.AutoTaggingException;
import de.funkedigital.autotagging.responses.Response;
import de.funkedigital.autotagging.semantic.services.SemanticAutoTaggingService;
import de.funkedigital.autotagging.services.AutoTaggingWebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@ControllerAdvice
@RestController
@ConditionalOnProperty(name = "service.enabled", havingValue = "true", matchIfMissing = true)
public class AutoTaggingWebController {

    private static final Logger LOG = LoggerFactory.getLogger(AutoTaggingWebController.class);

    /**
     * Constructor based Autowired SemanticAutoTaggingService, using {@link SemanticAutoTaggingService}
     */
    private final AutoTaggingWebService service;

    @Autowired
    public AutoTaggingWebController(AutoTaggingWebService service) {
        this.service = service;
    }

    /**
     * @param model
     * @param articleId
     * @return
     */
    @RequestMapping("/auto-tag/{publication}/{articleId}")
    @ResponseBody
    public ResponseEntity<Response> getKeywords(Model model,
                                                @PathVariable(name = "publication") String publication,
                                                @PathVariable(name = "articleId") Long articleId) {

        // Calling Service to retrieve keyword
        String keywords = this.service.returnKeywords(publication, articleId);

        // Preparing response body
        Response response = new Response(new Date(), HttpStatus.OK, keywords);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * @param ex
     * @param request
     * @return
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
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Response> handleNotFoundException(Exception ex, WebRequest request) {
        LOG.error("Error occurred : ", ex);

        Response exceptionResponse = new Response(new Date(), ex.getMessage(),
                request.getDescription(false), HttpStatus.INTERNAL_SERVER_ERROR
                , null);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
