package de.funkedigital.autotagging.JUnits;

import de.funkedigital.autotagging.escenic.services.EscenicService;
import de.funkedigital.autotagging.entities.repo.PendingArticleEntity;
import de.funkedigital.autotagging.repositories.ExecutedArticleRepository;
import de.funkedigital.autotagging.repositories.FailedArticleRepository;
import de.funkedigital.autotagging.repositories.PendingArticleRepository;
import de.funkedigital.autotagging.semantic.services.SemanticService;
import de.funkedigital.autotagging.services.PendingArticleService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@SpringBootTest
public class PendingArticlesServiceTest {


    private static final Logger log = LoggerFactory.getLogger(PendingArticlesServiceTest.class);

    @Mock
    private ExecutedArticleRepository executedArticleRepository;

    @Mock
    private PendingArticleRepository pendingArticleRepository;

    @Mock
    private FailedArticleRepository failedArticleRepository;

    @Mock
    private SemanticService semanticService;

    @Mock
    private EscenicService escenicService;

    @InjectMocks
    private PendingArticleService pendingArticleService = new PendingArticleService();

    Map<String, PendingArticleEntity> pendingArticleEntityRepository;

    List<String> publications;

    String keyword;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        keyword = "KeywordStore";

        publications = new ArrayList<String>() {{
            add("hao");
            add("bmo");
        }};

        pendingArticleEntityRepository = new HashMap<>();

        pendingArticleEntityRepository.put(publications.get(0), new PendingArticleEntity(publications.get(0), "/hao/"));
        pendingArticleEntityRepository.put(publications.get(1), new PendingArticleEntity(publications.get(1), "/bmo/"));
    }

    @Test
    public void testSuccess() {
        for (String pub : publications) {
            mock(pub);
            runService(pub);
        }
    }

    @Test
    public void testFailure1() {
        for (String pub : publications) {
            mock(pub);
            when(pendingArticleRepository.findTopArticle(pub))
                    .thenThrow(new RuntimeException("Mocked DB down exception"));
            runService(pub);
        }
    }

    @Test
    public void testFailure2() {
        for (String pub : publications) {
            mock(pub);
            when(semanticService.returnKeywords(anyString()))
                    .thenThrow(new RuntimeException("Semantic service failed"));
            runService(pub);
        }
    }

    @Test
    public void testFailure3() {
        for (String pub : publications) {
            mock(pub);
            when(semanticService.returnKeywords(anyString()))
                    .thenThrow(new RuntimeException("Semantic service failed"));
            doAnswer((i) -> {
                throw new RuntimeException("failedArticleRepository save is failed");
            }).when(failedArticleRepository).save(anyString(), anyString());

            runService(pub);
        }
    }

    @Test
    public void testFailure4() {
        for (String pub : publications) {
            mock(pub);
            when(semanticService.returnKeywords(anyString()))
                    .thenThrow(new RuntimeException("Semantic service failed"));

            doAnswer((i) -> {
                throw new RuntimeException("failedArticleRepository save is failed");
            }).when(failedArticleRepository).save(anyString(), anyString());

            doAnswer((i) -> {
                throw new RuntimeException("pendingArticleRepository delete is failed");
            }).when(pendingArticleRepository).delete(Mockito.any(PendingArticleEntity.class));

            runService(pub);
        }
    }

    private void mock(String pub) {
        PendingArticleEntity pendingArticle = pendingArticleEntityRepository.get(pub);

        when(pendingArticleRepository.findTopArticle(pub))
                .thenReturn(pendingArticle);

        when(semanticService.returnKeywords(pendingArticle.getUrl())).thenReturn(anyObject());

        doAnswer((i) -> {
            log.info("escenicService push is success");
            return null;
        }).when(escenicService).pushKeywords(keyword, anyString());

        doAnswer((i) -> {
            log.info("executedArticleRepository save is success");
            return null;
        }).when(executedArticleRepository).save(pendingArticle.getPublication(),
                pendingArticle.getUrl(), pendingArticle.getUrl());

        doAnswer((i) -> {
            log.info("failedArticleRepository save is success");
            return null;
        }).when(failedArticleRepository).save(pendingArticle.getPublication(), pendingArticle.getUrl());

        doAnswer((i) -> {
            log.info("pendingArticleRepository delete is success");
            return null;
        }).when(pendingArticleRepository).delete(pendingArticle);
    }

    private void runService(String pub) {
        pendingArticleService.setProperties(pub);
        pendingArticleService.run();
    }

}
