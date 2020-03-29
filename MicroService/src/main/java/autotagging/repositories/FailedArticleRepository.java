package de.funkedigital.autotagging.repositories;

import de.funkedigital.autotagging.entities.repo.FailedArticleEntity;
import de.funkedigital.autotagging.exceptions.RepositoryException;
import de.funkedigital.autotagging.utils.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import javax.validation.ConstraintViolationException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * This clss is to save records in {@link FailedArticleEntity}, if they fail to be processed
 * because of any reason and also other bundle other queries.
 * <p>
 * This class performs a fail safe operation when the {@link #save(String, String)} or
 * {@link #delete(FailedArticleEntity, Long...)} operation fails, save those records in a file.
 * Mainly the reason for such failure is DB down. which is very unlikely.
 * <p>
 * For other exceptions it handle the situation differently. Look for the method documentation.
 *
 * @author sraj
 */
@Repository
public interface FailedArticleRepository extends CrudRepository<FailedArticleEntity, Long> {

    Logger LOG = LoggerFactory.getLogger(FailedArticleRepository.class);

    /**
     * File to store the records in case of failure while saving the records
     */
    String SAVE_FAIL_SAFE_FILE = "failed-save-articles.txt";

    /**
     * File to store the records in case of failure while deleting the records
     */
    String DELETE_FAIL_SAFE_FILE = "failed-delete-articles.txt";

    /**
     * It gets the top article from {@link FailedArticleEntity}
     *
     * @param publication using publication as parameter
     * @return {@link FailedArticleEntity} containing top data from table
     */
    @Query(nativeQuery = true, value = "SELECT TOP 1 * from FAILED_ARTICLES where publication = :publication")
    FailedArticleEntity findTopArticle(@Param("publication") String publication);

    /**
     * This method fires a query to find the record if already present in the {@link FailedArticleEntity}
     *
     * @param publication publication,for which the service is executing
     * @param url         url to be processed
     * @return Boolean, true , if record is present
     */
    @Query(nativeQuery = true, value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END from " +
            "FAILED_ARTICLES WHERE publication = :publication AND url = :url ")
    boolean findByPublicationAndUrl(@Param("publication") String publication,
                                    @Param("url") String url);

    /**
     * This method returns the count of records for current month. and helps keep the
     * record of processed records.
     *
     * @return Count of records of current month
     */
    @Query(nativeQuery = true, value = "select count(*) from FAILED_ARTICLES " +
            "where year(execution_date) = year(current_date) and  month(execution_date) = month(current_date)")
    int getCountForCurrentMonth();

    /**
     * This method perfrom search using article ID in url
     *
     * @param publication publication,for which the service is executing
     * @param url         regex expression if url to be processed
     * @return List of Objects
     */
    @Query(nativeQuery = true, value = "SELECT * from " +
            "FAILED_ARTICLES WHERE publication = :publication AND url like :url ")
    List<FailedArticleEntity> findAllByPublicationAndUrl(@Param("publication") String publication,
                                                         @Param("url") String url);

    /**
     * Get all the records given  publication
     *
     * @param publication publication for query
     * @return List of {@link FailedArticleEntity}
     */
    @Query(nativeQuery = true, value = "SELECT * from FAILED_ARTICLES WHERE publication = :publication")
    List<FailedArticleEntity> findAllByPublication(@Param("publication") String publication);


    /**
     * This method save the Executed article.
     * <p>
     * This method, do not perform any action if a duplicate record is being stored
     * {@link DataIntegrityViolationException}.
     * This method, saves the data to a file in case of exception occurred
     * <p>
     * This method runs asynchronously (In the background)
     *
     * @param publication publication,for which the service is executing
     * @param url         url of the article
     */
    @Async
    default void save(String publication, String url) {
        LOG.debug("Saving article : {} {} {}", publication, url, Thread.currentThread().getName());
        FailedArticleEntity failedArticle = new FailedArticleEntity(publication, url);
        try {
            this.save(failedArticle);
        } catch (ConstraintViolationException | DataIntegrityViolationException ce) {
            LOG.warn("Constraint violation exception, record already exists in DB : {}", failedArticle.toSaveString(),
                    ExceptionUtils.getRootCauseMessage(ce));
        } catch (Exception e) {
            throw new RepositoryException(String.format("FailedArticleEntity persistence failed for %s \t %s"
                    , publication, e.getMessage())
                    , e
                    , String.format("%s/%s", publication, this.SAVE_FAIL_SAFE_FILE)
                    , failedArticle.toSaveString());
        }
    }

    /**
     * This method, takes argument {@link FailedArticleEntity} or {@link Long[]} and deletes them.
     * <p>
     * IF, deletion fails because of {@link EmptyResultDataAccessException}, mean data is not available
     * to delete, it does nothing. As it is ok that data is not available
     *
     * @param failedArticle    {@link FailedArticleEntity} to delete
     * @param failedArticleIds {@link Long[]} as variable argument to delete by failedArticleId
     */
    default void delete(FailedArticleEntity failedArticle, Long... failedArticleIds) {
        if (failedArticle != null) {
            LOG.debug("Deleting article : {} {} {}", failedArticle.getPublication(), failedArticle.getUrl(),
                    Thread.currentThread().getName());
            try {
                if (!ArrayUtils.isEmpty(failedArticleIds)) {
                    for (Long failedArticleId : failedArticleIds) {
                        try {
                            this.delete(failedArticleId);
                        } catch (EmptyResultDataAccessException | ConstraintViolationException ce) {
                            LOG.warn("Constraint violation exception, record already deleted from DB : {}", failedArticleId,
                                    ExceptionUtils.getRootCauseMessage(ce));
                        }
                    }
                } else {
                    this.delete(failedArticle);
                }
            } catch (ConstraintViolationException | EmptyResultDataAccessException ce) {
                LOG.warn("Constraint violation exception, record already deleted from DB : {}",
                        failedArticle.toDeleteString(),
                        ExceptionUtils.getRootCauseMessage(ce));
            } catch (Exception e) {
                throw new RepositoryException(String.format("FailedArticleEntity persistence failed for %s \t %s"
                        , failedArticle.getPublication(), e.getMessage())
                        , e
                        , String.format("%s/%s", failedArticle.getPublication(), this.DELETE_FAIL_SAFE_FILE)
                        , failedArticle.toDeleteString());
            }
        }
    }


    /**
     * This method save the articles that were failed while saving in {@link FailedArticleEntity}
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

            Set<FailedArticleEntity> failedArticles = new HashSet<>();
            for (String line : lines) {
                FailedArticleEntity failedArticle = new FailedArticleEntity().fromSaveString(line);
                if (failedArticle != null) {
                    failedArticles.add(failedArticle);
                }
            }
            this.save(failedArticles);
            FileUtils.deleteQuietly(file);
        } catch (IOException e) {
            LOG.debug("No records are there in fail-safe to save ", ExceptionUtils.getRootCauseMessage(e));
        } catch (Exception e) {
            LOG.error("Could not persist file data to DB ", ExceptionUtils.getRootCauseMessage(e));
        }
    }


    /**
     * This method save the articles that were failed while deleting from {@link FailedArticleEntity}
     * first time and was saved to the file.
     * <p>
     * This method, reads the data from file and once all the data is deleted, delete the file.
     * <p>
     * This method runs asynchronously (In the background)
     *
     * <b>IF, file is not present, that means there are no failed records to procees and nothing to worry.</b>
     *
     * @param publication publication,for which the service is executing
     * @param resource    location of resource.
     */
    @Async
    default void deleteFromFile(String resource, String publication) {
        LOG.debug("deleting data from file : {} {} {}", resource, publication, Thread.currentThread().getName());
        try {
            File file = new File(resource.concat(publication)
                    .concat(Constants.FOLDER_SEPARATOR + DELETE_FAIL_SAFE_FILE));
            List<String> lines = FileUtils.readLines(file, Constants.STRING_ENCODING);

            Set<FailedArticleEntity> failedArticles = new HashSet<>();
            for (String line : lines) {
                FailedArticleEntity failedArticle = new FailedArticleEntity().fromDeleteString(line);
                if (failedArticle != null) {
                    failedArticles.add(failedArticle);
                }
            }
            this.delete(failedArticles);
            // Once proceed all the articles successfully, delete the file.
            FileUtils.deleteQuietly(file);
        } catch (IOException e) {
            LOG.debug("No records are there in fail-safe to delete ", ExceptionUtils.getRootCauseMessage(e));
        } catch (Exception e) {
            LOG.error("Could not persist file data to DB ", ExceptionUtils.getRootCauseMessage(e));
        }
    }
}
