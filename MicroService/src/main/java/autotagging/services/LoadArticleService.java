package de.funkedigital.autotagging.services;

import de.funkedigital.autotagging.entities.repo.ExecutedArticleEntity;
import de.funkedigital.autotagging.entities.repo.PendingArticleEntity;
import de.funkedigital.autotagging.entities.repo.SitemapEntity;
import de.funkedigital.autotagging.entities.xml.Sitemap;
import de.funkedigital.autotagging.entities.xml.SitemapIndex;
import de.funkedigital.autotagging.entities.xml.Url;
import de.funkedigital.autotagging.entities.xml.UrlSet;
import de.funkedigital.autotagging.jobs.SchedulerJob;
import de.funkedigital.autotagging.repositories.ExecutedArticleRepository;
import de.funkedigital.autotagging.repositories.FailedArticleRepository;
import de.funkedigital.autotagging.repositories.PendingArticleRepository;
import de.funkedigital.autotagging.repositories.SitemapRepository;
import de.funkedigital.autotagging.services.interfaces.ServiceInterface;
import de.funkedigital.autotagging.utils.JaxbUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * This class is to load articles to {@link PendingArticleRepository} after fetcjing the Urls
 * from Archive.xml
 * <p>
 * It fetches all the files from Archive.xml save them aone at every 6 hours ans save the data
 * to {@link PendingArticleRepository},
 * <p>
 * This service is separate from other services as all the loading part (Heavy work) is done by
 * this service and thus keeps processing separated from data loading
 *
 * @author sraj
 */
