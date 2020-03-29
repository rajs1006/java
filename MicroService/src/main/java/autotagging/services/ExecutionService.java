package de.funkedigital.autotagging.services;

import de.funkedigital.autotagging.entities.repo.ExecutedArticleEntity;
import de.funkedigital.autotagging.entities.repo.FailedArticleEntity;
import de.funkedigital.autotagging.entities.repo.PendingArticleEntity;
import de.funkedigital.autotagging.entities.repo.PropertyEntity;
import de.funkedigital.autotagging.entities.repo.PublicationEntity;
import de.funkedigital.autotagging.entities.repo.ScheduleEntity;
import de.funkedigital.autotagging.entities.repo.SitemapEntity;
import de.funkedigital.autotagging.entities.web.Articles;
import de.funkedigital.autotagging.entities.web.Detail;
import de.funkedigital.autotagging.entities.web.ScheduleDetail;
import de.funkedigital.autotagging.entities.web.Sitemaps;
import de.funkedigital.autotagging.enums.RepositoryEnum;
import de.funkedigital.autotagging.enums.ServiceEnum;
import de.funkedigital.autotagging.jobs.SchedulerJob;
import de.funkedigital.autotagging.repositories.ExecutedArticleRepository;
import de.funkedigital.autotagging.repositories.FailedArticleRepository;
import de.funkedigital.autotagging.repositories.PendingArticleRepository;
import de.funkedigital.autotagging.repositories.PublicationRepository;
import de.funkedigital.autotagging.repositories.SitemapRepository;
import de.funkedigital.autotagging.scheduler.ScheduleManager;
import de.funkedigital.autotagging.utils.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Control the execution of services and fetch details.
 * <p>
 * Author Sraj
 */
@Service
public class ExecutionService {

    private static Logger LOG = LoggerFactory.getLogger(ExecutionService.class);

    /**
     * Autowired Instance of {@link ScheduleManager}
     */
    @Autowired
    private ScheduleManager scheduleManager;

    /**
     * Autowired Instance of {@link PublicationRepository}
     */
    @Autowired
    private PublicationRepository publicationRepository;

    /**
     * Autowired Instance of {@link ExecutedArticleRepository}
     */
    @Autowired
    private ExecutedArticleRepository executedArticleRepository;

    /**
     * Autowired Instance of {@link PendingArticleRepository}
     */
    @Autowired
    private PendingArticleRepository pendingArticleRepository;

    /**
     * Autowired Instance of {@link FailedArticleService}
     */
    @Autowired
    private FailedArticleRepository failedArticleRepository;

    /**
     * Autowired Instance of {@link SitemapRepository}
     */
    @Autowired
    private SitemapRepository sitemapRepository;


    /**
     * This method enables/disables the services and then store the status in database
     *
     * @param publication Publication for which service need to be enabled/disabled.
     * @param enable      True/False, if service is enabled or disabled
     * @param job         contains the details of service.
     */
    public void executeAndStore(String publication, ServiceEnum service, Boolean enable, int delay, SchedulerJob job) {
        LOG.debug("Executing executeAndStore {} : {} : {} : {}"
                , publication, enable, job, Thread.currentThread().getName());
        int existingDelay = publicationRepository.findDelayByPublicationAndService(publication, service);
        // Get the details of job from already loaded bean
        boolean isJobCancelled = job.getFuture().isCancelled();
        if (enable) {
            job.getTrigger().setInterval(delay);
            if (!isJobCancelled && existingDelay != delay) {
                // in case interval changes, stop and start service with new interval.
                scheduleManager.stop(job);
                // Start with new interval
                scheduleManager.start(job);
            } else if (isJobCancelled) {
                scheduleManager.start(job);
            }
        } else {
            if (!isJobCancelled) {
                scheduleManager.stop(job);
            }
        }
        // Update the status in DB, even for same values update the table as it won't make any difference.
        publicationRepository.updateScheduleStatus(publication, service, delay, enable);
    }

    /**
     * This method return all the records from {@link PublicationEntity} along with
     * {@link PropertyEntity} and {@link ScheduleEntity} and then transfer the details to
     * {@link Detail} to show it on {@link de.funkedigital.autotagging.controllers.ExecutionController}
     *
     * @return {@link Detail}, all the details to show on UI from {@link PublicationEntity}
     */
    public List<Detail> getDetails() {
        LOG.debug("Executing getDetails {} ", Thread.currentThread().getName());
        // get details from repository
        Iterable<PublicationEntity> publicationEntities = publicationRepository.findAll();

        List<Detail> details = new ArrayList<>();
        publicationEntities.forEach(publicationEntity -> {
                    List<ScheduleDetail> scheduleDetails = new ArrayList<>();
                    // Sorted list
                    List<ScheduleEntity> scheduleEntities = publicationEntity.getSortedScheduleEntities();
                    for (ScheduleEntity scheduleEntity : scheduleEntities) {
                        scheduleDetails.add(new ScheduleDetail(scheduleEntity.getService().getServiceEnum(),
                                scheduleEntity.getDelay(), scheduleEntity.getEnabled()));
                    }

                    details.add(new Detail(publicationEntity.getId()
                            , publicationEntity.getPublication()
                            , publicationEntity.getPropertyEntity().getSitemapFiles()
                            , scheduleDetails));
                }
        );
        // List of details.
        return details;
    }

