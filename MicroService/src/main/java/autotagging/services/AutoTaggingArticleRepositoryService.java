package de.funkedigital.autotagging.services;

import de.funkedigital.autotagging.entities.PendingArticle;
import de.funkedigital.autotagging.repositories.PendingArticleRepository;
import de.funkedigital.autotagging.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AutoTaggingArticleRepositoryService implements AutoTaggingServiceInterface {

    private static final Logger LOG = LoggerFactory.getLogger(AutoTaggingArticleRepositoryService.class);

    private static final Character ESCAPE_CHAR = '\n';

    private static final Character LIMIT = 10;

    private int startChunk = 0;

    private String filename;

    private String publication;

    @Autowired
    private PendingArticleRepository pendingArticleRepository;

    public void setProperties(String fileLocation, String publication) {
        this.filename = fileLocation;
        this.publication = publication;
    }

    @Override
    public void run() {
        try {
            String file = this.getClass().getClassLoader().getResource(filename).getFile();
            Map<Integer, List<String>> chunks = Utils.readFileInChunks(file, ESCAPE_CHAR, startChunk, LIMIT);

            for (Map.Entry<Integer, List<String>> entry : chunks.entrySet()) {
                startChunk = entry.getKey();
                if (entry.getValue().size() != 0) {
                    List<PendingArticle> pendingArticles = new ArrayList<>();
                    for (String articleId : entry.getValue()) {
                        pendingArticles.add(new PendingArticle(Long.parseLong(articleId.trim()), publication));
                    }
                    pendingArticleRepository.save(pendingArticles);
                }
            }
        } catch (IOException ie) {
            LOG.error("Error occurred while accessing file {}", HttpStatus.EXPECTATION_FAILED, ie);
        } catch (Exception e) {
            LOG.error("Error occurred, look for {file is present or DB is up } ", e);
        }
    }
}
