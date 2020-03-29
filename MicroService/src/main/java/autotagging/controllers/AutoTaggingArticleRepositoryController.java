package de.funkedigital.autotagging.controllers;


import de.funkedigital.autotagging.entities.AutoTagging;
import de.funkedigital.autotagging.jobs.AutoTaggingJob;
import de.funkedigital.autotagging.repositories.AutoTaggingRepository;
import de.funkedigital.autotagging.scheduler.AutoTaggingScheduleManager;
import de.funkedigital.autotagging.services.AutoTaggingArticleRepositoryService;
import de.funkedigital.autotagging.triggers.AutoTaggingTrigger;
import de.funkedigital.autotagging.triggers.LoadArticleTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class AutoTaggingArticleRepositoryController {

    /**
     *
     */
    @Autowired
    private AutoTaggingScheduleManager scheduler;

    /**
     *
     */
    @Autowired
    private AutoTaggingRepository autoTaggingRepository;


    @Bean(name = "repositoryServices")
    public Map<String, AutoTaggingJob> repositoryServices(ApplicationContext ctx) {
        Map<String, AutoTaggingJob> services = new HashMap<>();

        Iterable<AutoTagging> configurations = autoTaggingRepository.findAll();
        for (AutoTagging autoTagging : configurations) {
            // Initializing bean using application context as using New keyword
            //to instantiate prototype bean leads to have null sigleton beans in prototype class.
            // repository was null in AutoTaggingArticleRepositoryService if initialize using New.
            AutoTaggingArticleRepositoryService service = ctx.getBean(AutoTaggingArticleRepositoryService.class);
            service.setProperties(autoTagging.getConfig().getSitemapFiles(),
                    autoTagging.getPublication());
            AutoTaggingJob job = new AutoTaggingJob(service, new LoadArticleTrigger());

            if (autoTagging.getSchedule().getEnabled()) {
                scheduler.start(job);
            }
            services.put(autoTagging.getPublication(), job);
        }
        return services;
    }
}
