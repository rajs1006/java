package de.funkedigital.autotagging.services;

import de.funkedigital.autotagging.entities.repo.ExecutedArticleEntity;
import de.funkedigital.autotagging.entities.repo.FailedArticleEntity;
import de.funkedigital.autotagging.entities.repo.PendingArticleEntity;
import de.funkedigital.autotagging.escenic.services.EscenicService;
import de.funkedigital.autotagging.jobs.SchedulerJob;
import de.funkedigital.autotagging.repositories.ExecutedArticleRepository;
import de.funkedigital.autotagging.repositories.FailedArticleRepository;
import de.funkedigital.autotagging.repositories.PendingArticleRepository;
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
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;


/**
 * This class is to process articles from {@link PendingArticleEntity} one-by-one.
 * <p>
 * It picks the top record from {@link PendingArticleEntity}, process it with {@link SemanticService}
 * then save it to {@link ExecutedArticleEntity} if return success else saves it to
 * {@link FailedArticleEntity} and then deletes it from {@link PendingArticleEntity} afterwards.
 *
 * @author sraj
 */
@Service
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PendingArticleService implements ServiceInterface {

    private static final Logger LOG = LoggerFactory.getLogger(PendingArticleService.class);


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
     * Autowired instance of {@link UnicornService}
     */
    @Autowired
    private UnicornService unicornService;

    /**
     * Autowired instance of {@link SemanticService}
     */
    @Autowired
    private SemanticService semanticService;

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

    // Publication propeprty
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
     * -> delete all the data from fail-safe file from {@link PendingArticleEntity}
     * -> Get the top record from {@link PendingArticleEntity}
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
     * ---------------------->Save the article in {@link FailedArticleEntity}
     * --------------------------> If Fails:
     * -------------------------------> If {@link DataIntegrityViolationException}
     * -------------------------------------> Log the warning and do nothing, as data is already there
     * -------------------------------> If {@link Exception}
     * -------------------------------------> Save the failed record to file
     * --------------> In both cases, finally block
     * --------------> Delete the article from {@link PendingArticleEntity}
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
            // process records from fail safe file before any further processing.
            executeFailSafe();
            // Fetch the top pending article from DB to process.
            PendingArticleEntity pendingArticle = pendingArticleRepository.findTopArticle(this.publication);
            if (pendingArticle != null) {
                String publication = pendingArticle.getPublication();
                String url = pendingArticle.getUrl();

                try {
                    // Execute services.
                    UnicornStore unicornStore = unicornService.analyzeArticle(url);
                    KeywordStore keywords = semanticService.returnKeywords(unicornStore.getAssetId());
                    escenicService.pushKeywords(keywords.toString(), unicornStore.getArticleId());
                    // Saves the record to executed list.
                    executedArticleRepository.save(publication, unicornStore.getAssetId(), url);
                } catch (Exception ex) {
                    LOG.error("Error occurred while processing {} : {} : {}", publication, url,
                            ExceptionUtils.getRootCauseMessage(ex), ex);
                    // If record fails, save the record to failed list.
                    failedArticleRepository.save(publication, url);
                } finally {
                    // delete the data from table, it avoid re-run for same record again and again.
                    pendingArticleRepository.delete(pendingArticle, ArrayUtils.EMPTY_LONG_OBJECT_ARRAY);
                }
            } else {
                // If no record found from pending articles, just log the warning and proceed.
                LOG.warn("No pending records for processing from {} !!", this.publication);
            }
        } catch (Throwable th) {
            // This situation should only happen when the fetching of record fails from Pending articles.
            // Which means DB is down.
            LOG.error("Database seem to be down {} : {} !!", this.publication,
                    ExceptionUtils.getRootCauseMessage(th), th);
        }
    }

    /**
     * This method is to delete failed {@link PendingArticleEntity} articles from Fail-safe files to
     * database before processing next record.
     * <p>
     * it takes care of records failed while being processed previously.
     */
    private void executeFailSafe() {
        LOG.debug("Executing executeFailSafe() {} : {} : {}", this.resourcePath, this.publication,
                Thread.currentThread().getName());
        // Pick articles from file and delete them.
        pendingArticleRepository.deleteFromFile(this.resourcePath, this.publication);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "PendingArticleService{" +
                "publication='" + publication + '\'' +
                '}';
    }
}
