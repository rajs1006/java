package de.funkedigital.autotagging.repositories;

import de.funkedigital.autotagging.entities.repo.SitemapEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * This repository class is to interact with {@link SitemapRepository}
 *
 * @author sraj
 */
@Repository
public interface SitemapRepository extends CrudRepository<SitemapEntity, Long> {

    Logger LOG = LoggerFactory.getLogger(SitemapRepository.class);

    /**
     * Find the top record from {@link SitemapEntity} given publication and execution_status as false
     *
     * @param publication publication of execution
     * @return {@link SitemapEntity}
     */
    @Query(nativeQuery = true, value = "SELECT TOP 1 * from SITEMAPS where publication = :publication " +
            "and execution_status = false")
    SitemapEntity findTopSitemap(@Param("publication") String publication);

    /**
     * Get all the records given  publication
     *
     * @param publication publication for query
     * @return List of {@link SitemapEntity}
     */
    @Query(nativeQuery = true, value = "SELECT * from SITEMAPS WHERE publication = :publication")
    List<SitemapEntity> findAllByPublication(@Param("publication") String publication);

    /**
     * Get all the records given  publication and regex of url
     *
     * @param publication publication for query
     * @param url         regex for url
     * @return List of {@link SitemapEntity}
     */
    @Query(nativeQuery = true, value = "SELECT * from SITEMAPS WHERE publication = :publication AND url like :url")
    List<SitemapEntity> findAllByPublicationAndUrl(@Param("publication") String publication,
                                                   @Param("url") String url);


    /**
     * This method is to mark the Status of Sitemap if all the records been saved to
     * {@link de.funkedigital.autotagging.entities.repo.PendingArticleEntity} successfully.
     *
     * @param sitemapEntity entity to mark as executed.
     */
    default void markExecuted(SitemapEntity sitemapEntity) {
        LOG.debug("Saving sitemap : {} {} {}", sitemapEntity.getPublication(), sitemapEntity.getUrl(),
                Thread.currentThread().getName());
        sitemapEntity.setExecutionStatus(Boolean.TRUE);
        this.save(sitemapEntity);
    }
}
