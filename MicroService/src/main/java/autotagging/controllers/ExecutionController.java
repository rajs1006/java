package de.funkedigital.autotagging.controllers;

import de.funkedigital.autotagging.entities.web.Articles;
import de.funkedigital.autotagging.entities.web.Detail;
import de.funkedigital.autotagging.entities.web.DetailForm;
import de.funkedigital.autotagging.entities.web.Response;
import de.funkedigital.autotagging.entities.web.ScheduleDetail;
import de.funkedigital.autotagging.entities.web.Sitemaps;
import de.funkedigital.autotagging.enums.RepositoryEnum;
import de.funkedigital.autotagging.enums.SystemEnum;
import de.funkedigital.autotagging.jobs.SchedulerJob;
import de.funkedigital.autotagging.services.ExecutionService;
import de.funkedigital.autotagging.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This class is basically for monitoring purpose and staar/stop services.
 *
 * @author sraj
 */
@ControllerAdvice
@Controller
@ConditionalOnProperty(name = "scheduler.service.enabled", havingValue = "true", matchIfMissing = true)
@RequestMapping("/auto-tagging")
public class ExecutionController {


    private static Logger LOG = LoggerFactory.getLogger(ExecutionController.class);

    /**
     * Autowired Instance of {@link SchedulerController#articlesServices(Iterable)} ()}
     */
    @Autowired
    @Qualifier("articlesServices")
    private Map<String, SchedulerJob> articlesServices;

    /**
     * All the publication Ids, Autowired Instance of {@link SchedulerController#publicationIds(Iterable)}
     */
    @Autowired
    @Qualifier("publicationIds")
    private List<String> publicationIds;

    /**
     * Autowired Instance of {@link ExecutionService}
     */
    @Autowired
    private ExecutionService executionService;

    /**
     * Path of resource folder
     */
    @Autowired
    private String resourcePath;


    /**
     * This method is start and stop the service execution
     * for each publication and service
     *
     * @param detailForm {@link DetailForm}
     * @return redirect to next method {@link #details(Model)}
     */
    @RequestMapping("/schedule")
    public ModelAndView schedule(
            @ModelAttribute DetailForm detailForm) {
        LOG.debug("Running schedule {} : {}", detailForm.toString(), Thread.currentThread().getName());
        // Iterate over details.
        for (Detail detail : detailForm.getDetails()) {
            String publication = detail.getPublication();
            List<ScheduleDetail> scheduleDetails = detail.getScheduleDetails();
            for (ScheduleDetail scheduleDetail : scheduleDetails) {
                // execute service
                executionService.executeAndStore(publication, scheduleDetail.getServiceEnum(), scheduleDetail.isEnable()
                        , scheduleDetail.getDelay()
                        , articlesServices.get(publication + Constants.NAME_SEPARATOR + scheduleDetail.getServiceEnum()));
            }
        }
        // Preparing response body
        return new ModelAndView("redirect:/auto-tagging/details");
    }

    /**
     * This method is to return all the details for Publication and gives a
     * way to enable disable services for each publication separatly on UI.
     *
     * @param model {@link Model}
     * @return Template /resources/templates/auto-tagging
     */
    @RequestMapping("/details")
    public String details(Model model) {
        LOG.debug("Running details {}", Thread.currentThread().getName());
        List<Detail> details = executionService.getDetails();
        // Preparing response body
        DetailForm detailForm = new DetailForm(details);
        model.addAttribute("detailForm", detailForm);
        return "auto-tagging-details";
    }

    /**
     * This method is to return detils of sitemap files for Publication.
     *
     * @param model        {@link Model}
     * @param requestedPub Requested publication
     * @return Template /resources/templates/auto-tagging
     */
    @RequestMapping("/sitemap-details")
    public String sitemapDetails(Model model,
                                 @RequestParam(name = "requestedPub", required = false) String requestedPub,
                                 @RequestParam(name = "sitemap", required = false) String sitemap) {
        LOG.debug("Running sitemapDetails {}", Thread.currentThread().getName());

        // default vaulue is the first entry in publication
        if (requestedPub == null) {
            requestedPub = publicationIds.get(0);
        }
        model.addAttribute("requestedPub", requestedPub);
        // Set publications.
        model.addAttribute("publications", publicationIds);
        // set search parameter
        model.addAttribute("sitemap", sitemap);

        List<Sitemaps> sitemaps = executionService.getSiteMapDetails(requestedPub, sitemap);
        model.addAttribute("count", sitemaps.size());
        // Preparing response body
        model.addAttribute("sitemaps", sitemaps);

        return "auto-tagging-sitemaps";
    }

    /**
     * This method is to return all the articles on UI
     *
     * @param model           {@link Model}
     * @param requestedPub    This is the requested publication from Dropdown
     * @param requestedRepo   This is the requested {@link RepositoryEnum} from Dropdown.
     * @param requestedSystem This is the requested {@link SystemEnum} from Dropdown.
     * @return Template /resources/templates/auto-tagging-articles
     */
    @RequestMapping("/article-details")
    public String articleDetails(Model model,
                                 @RequestParam(name = "requestedPub", required = false) String requestedPub,
                                 @RequestParam(name = "requestedRepo", required = false) RepositoryEnum requestedRepo,
                                 @RequestParam(name = "requestedSystem", required = false) SystemEnum requestedSystem,
                                 @RequestParam(name = "article", required = false) String article) {
        LOG.debug("Running articleDetails {} : {} : {} : {}"
                , requestedPub
                , requestedRepo
                , requestedSystem
                , Thread.currentThread().getName());
        // default vaulue is the first entry in publication
        if (requestedPub == null) {
            requestedPub = publicationIds.get(0);
        }
        model.addAttribute("requestedPub", requestedPub);

        // Default value is Executed
        if (requestedSystem == null) {
            requestedSystem = SystemEnum.Database;
        }
        model.addAttribute("requestedSystem", requestedSystem);

        // Default value is Executed
        if (requestedRepo == null) {
            requestedRepo = RepositoryEnum.Executed;
        }
        model.addAttribute("requestedRepo", requestedRepo);
        // Set publications.
        model.addAttribute("publications", publicationIds);
        // set search parameter
        model.addAttribute("article", article);

        int size = 0;
        // Executed articles
        List<Articles> articles;
        if (requestedSystem.equals(SystemEnum.Database)) {
            articles = executionService.getArticleDetails(requestedPub, requestedRepo, article);
            size = articles.size();
        } else {
            articles = executionService.getFailSafeArticleDetails(requestedPub, requestedRepo, this.resourcePath);
            // We append some extra objects so for size we substract them
            if(requestedRepo == RepositoryEnum.Failed) {
                size = articles.size() - 2;
            }else{
                size = articles.size() - 1;
            }
        }
        model.addAttribute("count", size);
        model.addAttribute("articles", articles);

        return "auto-tagging-articles";
    }

    /**
     * Exception handler for all he exception occurred
     */
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Response> handleNotFoundException(Exception ex, WebRequest request) {

        Response exceptionResponse = new Response(new Date(), ex.getMessage(),
                request.getDescription(false), HttpStatus.INTERNAL_SERVER_ERROR
                , null);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
