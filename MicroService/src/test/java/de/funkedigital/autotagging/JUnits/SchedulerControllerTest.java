package de.funkedigital.autotagging.JUnits;

import de.funkedigital.autotagging.controllers.SchedulerController;
import de.funkedigital.autotagging.exceptions.AutoTaggingException;
import de.funkedigital.autotagging.jobs.SchedulerJob;
import de.funkedigital.autotagging.entities.repo.PropertyEntity;
import de.funkedigital.autotagging.entities.repo.PublicationEntity;
import de.funkedigital.autotagging.entities.repo.ScheduleEntity;
import de.funkedigital.autotagging.repositories.PublicationRepository;
import de.funkedigital.autotagging.scheduler.ScheduleManager;
import de.funkedigital.autotagging.scheduler.Scheduler;
import org.junit.Assert;
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
import java.util.List;

@SpringBootTest
public class SchedulerControllerTest {

    private static final Logger log = LoggerFactory.getLogger(SchedulerControllerTest.class);

    @InjectMocks
    private SchedulerController schedulerController = new SchedulerController();

    @Mock
    private ScheduleManager scheduleManager;

    @Mock
    private Scheduler scheduler;

    @Mock
    private PublicationRepository publicationRepository;

    private List<PublicationEntity> publicationEntityList;

    @Before
    public void setRepositoryMockOutput() {
        MockitoAnnotations.initMocks(this);
        publicationEntityList = getPublicationEntities();

        Mockito.doAnswer((i) -> {
            log.info("scheduleManager is loaded");
            SchedulerJob job = (SchedulerJob) i.getArguments()[0];
            scheduler.schedule(() -> job.getService().run(), job.getTrigger());
            return null;
        }).when(scheduleManager).start(Mockito.anyObject());

    }

    @Test
    public void testLoadArticleBeanSuccess() {
        Mockito.when(publicationRepository.findAll()).thenReturn(publicationEntityList);
        try {
//            Map<String, SchedulerJob> services = schedulerController.loadArticlesServices();
            Assert.assertEquals("service is not loaded properly ", 1, publicationEntityList.size());
        } catch (Exception e) {
            log.error("loadArticlesServices Bean failed", e);
        }
    }

    @Test(expected = AutoTaggingException.class)
    public void testLoadArticleBeanFail() {
        Mockito.when(publicationRepository.findAll()).thenThrow(new RuntimeException("Mocked exception"));
        //Map<String, SchedulerJob> services = schedulerController.loadArticlesServices();
    }


    public List<PublicationEntity> getPublicationEntities() {
        PublicationEntity publicationEntity1 = new PublicationEntity();
        publicationEntity1.setId(1L);
        publicationEntity1.setPublication("hao");


        ScheduleEntity scheduleEntity1 = new ScheduleEntity();
        scheduleEntity1.setId(1L);
        scheduleEntity1.setEnabled(true);

        publicationEntity1.setScheduleEntities(scheduleEntity1);

        PropertyEntity propertyEntity1 = new PropertyEntity();
        propertyEntity1.setId(1L);
        propertyEntity1.setSitemapFiles("/hao/articles.txt");

        publicationEntity1.setPropertyEntity(propertyEntity1);

        PublicationEntity publicationEntity2 = new PublicationEntity();
        publicationEntity2.setId(12L);
        publicationEntity2.setPublication("bmo");


        ScheduleEntity scheduleEntity2 = new ScheduleEntity();
        scheduleEntity2.setId(2L);
        scheduleEntity2.setEnabled(true);

        publicationEntity2.setScheduleEntities(scheduleEntity2);

        PropertyEntity propertyEntity2 = new PropertyEntity();
        propertyEntity2.setId(2L);
        propertyEntity2.setSitemapFiles("/bmo/articles.txt");

        publicationEntity2.setPropertyEntity(propertyEntity2);

        return new ArrayList<PublicationEntity>() {{
            add(publicationEntity1);
            add(publicationEntity2);
        }};
    }
}
