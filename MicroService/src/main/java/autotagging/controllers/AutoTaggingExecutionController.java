package de.funkedigital.autotagging.controllers;

import de.funkedigital.autotagging.jobs.AutoTaggingJob;
import de.funkedigital.autotagging.repositories.AutoTaggingRepository;
import de.funkedigital.autotagging.responses.Response;
import de.funkedigital.autotagging.scheduler.AutoTaggingScheduleManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.Map;

@ControllerAdvice
@RestController
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class AutoTaggingExecutionController {

    @Autowired
    private AutoTaggingScheduleManager scheduler;

    @Autowired
    private AutoTaggingRepository autoTaggingRepository;

    @Autowired
    @Qualifier("scheduledServices")
    private Map<String, AutoTaggingJob> scheduledServices;

    @Autowired
    @Qualifier("repositoryServices")
    private Map<String, AutoTaggingJob> repositoryServices;


    /**
     * @param model
     * @param publication
     * @param enable
     * @return
     */
    @RequestMapping("/schedule/{publication}/{enable}")
    @ResponseBody
    public ResponseEntity<Response> schedule(Model model,
                                             @PathVariable(name = "publication") String publication,
                                             @PathVariable(name = "enable") Boolean enable) {
        // Get the details of job from already loaded bean
        AutoTaggingJob scheduledJob = scheduledServices.get(publication);
        AutoTaggingJob repositoryJob = scheduledServices.get(publication);
        if (enable) {
            scheduler.start(scheduledJob);
            scheduler.start(repositoryJob);
        } else {
            scheduler.stop(scheduledJob);
            scheduler.stop(repositoryJob);
        }
        // Update the status in DB.
        autoTaggingRepository.updateScheduleStatus(enable, publication);
        // Preparing response body
        Response response = new Response(new Date(), HttpStatus.OK, "Success");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Response> handleNotFoundException(Exception ex, WebRequest request) {

        Response exceptionResponse = new Response(new Date(), ex.getMessage(),
                request.getDescription(false), HttpStatus.INTERNAL_SERVER_ERROR
                , null);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