    /**
     * This method return all the records from {@link Sitemaps} then transfer the details to
     * {@link Sitemaps} to show it on {@link de.funkedigital.autotagging.controllers.ExecutionController}
     *
     * @return {@link Sitemaps}, all the details to show on UI from {@link SitemapEntity}
     */
    public List<Sitemaps> getSiteMapDetails(String publication, String sitemap) {
        LOG.debug("Executing getSiteMapDetails {} ", Thread.currentThread().getName());
        // get details from repository
        Iterable<SitemapEntity> sitemapEntities;
        if (sitemap != null) {
            sitemapEntities = sitemapRepository.findAllByPublicationAndUrl(publication, "%" + sitemap + "%");
        } else {
            sitemapEntities = sitemapRepository.findAllByPublication(publication);
        }

        List<Sitemaps> sitemaps = new ArrayList<>();
        sitemapEntities.forEach(sitemapEntity ->
                sitemaps.add(new Sitemaps(sitemapEntity.getId()
                        , sitemapEntity.getPublication()
                        , sitemapEntity.getUrl()
                        , sitemapEntity.getLastModDate()
                        , sitemapEntity.getExecutionStatus()
                        , sitemapEntity.getSiteMapLoadDate()))
        );
        // List of details.
        return sitemaps;
    }

    /**
     * Service method to return all the articles.
     *
     * @param publication    Publication to be queried
     * @param repositoryEnum enum type
     * @return {@link List<Articles>}
     */
    public List<Articles> getArticleDetails(String publication, RepositoryEnum repositoryEnum, String articleId) {
        LOG.debug("Executing getArticleDetails {} : {} : {} ", publication, repositoryEnum
                , Thread.currentThread().getName());

        String articleUrl = null;
        if (articleId != null) {
            // regex expression of URL search
            articleUrl = "%" + articleId + "%";
        }

        List<Articles> articles = new ArrayList<>();
        switch (repositoryEnum) {
            case Executed:
                Iterable<ExecutedArticleEntity> executedArticleEntities;
                if (articleUrl != null) {
                    executedArticleEntities = executedArticleRepository
                            .findAllByPublicationAndUrl(publication, articleUrl);
                } else {
                    executedArticleEntities = executedArticleRepository
                            .findAllByPublication(publication);
                }
                executedArticleEntities.forEach(articleEntity ->
                        setExecutedArticleEntity(articles, articleEntity)
                );
                break;
            case Pending:
                Iterable<PendingArticleEntity> pendingArticleEntities;

                if (articleUrl != null) {
                    pendingArticleEntities = pendingArticleRepository
                            .findAllByPublicationAndUrl(publication, articleUrl);
                } else {
                    pendingArticleEntities = pendingArticleRepository
                            .findAllByPublication(publication);
                }
                pendingArticleEntities.forEach(articleEntity ->
                        setPendingArticleEntity(articles, articleEntity)
                );
                break;
            case Failed:
                Iterable<FailedArticleEntity> failedArticleEntities;
                if (articleUrl != null) {
                    failedArticleEntities = failedArticleRepository
                            .findAllByPublicationAndUrl(publication, articleUrl);
                } else {
                    failedArticleEntities = failedArticleRepository
                            .findAllByPublication(publication);
                }
                failedArticleEntities.forEach(articleEntity ->
                        setFailedArticleEntity(articles, articleEntity)
                );
                break;
        }

        return articles;
    }


    /**
     * This method fetch details from Fail-safe files
     * <p>
     * FIle location is : /build/classes/java/main/{publication}/files
     *
     * @param publication    Publication to be queried
     * @param repositoryEnum enum type
     * @return {@link List<Articles>}
     */
    public List<Articles> getFailSafeArticleDetails(String publication, RepositoryEnum repositoryEnum, String
            resourcePath) {
        LOG.debug("Executing getFailSafeArticleDetails {} : {} : {} ", publication, repositoryEnum, Thread.currentThread().getName());

        List<Articles> articles = new ArrayList<>();
        switch (repositoryEnum) {
            case Executed:
                savedExecutedArticles(publication, resourcePath, articles);
                break;
            case Pending:
                savedPendingArticles(publication, resourcePath, articles);
                break;
            case Failed:
                savedFailedArticles(publication, resourcePath, articles);
                deletedFailedArticles(publication, resourcePath, articles);
                break;
        }
        return articles;
    }

