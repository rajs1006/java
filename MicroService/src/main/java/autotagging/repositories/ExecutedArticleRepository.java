package de.funkedigital.autotagging.repositories;

import de.funkedigital.autotagging.entities.repo.ExecutedArticleEntity;
import de.funkedigital.autotagging.exceptions.RepositoryException;
import de.funkedigital.autotagging.utils.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import javax.validation.ConstraintViolationException;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is to save records in {@link ExecutedArticleEntity}, if they fail to be processed
 * because of any reason and also other bundle other queries.
 * <p>
 * This class performs a fail safe operation when the {@link #save(String, String, String)}
 * operation fails, save those records in a file.Mainly the reason for such failure is
 * DB down. which is very unlikely.
 * <p>
 * For other exceptions it handle the situation differently. Look for the method documentation.
 *
 * @author sraj
 */
@Repository
public interface ExecutedArticleRepository extends CrudRepository<ExecutedArticleEntity, Long> {

    Logger LOG = LoggerFactory.getLogger(ExecutedArticleRepository.class);

    /**
     * File to store the records in case of failure while saving the record
     */
    String SAVE_FAIL_SAFE_FILE = "executed-save-articles.txt";

    /**
     * This method returns the count of records for current month. and helps keep the
     * record of processed records.
     *
     * @return Count of records of current month
     */
    @Query(nativeQuery = true, value = "select count(*) from EXECUTED_ARTICLES " +
            "where year(execution_date) = year(current_date) and  month(execution_date) = month(current_date)")
    int getCountForCurrentMonth();

    /**
     * This method fires a query to find the record if already present in the {@link ExecutedArticleEntity}
     *
     * @param publication publication,for which the service is executing
     * @param url         url to be processed
     * @return Boolean, true , if record is present
     */
    @Query(nativeQuery = true, value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END from " +
            "EXECUTED_ARTICLES WHERE publication = :publication AND url = :url ")
    boolean findByPublicationAndUrl(@Param("publication") String publication,
                                    @Param("url") String url);

    /**
     * This method perfrom search using article ID in url
     *
     * @param publication publication,for which the service is executing
     * @param url         regex expression if url to be processed
     * @return List of Objects
     */
    @Query(nativeQuery = true, value = "SELECT * from " +
            "EXECUTED_ARTICLES WHERE publication = :publication AND url like :url ")
    List<ExecutedArticleEntity> findAllByPublicationAndUrl(@Param("publication") String publication,
                                    @Param("url") String url);

    /**
     * Get all the records given  publication
     *
     * @param publication publication for query
     * @return List of {@link ExecutedArticleEntity}
     */
    @Query(nativeQuery = true, value = "SELECT * from EXECUTED_ARTICLES WHERE publication = :publication")
    List<ExecutedArticleEntity> findAllByPublication(@Param("publication") String publication);


    /**
     * This method save the Executed article asynchronously(In background)
     * <p>
     * This method, do not perform any action if a duplicate record is being stored
     * {@link DataIntegrityViolationException}.
     * This method, saves the data to a file in case of exception occurred
     * <p>
     * This method runs asynchronously (In the background)
     *
     * @param publication publication,for which the service is executing
     * @param assetId     asset Id returned from {@link de.funkedigital.autotagging.semantic.services.UnicornService}
     * @param url         url of the article
     */
    @Async
    default void save(String publication, String assetId, String url) {
        LOG.debug("Saving article : {} {} {}", publication, assetId, Thread.currentThread().getName());
        ExecutedArticleEntity executedArticle = new ExecutedArticleEntity(publication, assetId, url);
        try {
            this.save(executedArticle);
        } catch (ConstraintViolationException | DataIntegrityViolationException ce) {
            LOG.warn("Constraint violation exception, record already exists in DB : {}", executedArticle.toString(),
                    ExceptionUtils.getRootCauseMessage(ce));
        } catch (Exception e) {
            throw new RepositoryException(String.format("ExecutedArticleEntity persistence failed %s \t %s"
                    , publication, e.getMessage())
                    , e
                    , String.format("%s/%s", publication, this.SAVE_FAIL_SAFE_FILE)
                    , executedArticle.toString());
        }
    }

    /**
     * This method save the articles that were failed while saving in {@link ExecutedArticleEntity}
     * first time and was saved to the file.
     * <p>
     * This method, reads the data from file and once all the data is saved, delete the file.
     * <p>
     * This method runs asynchronously (In the background)
     *
     * <b>IF, file is not present, that means there are no failed records to procees and nothing to worry.</b>
     *
     * @param publication publication,for which the service is executing
     * @param resource    location of resource.
     */
    @Async
    default void saveFromFile(String resource, String publication) {
        LOG.debug("Saving data from file : {} {} {}", resource, publication, Thread.currentThread().getName());
        try {
            File file = new File(resource.concat(publication)
                    .concat(Constants.FOLDER_SEPARATOR + SAVE_FAIL_SAFE_FILE));
            List<String> lines = FileUtils.readLines(file, Constants.STRING_ENCODING);

            Set<ExecutedArticleEntity> executedArticles = new HashSet<>();

            for (String line : lines) {
                ExecutedArticleEntity executedArticle = new ExecutedArticleEntity().fromString(line);
                if (executedArticle != null) {
                    executedArticles.add(executedArticle);
                }
            }
            this.save(executedArticles);
            // Once proceed all the articles successfully, delete the file.
            FileUtils.deleteQuietly(file);
        } catch (IOException e) {
            LOG.debug("No records are there in fail-safe to save ", ExceptionUtils.getRootCauseMessage(e));
        } catch (Exception e) {
            LOG.error("Could not persist file data to DB ", ExceptionUtils.getRootCauseMessage(e));
        }
    }
}
