package de.funkedigital.autotagging.repositories;

import de.funkedigital.autotagging.entities.repo.PendingArticleEntity;
import de.funkedigital.autotagging.exceptions.RepositoryException;
import de.funkedigital.autotagging.utils.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is to save,delete records in {@link PendingArticleRepository}, if they fail to be processed
 * because of any reason execute save them to a file and also bundle other queries.
 * <p>
 * This class performs a fail safe operation when the {@link #delete(PendingArticleEntity, Long...)}
 * operation fails, save those records in a file.Mainly the reason for such failure is
 * DB down. which is very unlikely.
 * <p>
 * For other exceptions it handle the situation differently. Look for the method documentation.
 *
 * @author sraj
 */
@Repository
public interface PendingArticleRepository extends CrudRepository<PendingArticleEntity, Long> {

    Logger LOG = LoggerFactory.getLogger(PendingArticleRepository.class);

    String DELETE_FAIL_SAFE_FILE = "pending-delete-articles.txt";

    /**
     * It gets the top article from {@link PendingArticleEntity}
     *
     * @param publication using publication as parameter
     * @return {@link PendingArticleEntity} containing top data from table
     */
    @Query(nativeQuery = true, value = "SELECT TOP 1 * from PENDING_ARTICLES where publication = :publication")
    PendingArticleEntity findTopArticle(@Param("publication") String publication);


    /**
     * This method fires a query to find the record if already present in the {@link PendingArticleEntity}
     *
     * @param publication publication,for which the service is executing
     * @param url         url to be processed
     * @return Boolean, true , if record is present
     */
    @Query(nativeQuery = true, value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END from " +
            "PENDING_ARTICLES WHERE publication = :publication AND url = :url ")
    boolean findByPublicationAndUrl(@Param("publication") String publication,
                                    @Param("url") String url);

    /**
     * This method returns the count of records for current month. and helps keep the
     * record of processed records.
     *
     * @return Count of records of current month
     */
    @Query(nativeQuery = true, value = "select count(*) from PENDING_ARTICLES " +
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
            "PENDING_ARTICLES WHERE publication = :publication AND url like :url ")
    List<PendingArticleEntity> findAllByPublicationAndUrl(@Param("publication") String publication,
                                                          @Param("url") String url);

    /**
     * Get all the records given  publication
     *
     * @param publication publication for query
     * @return List of {@link PendingArticleEntity}
     */
    @Query(nativeQuery = true, value = "SELECT * from PENDING_ARTICLES WHERE publication = :publication")
    List<PendingArticleEntity> findAllByPublication(@Param("publication") String publication);


    /**
     * This method, takes argument {@link PendingArticleEntity} or {@link Long[]} and deletes them.
     * <p>
     * IF, deletion fails because of {@link EmptyResultDataAccessException}, that means data is not available
     * to delete, it does nothing. As it is ok that data is not available.
     *
     * @param pendingArticle    {@link PendingArticleEntity} to delete
     * @param pendingArticleIds {@link Long[]} as variable argument to delete by failedArticleId
     */
    default void delete(@NotNull PendingArticleEntity pendingArticle, Long... pendingArticleIds) {
        if (pendingArticle != null) {
            LOG.debug("Deleting article :{} {} {}", pendingArticle.getPublication(), pendingArticle.getUrl(),
                    Thread.currentThread().getName());
            try {
                if (!ArrayUtils.isEmpty(pendingArticleIds)) {
                    for (Long pendingArticleId : pendingArticleIds) {
                        try {
                            this.delete(pendingArticleId);
                        } catch (ConstraintViolationException | EmptyResultDataAccessException ce) {
                            LOG.warn("Constraint violation exception, record already deleted from DB : {}",
                                    pendingArticleId,
                                    ExceptionUtils.getRootCauseMessage(ce));
                        }
                    }
                } else {
                    this.delete(pendingArticle);
                }
            } catch (ConstraintViolationException | EmptyResultDataAccessException ce) {
                LOG.warn("Constraint violation exception, record already deleted from DB : {}", pendingArticle.toString(),
                        ExceptionUtils.getRootCauseMessage(ce));
            } catch (Exception e) {
                throw new RepositoryException(String.format("PendingArticleEntity deletion failed for %s \t %s"
                        , pendingArticle.getPublication(), e.getMessage())
                        , e
                        , String.format("%s/%s", pendingArticle.getPublication(), this.DELETE_FAIL_SAFE_FILE)
                        , pendingArticle.toString());
            }
        }
    }

    /**
     * This method save the articles that were failed while deleting from {@link PendingArticleEntity}
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
        LOG.debug("Deleting data from file : {} {} {}", resource, publication, Thread.currentThread().getName());
        try {
            File file = new File(resource.concat(publication)
                    .concat(Constants.FOLDER_SEPARATOR + DELETE_FAIL_SAFE_FILE));
            List<String> lines = FileUtils.readLines(file, Constants.STRING_ENCODING);

            Set<PendingArticleEntity> pendingArticles = new HashSet<>();
            for (String line : lines) {
                PendingArticleEntity pendingArticle = new PendingArticleEntity().fromString(line);
                if (pendingArticle != null) {
                    pendingArticles.add(pendingArticle);
                }
            }
            this.delete(pendingArticles);
            // Once proceed all the articles succesfully, delete the file.
            FileUtils.deleteQuietly(file);
        } catch (IOException e) {
            LOG.debug("No records are there in fail-safe to delete ", e.getMessage());
        } catch (Exception e) {
            LOG.error("Could not persist file data to DB ", e.getMessage());
        }
    }
}
