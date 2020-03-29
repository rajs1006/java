package de.funkedigital.autotagging.services;

import de.funkedigital.autotagging.enums.ServiceEnum;
import de.funkedigital.autotagging.jobs.SchedulerJob;
import de.funkedigital.autotagging.repositories.ExecutedArticleRepository;
import de.funkedigital.autotagging.repositories.FailedArticleRepository;
import de.funkedigital.autotagging.repositories.PendingArticleRepository;
import de.funkedigital.autotagging.repositories.PublicationRepository;
import de.funkedigital.autotagging.scheduler.ScheduleManager;
import de.funkedigital.autotagging.services.interfaces.ServiceInterface;
import de.funkedigital.autotagging.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Checks for conditions whether services are allowed to run or not.
 * like, if already max number of records have been processed for the month.
 * <p>
 * Author Sraj
 */
@Service
@ConditionalOnProperty(name = "scheduler.service.enabled", havingValue = "true", matchIfMissing = true)
public class ArticlesOverviewService implements ServiceInterface {

    private static final Logger LOG = LoggerFactory.getLogger(ArticlesOverviewService.class);

    /**
     * Max record is 80,000 as, we have set 20,000 records to be accessed
     * as duplicate from {@link WebService}
     */
    @Value("${semantic.records.max.per.month}")
    private Integer maxRecordsPerMonth;

    @Autowired
    @Qualifier("articlesServices")
    private Map<String, SchedulerJob> articlesServices;

    /**
     * Autowired instance of {@link ScheduleManager}
     */
    @Autowired
    private ScheduleManager scheduleManager;

    /**
     * Autowired Instance of {@link PublicationRepository}
     */
    @Autowired
    private PublicationRepository publicationRepository;

    /**
     * Autowired instance of {@link ExecutedArticleRepository}
     */
    @Autowired
    private ExecutedArticleRepository executedArticleRepository;

    /**
     * Autowired instance of {@link PendingArticleRepository}
     */
    @Autowired
    private PendingArticleRepository pendingArticleRepository;

    /**
     * Autowired instance of {@link FailedArticleRepository}
     */
    @Autowired
    private FailedArticleRepository failedArticleRepository;

    /**
     * This service used to controll the execution if number of execution excceds the limit.
     * <p>
     * --> Check count from {@link ExecutedArticleRepository} and {@link FailedArticleRepository}
     * ---> If records processed > max number (100,000)
     * -----> Stop all services, if not stopped
     * ---> Else
     * -----> Check if service is running, then do nothing
     * -----> If service is stopped
     * -------->we check DB {@link PublicationRepository} to check if Service is intentionally stopped
     * ---------->if yes
     * ----------->we do nothing
     * ---------> else
     * -----------> We start service using the delay from DB.
     * <p>
     * Runs after every 24 hours
     */
    @Scheduled(fixedDelay = (1000 * 60 * 60 * 24))
    public void run() {
        LOG.debug("Executing run() every 24 hour : {}", Thread.currentThread().getName());
        // Get counts
        int executedArticleCounts = executedArticleRepository.getCountForCurrentMonth();
        int failedArticlesCount = failedArticleRepository.getCountForCurrentMonth();
        int pendingArticlesCount = pendingArticleRepository.getCountForCurrentMonth();

        LOG.info("Total pending records left to be processed {}", pendingArticlesCount);
        // Total count
        int totalRecordsProcessedThisMonth = executedArticleCounts + failedArticlesCount;
        if (totalRecordsProcessedThisMonth >= maxRecordsPerMonth) {
            LOG.warn("Total count {} for month has exceeded the limit {}", totalRecordsProcessedThisMonth, maxRecordsPerMonth);
            LOG.warn("Stopping the execution of all the scheduled services !!");
            // Iterate over services.
            for (Map.Entry<String, SchedulerJob> schedulerJobEntry : articlesServices.entrySet()) {
                SchedulerJob job = schedulerJobEntry.getValue();
                boolean isJobCancelled = job.getFuture().isCancelled();
                // Only stop if job is not stopped already
                if (!isJobCancelled) {
                    scheduleManager.stop(job);
                } else {
                    LOG.warn("Service {} is already stopped ", job);
                }
            }
        } else {
            LOG.info("Current count of processed records this month is {} less than limit {}", totalRecordsProcessedThisMonth, maxRecordsPerMonth);
            LOG.info("Continuing the execution of all scheduled service as expected !!");
            // Iterate over services.
            for (Map.Entry<String, SchedulerJob> schedulerJobEntry : articlesServices.entrySet()) {
                SchedulerJob job = schedulerJobEntry.getValue();
                boolean isJobCancelled = job.getFuture().isCancelled();
                // Only start of job is stopped.
                if (isJobCancelled) {
                    String key = schedulerJobEntry.getKey();
                    String[] k = key.split(Constants.NAME_SEPARATOR);
                    // Get publication and service
                    if (k.length == 2) {
                        String pub = k[0];
                        ServiceEnum ser = ServiceEnum.getServiceEnum(k[1]);
                        int existingDelay = publicationRepository.findDelayByPublicationAndService(pub, ser);
                        boolean existingEnabled = publicationRepository.findEnabledByPublicationAndService(pub, ser);
                        // If existing service is enabled in DB then only start the service else skip.
                        if (existingEnabled) {
                            job.getTrigger().setInterval(existingDelay);
                            scheduleManager.start(job);
                        } else {
                            LOG.warn("Service {} has been intentionally stopped, please use '/auto-tagging/details' url to start this service ", job);
                        }
                    } else {
                        LOG.error("{} : {} is not a valid service ", key, job);
                    }
                } else {
                    LOG.debug("Service {} is already running ", job);
                }
            }
        }
    }
}
