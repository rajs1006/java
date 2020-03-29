package de.funkedigital.autotagging.JUnits;

import de.funkedigital.autotagging.escenic.services.EscenicService;
import de.funkedigital.autotagging.entities.repo.FailedArticleEntity;
import de.funkedigital.autotagging.repositories.ExecutedArticleRepository;
import de.funkedigital.autotagging.repositories.FailedArticleRepository;
import de.funkedigital.autotagging.repositories.PendingArticleRepository;
import de.funkedigital.autotagging.semantic.services.SemanticService;
import de.funkedigital.autotagging.semantic.services.UnicornService;
import de.funkedigital.autotagging.services.FailedArticleService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
public class FailedArticlesServiceTest {


    private static final Logger log = LoggerFactory.getLogger(FailedArticlesServiceTest.class);

    @Mock
    private ExecutedArticleRepository executedArticleRepository;

    @Mock
    private PendingArticleRepository pendingArticleRepository;

    @Mock
    private FailedArticleRepository failedArticleRepository;

    @Mock
    private SemanticService semanticService;

    @Mock
    private UnicornService unicornService;

    @Mock
    private EscenicService escenicService;

    @InjectMocks
    private FailedArticleService failedArticleService = new FailedArticleService();

    Map<String, FailedArticleEntity> failedArticleEntityRepository;

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

        failedArticleEntityRepository = new HashMap<>();

        failedArticleEntityRepository.put(publications.get(0), new FailedArticleEntity(publications.get(0), "/hao/"));
        failedArticleEntityRepository.put(publications.get(1), new FailedArticleEntity(publications.get(1), "/bmo/"));
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
            when(failedArticleRepository.findTopArticle(pub))
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


    private void mock(String pub) {
        FailedArticleEntity failedArticle = failedArticleEntityRepository.get(pub);

        when(failedArticleRepository.findTopArticle(pub))
                .thenReturn(failedArticle);

        when(unicornService.analyzeArticle(failedArticle.getUrl())).thenReturn(anyObject());

        when(semanticService.returnKeywords("asset - "+pub)).thenReturn(anyObject());

        doAnswer((i) -> {
            log.info("escenicService push is success");
            return null;
        }).when(escenicService).pushKeywords(keyword, anyString());

        doAnswer((i) -> {
            log.info("executedArticleRepository save is success");
            return null;
        }).when(executedArticleRepository).save(failedArticle.getPublication(),
                "asset - "+pub, failedArticle.getUrl());

        doAnswer((i) -> {
            log.info("failedArticleRepository save is success");
            return null;
        }).when(failedArticleRepository).save(failedArticle.getPublication(), failedArticle.getUrl());

        doAnswer((i) -> {
            log.info("failedArticleRepository delete is success");
            return null;
        }).when(failedArticleRepository).delete(failedArticle);
    }

    private void runService(String pub) {
        failedArticleService.setProperties(pub);
        failedArticleService.run();
    }

}