    /*
     **********************************************
     * Private methods to GET DATA FROM file-SYSTEM
     **********************************************
     */


    private void savedExecutedArticles(String publication, String resourcePath, List<Articles> articles) {
        try {
            File fileUrl;
            articles.add(new Articles(0L
                    , "----Below are the Fail-safe articles failed to be SAVED because of Database operations----"
                    , "-----------------", null));
            fileUrl = new File(resourcePath.concat(String.format("%s/%s", publication
                    , ExecutedArticleRepository.SAVE_FAIL_SAFE_FILE)));
            Iterable<String> executedFailSafeArticles = FileUtils.readLines(fileUrl, Constants.STRING_ENCODING);
            executedFailSafeArticles.forEach(executedSaveArticles -> {

                        ExecutedArticleEntity articleEntity = new ExecutedArticleEntity()
                                .fromString(executedSaveArticles);
                        setExecutedArticleEntity(articles, articleEntity);
                    }
            );
        } catch (IOException ie) {
            logFileNotFoundException(ie);
        }
    }


    private void savedPendingArticles(String publication, String resourcePath, List<Articles> articles) {

        try {
            File fileUrl;
            articles.add(new Articles(0L
                    , "----Below are the Fail-safe articles failed to be SAVED because of Database operations----"
                    , "-----------------", null));
            fileUrl = new File(resourcePath.concat(String.format("%s/%s"
                    , publication, PendingArticleRepository.DELETE_FAIL_SAFE_FILE)));
            Iterable<String> pendingFailSafeArticles = FileUtils.readLines(fileUrl, "UTF-8");
            pendingFailSafeArticles.forEach(pendingSaveArticles -> {

                        PendingArticleEntity articleEntity = new PendingArticleEntity()
                                .fromString(pendingSaveArticles);
                        setPendingArticleEntity(articles, articleEntity);
                    }
            );
        } catch (IOException ie) {
            logFileNotFoundException(ie);
        }
    }


    private void savedFailedArticles(String publication, String resourcePath, List<Articles> articles) {
        File fileUrl;
        try {
            articles.add(new Articles(0L
                    , "----Below are the Fail-safe articles failed to be SAVED because of Database operations----"
                    , "-----------------", null));
            // Add Article from SAVE fail-safe file
            fileUrl = new File(resourcePath.concat(String.format("%s/%s"
                    , publication, FailedArticleRepository.SAVE_FAIL_SAFE_FILE)));
            Iterable<String> failedSaveFailSafeArticles = FileUtils.readLines(fileUrl, "UTF-8");
            failedSaveFailSafeArticles.forEach(failedSaveArticles -> {

                        FailedArticleEntity articleEntity = new FailedArticleEntity()
                                .fromSaveString(failedSaveArticles);
                        setFailedArticleEntity(articles, articleEntity);
                    }
            );
        } catch (IOException ie) {
            logFileNotFoundException(ie);
        }
    }


    private void deletedFailedArticles(String publication, String resourcePath, List<Articles> articles) {
        File fileUrl;
        try {
            articles.add(new Articles(0L
                    , "----Below are the Fail-safe articles failed to be DELETED because of Database operations----"
                    , "-----------------", null));

            // Add Article from DELETE fail-safe file
            fileUrl = new File(resourcePath.concat(String.format("%s/%s"
                    , publication, FailedArticleRepository.DELETE_FAIL_SAFE_FILE)));
            Iterable<String> failedDeleteFailSafeArticles = FileUtils.readLines(fileUrl, "UTF-8");
            failedDeleteFailSafeArticles.forEach(failedSaveArticles -> {

                        FailedArticleEntity articleEntity = new FailedArticleEntity()
                                .fromDeleteString(failedSaveArticles);
                        setFailedArticleEntity(articles, articleEntity);
                    }
            );
        } catch (IOException ie) {
            logFileNotFoundException(ie);
        }
    }


    private void setExecutedArticleEntity(List<Articles> articles, ExecutedArticleEntity articleEntity) {
        articles.add(new Articles(articleEntity.getId()
                , articleEntity.getPublication()
                , articleEntity.getAssetId()
                , articleEntity.getUrl()
                , articleEntity.getExecutionDate()));
    }

    private void setPendingArticleEntity(List<Articles> articles, PendingArticleEntity articleEntity) {
        articles.add(new Articles(articleEntity.getId()
                , articleEntity.getPublication()
                , articleEntity.getUrl()
                , articleEntity.getExecutionDate()));
    }

    private void setFailedArticleEntity(List<Articles> articles, FailedArticleEntity articleEntity) {
        articles.add(new Articles(articleEntity.getId()
                , articleEntity.getPublication()
                , articleEntity.getUrl()
                , articleEntity.getExecutionDate()));
    }

    private void logFileNotFoundException(IOException ie) {
        LOG.warn("There is no file to access so it is OK !!" + ExceptionUtils.getRootCauseMessage(ie));
    }
}
