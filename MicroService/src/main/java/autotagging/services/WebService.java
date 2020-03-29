package de.funkedigital.autotagging.services;


import de.funkedigital.autotagging.escenic.services.EscenicService;
import de.funkedigital.autotagging.repositories.ExecutedArticleRepository;
import de.funkedigital.autotagging.repositories.FailedArticleRepository;
import de.funkedigital.autotagging.semantic.entities.KeywordStore;
import de.funkedigital.autotagging.semantic.entities.UnicornStore;
import de.funkedigital.autotagging.semantic.services.SemanticService;
import de.funkedigital.autotagging.semantic.services.UnicornService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service serves the Web request directly from Escenic Event listner
 *
 * @author sraj
 */
@Service
public class WebService {

    private static final Logger LOG = LoggerFactory.getLogger(WebService.class);
    /**
     * Autowired instance of  {@link ExecutedArticleRepository}
     */
    @Autowired
    private ExecutedArticleRepository executedArticleRepository;

    /**
     * Autowired instance of  {@link FailedArticleRepository}
     */
    @Autowired
    private FailedArticleRepository failedArticleRepository;

    /**
     * Constructor based Autowired SemanticAutoTaggingService, using {@link UnicornService}
     */

    private final UnicornService unicornService;

    /**
     * Constructor based Autowired SemanticAutoTaggingService, using {@link SemanticService}
     */
    private final SemanticService semanticService;

    /**
     * Constructor based Autowired of {@link EscenicService}
     */
    private EscenicService escenicService;


    /**
     * Constructor for Autowiring.
     */
    @Autowired
    public WebService(SemanticService semanticService, UnicornService unicornService, EscenicService escenicService) {
        this.semanticService = semanticService;
        this.unicornService = unicornService;
        this.escenicService = escenicService;
    }

    /**
     * In this method
     * ---> analyze article with {@link UnicornService#analyzeArticle(String)} and get AssetID
     * ---> Pass that AssetId to {@link SemanticService#returnKeywords(String)} to get KeywordStore.
     * ---> if success:
     * --------> Save article to {@link ExecutedArticleRepository}, Asynchronously
     * ---> if fails:
     * --------> Save article to {@link FailedArticleRepository}, Asynchronously
     * <p>
     * This method returns the keyword does not matter if article is already executed.
     * We need to put a constraint from calling function to avoid duplication.
     *
     * @param publication Publication of article URL
     * @param articleUrl  url of article to be processed.
     * @return Manipulated keyword in string format
     */
    public String returnKeywords(String publication, String articleUrl) {
        LOG.debug("Executing returnKeywords {} : {} : {}",
                publication, articleUrl, Thread.currentThread().getName());
        try {
            // Get Asset ID
            UnicornStore unicornStore = unicornService.analyzeArticle(articleUrl);
            // get keywords
            KeywordStore keywords = this.semanticService.returnKeywords(unicornStore.getAssetId());

            escenicService.pushKeywords(keywords.toString(), unicornStore.getArticleId());
            // Execute repository Asynchronously
            executedArticleRepository.save(publication, unicornStore.getAssetId(), articleUrl);
            return keywords.toString();
        } catch (Throwable th) {
            try {
                // Pass on the exception
                throw th;
            } finally {
                // Execute repository Asynchronously
                failedArticleRepository.save(publication, articleUrl);
            }
        }
    }
}
