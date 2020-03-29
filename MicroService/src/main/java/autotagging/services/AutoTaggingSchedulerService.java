package de.funkedigital.autotagging.services;

import de.funkedigital.autotagging.entities.PendingArticle;
import de.funkedigital.autotagging.repositories.ExecutedArticleRepository;
import de.funkedigital.autotagging.repositories.FailedArticleRepository;
import de.funkedigital.autotagging.repositories.PendingArticleRepository;
import de.funkedigital.autotagging.semantic.services.SemanticAutoTaggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AutoTaggingSchedulerService implements AutoTaggingServiceInterface {

    private static final Logger LOG = LoggerFactory.getLogger(AutoTaggingSchedulerService.class);

    @Autowired
    private ExecutedArticleRepository executedArticleRepository;

    @Autowired
    private PendingArticleRepository pendingArticleRepository;

    @Autowired
    private FailedArticleRepository failedArticleRepository;

    @Autowired
    private SemanticAutoTaggingService semanticAutoTaggingService;

    private String publication;

    public void setProperties(String publication) {
        this.publication = publication;
    }

    @Override
    public void run() {
        LOG.info("The time is now {} ", Thread.currentThread().getName());

        PendingArticle pendingArticle = pendingArticleRepository.findTopArticle(this.publication);
        if (pendingArticle != null) {
            Long articleId = pendingArticle.getArticleId();
            String publication = pendingArticle.getPublication();

            try {
                semanticAutoTaggingService.returnKeywords(articleId);
                executedArticleRepository.save(publication, articleId);
            } catch (Throwable th) {
                try {
                    LOG.error("Error occurred while processing {} : {}", publication, articleId);
                } finally {
                    failedArticleRepository.save(publication, articleId);
                }
            } finally {
                pendingArticleRepository.delete(pendingArticle);
            }
        } else {
            LOG.error("No pending records for processing...It will pick up records to process " +
                    "once PendingArticles table is populated.");
        }
    }

}
