package de.funkedigital.autotagging.services;


import de.funkedigital.autotagging.repositories.ExecutedArticleRepository;
import de.funkedigital.autotagging.repositories.FailedArticleRepository;
import de.funkedigital.autotagging.semantic.services.SemanticAutoTaggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Configuration
@EnableAsync
@Service
public class AutoTaggingWebService {

    private static final Logger LOG = LoggerFactory.getLogger(AutoTaggingWebService.class);
    /**
     * Constructor based Autowired SemanticAutoTaggingService, using {@link SemanticAutoTaggingService}
     */
    private final SemanticAutoTaggingService service;

    @Autowired
    private ExecutedArticleRepository executedArticleRepository;

    @Autowired
    private FailedArticleRepository failedArticleRepository;

    @Autowired
    public AutoTaggingWebService(SemanticAutoTaggingService service) {
        this.service = service;
    }

    public String returnKeywords(String publication, Long articleId) {
        try {
            return this.service.returnKeywords(articleId);
        } catch (Throwable th) {
            try {
                throw th;
            } finally {
                failedArticleRepository.save(publication, articleId);
            }
        } finally {
            executedArticleRepository.save(publication, articleId);
        }
    }
}
