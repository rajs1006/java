package de.funkedigital.autotagging.controllers;

import de.funkedigital.autotagging.entities.repo.PublicationEntity;
import de.funkedigital.autotagging.entities.repo.ScheduleEntity;
import de.funkedigital.autotagging.enums.ServiceEnum;
import de.funkedigital.autotagging.exceptions.AutoTaggingException;
import de.funkedigital.autotagging.jobs.SchedulerJob;
import de.funkedigital.autotagging.jobs.factory.SchedulerJobFactory;
import de.funkedigital.autotagging.repositories.PublicationRepository;
import de.funkedigital.autotagging.scheduler.ScheduleManager;
import de.funkedigital.autotagging.services.FailedArticleService;
import de.funkedigital.autotagging.services.LoadArticleService;
import de.funkedigital.autotagging.services.PendingArticleService;
import de.funkedigital.autotagging.services.factory.ServiceFactory;
import de.funkedigital.autotagging.triggers.ServiceTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static de.funkedigital.autotagging.utils.Constants.NAME_SEPARATOR;

/**
 * This class is to load service supposed to run on scheduled.
 * <p>
 * This service can be enabled/disable
 * ----> (at Start Up) setting scheduler.service.enabled = true/false from Application.yml
 * ----> (Run Time) using {@link ExecutionController} url = "http://dns:port/auto-tagging/details"
 *
 * @author sraj
 */
