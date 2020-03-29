package de.funkedigital.autotagging.controllers;

import de.funkedigital.autotagging.entities.AutoTagging;
import de.funkedigital.autotagging.jobs.AutoTaggingJob;
import de.funkedigital.autotagging.repositories.AutoTaggingRepository;
import de.funkedigital.autotagging.repositories.PendingArticleRepository;
import de.funkedigital.autotagging.scheduler.AutoTaggingScheduleManager;
import de.funkedigital.autotagging.services.AutoTaggingSchedulerService;
import de.funkedigital.autotagging.triggers.AutoTaggingTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class AutoTaggingSchedulerController {

    private static final Logger log = LoggerFactory.getLogger(AutoTaggingSchedulerController.class);

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



    /**
     * @return
     */
    @Bean(name = "scheduledServices")
    public Map<String, AutoTaggingJob> scheduledServices(ApplicationContext ctx) {
        Map<String, AutoTaggingJob> services = new HashMap<>();

        Iterable<AutoTagging> configurations = autoTaggingRepository.findAll();
        for (AutoTagging autoTagging : configurations) {

            AutoTaggingSchedulerService service = ctx.getBean(AutoTaggingSchedulerService.class);
            service.setProperties(autoTagging.getPublication());

            AutoTaggingJob job = new AutoTaggingJob(service, new AutoTaggingTrigger());

            if (autoTagging.getSchedule().getEnabled()) {
                scheduler.start(job);
            }
            services.put(autoTagging.getPublication(), job);
        }
        return services;
    }
}
