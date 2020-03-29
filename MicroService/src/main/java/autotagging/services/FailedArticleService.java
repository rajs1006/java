package de.funkedigital.autotagging.services;

import de.funkedigital.autotagging.entities.repo.ExecutedArticleEntity;
import de.funkedigital.autotagging.entities.repo.FailedArticleEntity;
import de.funkedigital.autotagging.escenic.services.EscenicService;
import de.funkedigital.autotagging.jobs.SchedulerJob;
import de.funkedigital.autotagging.repositories.ExecutedArticleRepository;
import de.funkedigital.autotagging.repositories.FailedArticleRepository;
import de.funkedigital.autotagging.semantic.entities.KeywordStore;
import de.funkedigital.autotagging.semantic.entities.UnicornStore;
import de.funkedigital.autotagging.semantic.services.SemanticService;
import de.funkedigital.autotagging.semantic.services.UnicornService;
import de.funkedigital.autotagging.services.interfaces.ServiceInterface;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

/**
 * This class is to process articles from {@link FailedArticleEntity} one-by-one.
 * <p>
 * It picks the top record from {@link FailedArticleEntity}, process it with {@link UnicornService} and
 * {@link SemanticService} then save it to {@link ExecutedArticleEntity} if return success else saves
 * it to {@link FailedArticleEntity} and then deletes it from {@link FailedArticleEntity} afterwards.
 *
 * @author sraj
 */
@Service
public class FailedArticleService implements ServiceInterface {

    private static final Logger LOG = LoggerFactory.getLogger(FailedArticleService.class);

    /**
     * Autowired instance of {@link ExecutedArticleRepository}
     */
    @Autowired
    private ExecutedArticleRepository executedArticleRepository;

    /**
     * Autowired instance of {@link FailedArticleRepository}
     */
    @Autowired
    private FailedArticleRepository failedArticleRepository;

    /**
     * Autowired instance of {@link SemanticService}
     */
    @Autowired
    private SemanticService semanticService;

    /**
     * Autowired instance of {@link UnicornService}
     */
    @Autowired
    private UnicornService unicornService;

    /**
     * Autowired instance of {@link EscenicService}
     */
    @Autowired
    private EscenicService escenicService;

    /**
     * Path of resource folder
     */
    @Autowired
    private String resourcePath;

    // Publication property
    private String publication;

    /**
     * This method is to set the property, ie publication.
     * As we have publication specific properties, diff files for
     * diff publications.
     *
     * @param publication , publication the service is loaded
     */
    public void setProperties(String publication) {
        this.publication = publication;
    }

    /**
     * This method is triggered on schedule : after 1 second of completion of previous run.
     * <p>
     * -> delete all the data from fail-safe file from {@link FailedArticleEntity}
     * -> Get the top record from {@link FailedArticleEntity}
     * ----> If Fails:
     * ----------> DB might be down, Log the error an try again
     * ----> If Success:
     * ----------> Get urk and then get asset ID from {@link UnicornService}
     * ----------> Execute {@link SemanticService} and push keywords to {@link EscenicService}
     * --------------> If Success:
     * ---------------------->Save the article in {@link ExecutedArticleEntity}
     * --------------------------> If Fails:
     * -------------------------------> If {@link DataIntegrityViolationException}
     * -------------------------------------> Log the warning and do nothing, as data is already there
     * -------------------------------> If {@link Exception}
     * -------------------------------------> Save the failed record to file
     * --------------> If Fails:
     * ---------------------->Save the article in {@link FailedArticleEntity}, this is done to rotate
     * the Artciles from top to bottom
     * --------------------------> If Fails:
     * -------------------------------> If {@link DataIntegrityViolationException}
     * -------------------------------------> Log the warning and do nothing, as data is already there
     * -------------------------------> If {@link Exception}
     * -------------------------------------> Save the failed record to file
     * --------------> In both cases, finally block
     * --------------> Delete the article from {@link FailedArticleEntity}
     * --------------------------> If Fails:
     * -------------------------------> If {@link EmptyResultDataAccessException}
     * -------------------------------------> Log the warning and do nothing, as data does not exists
     * -------------------------------> If {@link Exception}
     * -------------------------------------> Save the failed record to file
     * <p>
     * This method can be triggered from {@link de.funkedigital.autotagging.scheduler.ScheduleManager#start(SchedulerJob)}
     * and the future runs can be cancelled {@link de.funkedigital.autotagging.scheduler.ScheduleManager#stop(SchedulerJob)}
     * <p>
     * There is {@link de.funkedigital.autotagging.controllers.ExecutionController} to start and stop the execution.
     */
    @Override
    public void run() {
        LOG.info("Executing run() {} : {}", this.publication, Thread.currentThread().getName());
        try {
            executeFailSafe();
            FailedArticleEntity failedArticle = failedArticleRepository.findTopArticle(this.publication);
            if (failedArticle != null) {
                String publication = failedArticle.getPublication();
                String url = failedArticle.getUrl();

                try {
                    // Execute services
                    UnicornStore unicornStore = unicornService.analyzeArticle(url);
                    KeywordStore keywords = semanticService.returnKeywords(unicornStore.getAssetId());
                    escenicService.pushKeywords(keywords.toString(), unicornStore.getArticleId());
                    // If everything goes well, mark the record as executed.
                    executedArticleRepository.save(publication, unicornStore.getAssetId(), url);
                    // delete the data from table, it avoid re-run for same record again and again.
                    failedArticleRepository.delete(failedArticle, ArrayUtils.EMPTY_LONG_OBJECT_ARRAY);
                } catch (Exception e) {
                    LOG.error("Error occurred while processing {} : {} : {}", publication, url,
                            ExceptionUtils.getRootCauseMessage(e), e);
                    // If exception occurred, we delete the data and save the same data again at the last record of table.
                    failedArticleRepository.delete(failedArticle, ArrayUtils.EMPTY_LONG_OBJECT_ARRAY);
                    failedArticleRepository.save(failedArticle.getPublication(), failedArticle.getUrl());
                }
            } else {
                LOG.warn("No failed records to process for {} !!", this.publication);
            }
        } catch (Throwable th) {
            LOG.error("Database seem to be down... {} : {} !!", this.publication, ExceptionUtils.getRootCauseMessage(th), th);
        }
    }

    /**
     * This method is to delete failed {@link FailedArticleEntity} articles from Fail-safe files to
     * database before processing next record.
     * <p>
     * it takes care of records failed while being processed previously.
     */
    private void executeFailSafe() {
        LOG.debug("Executing executeFailSafe() {} : {} : {}", this.resourcePath, this.publication,
                Thread.currentThread().getName());
        failedArticleRepository.deleteFromFile(this.resourcePath, this.publication);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "FailedArticleService{" +
                "publication='" + publication + '\'' +
                '}';
    }
}