@Service
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LoadArticleService implements ServiceInterface {

    private static final Logger LOG = LoggerFactory.getLogger(LoadArticleService.class);

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
     * Autowired instance of {@link ExecutedArticleRepository}
     */
    @Autowired
    private ExecutedArticleRepository executedArticleRepository;

    /**
     * Autowired instance of {@link SitemapRepository}
     */
    @Autowired
    private SitemapRepository sitemapRepository;

    /**
     * Path of resource folder
     */
    @Autowired
    private String resourcePath;

    // Properties
    private String filename;

    private String publication;

    /**
     * This method is to set the property, ie publication.
     * As we have publication specific properties, diff files for
     * diff publications.
     *
     * @param fileLocation the URL of archive.xml
     * @param publication  publicaion the service is loaded
     */
    public void setProperties(String fileLocation, String publication) {
        this.filename = fileLocation;
        this.publication = publication;
    }

    /**
     * This method is triggered on schedule : after 6 hours of completion of previous run.
     * <p>
     * -> Get the top record from {@link SitemapEntity}
     * ----> If Fails:
     * ----------> DB might be down, Log the error an try again
     * ----> If Success:
     * ----------> Check if there are any record already in the store. {@link SitemapEntity}
     * ------------> IF NO:
     * -------------> Fetch the list of gzip files from archive.xml ans save them to {@link SitemapEntity}
     * --------------------> If fails:
     * -------------------------> Try every 5 minutes WHILE its successful
     * ------------> IF YES:
     * -------------> Save the data from fail-safe file to {@link ExecutedArticleEntity} and {@link FailedArticleRepository}
     * --------------------> fetch the data from GZIP file and map them to Objects using Jaxb.
     * --------------------> Check for url in {@link ExecutedArticleEntity} and {@link FailedArticleRepository}, if present
     * --------------------> IF, not present:
     * -------------------------->Save the articles along with asset ID in {@link PendingArticleEntity}
     * --------------------------> If Success:
     * ------------------------------> Mark the file as executed in {@link SitemapEntity}
     * --------------------------> If Fails:
     * -------------------------------> Log the error and try again after 6 hours again.
     * <p>
     * This method can be triggered from {@link de.funkedigital.autotagging.scheduler.ScheduleManager#start(SchedulerJob)}
     * and the future runs can be cancelled {@link de.funkedigital.autotagging.scheduler.ScheduleManager#stop(SchedulerJob)}
     * <p>
     * There is {@link de.funkedigital.autotagging.controllers.ExecutionController} to start and stop the execution.
     */
    @Override
    public void run() {
        LOG.info("Executing run() {} : {} : {}", this.publication, this.filename, Thread.currentThread().getName());
        try {
            SitemapEntity sitemapEntity = sitemapRepository.findTopSitemap(this.publication);
            while (sitemapEntity == null) {
                try {
                    // Load the archive file and load its content to DB
                    SitemapIndex index = JaxbUtils.getXmlEntityFromUrl(this.filename, SitemapIndex.class);
                    List<SitemapEntity> sitemapEntities = new ArrayList<>();
                    for (Sitemap sitemap : index.getSitemaps()) {
                        // sitemap-section check is to avoid loading section articles.
                        if (!StringUtils.isEmpty(sitemap.getLoc()) && !(sitemap.getLoc().toLowerCase().contains("sitemap-section"))) {
                            sitemapEntities.add(new SitemapEntity(this.publication, sitemap.getLoc(), sitemap.getLastmod(),
                                    Boolean.FALSE));
                        }
                    }
                    // Save the sitemap urls to Sitemap Repository.
                    sitemapRepository.save(sitemapEntities);
                } catch (Exception e) {
                    LOG.error("Error occurred while loading data to sitemap repository for {} : {} : {}"
                            , this.publication, this.filename, ExceptionUtils.getRootCauseMessage(e), e);
                    // wait for 5 minute before trying to fetch the sitemaps again.
                    Thread.sleep(1000 * 60 * 5);
                } finally {
                    // fetch the top result after populating the store.
                    sitemapEntity = sitemapRepository.findTopSitemap(this.publication);
                }
            }
            // process records from fail safe file before any further processing.
            executeFailSafe();
            // ZIP files are stored in archive sitemaps and to process them we use Gzip and
            // Then jaxb to map the extracted xml file to objects.
            try (InputStreamReader stream =
                         new InputStreamReader(
                                 new GZIPInputStream(
                                         new URL(sitemapEntity.getUrl()).openStream()))) {
                //Populate xml.
                UrlSet urlSet = JaxbUtils.getXmlEntityFromStream(stream, UrlSet.class);
                // Populate pending list
                Set<PendingArticleEntity> pendingArticles = new HashSet<>();
                for (Url url : urlSet.getUrls()) {
                    String urlLoc = url.getLoc();
                    if (isEligibleForProcessing(urlLoc)) {
                        pendingArticles.add(new PendingArticleEntity(publication, urlLoc));
                    }
                }
                // Save all the records to pending list
                pendingArticleRepository.save(pendingArticles);
                // Change the status of executed record and change the status.
                sitemapRepository.markExecuted(sitemapEntity);
            } catch (Exception e) {
                LOG.error("Error occurred while loading data to pendingArticle repository for {}, {} , {}"
                        , this.publication, this.filename, ExceptionUtils.getRootCauseMessage(e), e);
            }
        } catch (Throwable th) {
            // This situation should only happen when the fetching of record fails from Pending articles.
            // Which means DB is down.
            LOG.error("Error occurred for {}, look for -: file is present {} or DB is up {} : {}",
                    this.publication, this.filename, "localhost:8080/h2-console",
                    ExceptionUtils.getRootCauseMessage(th), th);
        }
    }

    /**
     * Before Saving the articles to {@link PendingArticleEntity}, we check whether that
     * article is valid article url and was already processed by checking the article in
     * {@link ExecutedArticleEntity}, {@link FailedArticleRepository}, {@link PendingArticleRepository}
     * to avoid processing wrong url and duplication.
     *
     * @param url Url to be processed
     * @return Whether the article present in {@link ExecutedArticleEntity},
     * {@link FailedArticleRepository} and {@link PendingArticleRepository}
     */
    private boolean isEligibleForProcessing(String url) {

        boolean isValidArticleUrl = (url != null && url.toLowerCase().contains("article"));
        // Don't check further if url is not article URL.
        if (isValidArticleUrl) {
            boolean isPending = pendingArticleRepository.findByPublicationAndUrl(this.publication, url);
            boolean isExecuted = executedArticleRepository.findByPublicationAndUrl(this.publication, url);
            boolean isFailed = failedArticleRepository.findByPublicationAndUrl(this.publication, url);
            return (!isPending && !isExecuted && !isFailed);
        }
        return isValidArticleUrl;
    }


    /**
     * This method is to load failed articles from Fail-safe files to database before
     * processing next record.
     * <p>
     * it takes care of records failed while being processed previously.
     */
    private void executeFailSafe() {
        LOG.debug("Executing executeFailSafe() {} : {} : {}", this.resourcePath, this.publication,
                Thread.currentThread().getName());
        // Save articles.
        executedArticleRepository.saveFromFile(this.resourcePath, publication);
        failedArticleRepository.saveFromFile(this.resourcePath, publication);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "LoadArticleService{" +
                "publication='" + publication + '\'' +
                '}';
    }
}