@Controller
@ConditionalOnProperty(name = "scheduler.service.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerController {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerController.class);

    /**
     * Autowired instance of {@link ApplicationContext}
     */
    @Autowired
    private ApplicationContext ctx;

    /**
     * Autowired instance of {@link ScheduleManager}
     */
    @Autowired
    private ScheduleManager scheduleManager;

    /**
     * Autowired instance of {@link PublicationRepository}
     */
    @Autowired
    private PublicationRepository publicationRepository;


    /**
     * Get all the publications from {@link PublicationEntity}
     *
     * @return List of {@link PublicationEntity}
     */
    @Bean(name = "publications")
    public Iterable<PublicationEntity> publications() {
        LOG.debug("Running publications {} ", Thread.currentThread().getName());
        return publicationRepository.findAll();

    }

    /**
     * All the publication Ids
     *
     * @return List of publications
     */
    @Bean(name = "publicationIds")
    @DependsOn("publications")
    public List<String> publicationIds(Iterable<PublicationEntity> publications) {
        LOG.debug("Running publicationIds {} ", Thread.currentThread().getName());
        // Store the publication
        return StreamSupport.stream(publications.spliterator(), false)
                .map(PublicationEntity::getPublication)
                .collect(Collectors.toList());
    }


    /**
     * This bean used to load Prototype instance of Services based
     * on publication and services from {@link PublicationRepository}
     * and {@link ServiceEnum}
     *
     * @return {@link Map<String, SchedulerJob>}, this Map Bean is used later
     * to start/stop services in {@link ExecutionController}
     */
    @Bean(name = "articlesServices")
    @DependsOn("publications")
    public Map<String, SchedulerJob> articlesServices(Iterable<PublicationEntity> publications) {
        LOG.debug("Running loadArticlesServices {} ", Thread.currentThread().getName());
        Map<String, SchedulerJob> services = new HashMap<>();
        try {
            for (PublicationEntity publication : publications) {
                List<ScheduleEntity> scheduleEntities = publication.getSortedScheduleEntities();
                // Create Service from factory
                for (ScheduleEntity scheduleEntity : scheduleEntities) {
                    if (scheduleEntity.getEnabled()) {
                        ServiceEnum serviceName = scheduleEntity.getService().getServiceEnum();
                        String key = publication.getPublication() + NAME_SEPARATOR + serviceName;
                        switch (serviceName) {
                            // Switch case
                            case LoadArticleService:
                                loadArticleService(services, publication, scheduleEntity, key);
                                break;
                            case PendingArticleService:
                                pendingArticleService(services, publication, scheduleEntity, key);
                                break;
                            case FailedArticleService:
                                failedArticleService(services, publication, scheduleEntity, key);
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new AutoTaggingException("Something is seriously wrong, {Check DB and property file} ",
                    e, HttpStatus.PRECONDITION_FAILED);
        }
        return services;
    }


    /**
     * This method used to load Prototype instance of {@link FailedArticleService}
     * Given properties {@link SchedulerJob} and then start the service
     * <p>
     * In this method
     * ---> Fetch details from {@link PublicationRepository}
     * ---> create Prototype instances of {@link FailedArticleService} using {@link ServiceFactory}
     * ---> Create prototype instances of {@link SchedulerJob} and set {@link FailedArticleService} to JOB.
     * ---> Start the Service using {@link ScheduleManager#start(SchedulerJob)}
     * ---> Set {@link Map<String, SchedulerJob>} to further access it.
     *
     * @return {@link Map<String, SchedulerJob>}, this Map Bean is used later
     * to start/stop services in {@link ExecutionController}
     */
    private void failedArticleService(Map<String, SchedulerJob> services, PublicationEntity publication, ScheduleEntity scheduleEntity, String key) {
        FailedArticleService failedArticleService = new ServiceFactory<>(FailedArticleService.class, this.ctx)
                .getObject();
        failedArticleService.setProperties(publication.getPublication());
        // Create SchedlureJob from factory
        SchedulerJob failedArticleJob = new SchedulerJobFactory(failedArticleService,
                new ServiceTrigger(scheduleEntity.getDelay())).getObject();
        // Star the execution of service.
        scheduleManager.start(failedArticleJob);
        // put in map
        services.put(key, failedArticleJob);
    }


    /**
     * This method used to load Prototype instance of {@link PendingArticleService}
     * Given properties {@link SchedulerJob} and then start the service
     * <p>
     * In this method
     * ---> Fetch details from {@link PublicationRepository}
     * ---> create Prototype instances of {@link PendingArticleService} using {@link ServiceFactory}
     * ---> Create prototype instances of {@link SchedulerJob} and set {@link PendingArticleService} to JOB.
     * ---> Start the Service using {@link ScheduleManager#start(SchedulerJob)}
     * ---> Set {@link Map<String,SchedulerJob>} to further access it.
     *
     * @return {@link Map<String,SchedulerJob>}, this Map Bean is used later
     * to start/stop services in {@link ExecutionController}
     */
    private void pendingArticleService(Map<String, SchedulerJob> services, PublicationEntity publication, ScheduleEntity scheduleEntity, String key) {
        PendingArticleService pendingArticleService = new ServiceFactory<>(PendingArticleService.class, this.ctx)
                .getObject();
        pendingArticleService.setProperties(publication.getPublication());
        // Create Scheduler Job from factory
        SchedulerJob pendingArticleJob = new SchedulerJobFactory(pendingArticleService,
                new ServiceTrigger(scheduleEntity.getDelay())).getObject();
        // Star the execution of service.
        scheduleManager.start(pendingArticleJob);
        services.put(key, pendingArticleJob);
    }


    /**
     * This method used to load Prototype instance of {@link LoadArticleService}
     * Given properties {@link SchedulerJob} and then start the service
     * <p>
     * In this method
     * ---> Fetch details from {@link PublicationRepository}
     * ---> create Prototype instances of {@link LoadArticleService} using {@link ServiceFactory}
     * ---> Create prototype instances of {@link SchedulerJob} and set {@link LoadArticleService} to JOB.
     * ---> Start the Service using {@link ScheduleManager#start(SchedulerJob)}
     * ---> Set {@link Map<String, SchedulerJob>} to further access it.
     *
     * @return {@link Map<String, SchedulerJob>}, this Map Bean is used later
     * to start/stop services in {@link ExecutionController}
     */
    private void loadArticleService(Map<String, SchedulerJob> services, PublicationEntity publication, ScheduleEntity scheduleEntity, String key) {
        LoadArticleService loadArticleService = new ServiceFactory<>(LoadArticleService.class, this.ctx)
                .getObject();
        loadArticleService.setProperties(publication.getPropertyEntity().getSitemapFiles(), publication.getPublication());
        // Create SchedlureJob from factory
        SchedulerJob loadArticleJob = new SchedulerJobFactory(loadArticleService,
                new ServiceTrigger(scheduleEntity.getDelay())).getObject();
        // Star the execution of service.
        scheduleManager.start(loadArticleJob);
        services.put(key, loadArticleJob);
    }
}
