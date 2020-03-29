package de.funkedigital.autotagging.scheduler;

import de.funkedigital.autotagging.jobs.AutoTaggingJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;


@Service
public class AutoTaggingScheduleManager {

    private static final Logger log = LoggerFactory.getLogger(AutoTaggingScheduleManager.class);

    @Autowired
    private AutoTaggingTaskScheduler scheduler;

    public void start(AutoTaggingJob job) {
        ScheduledFuture<?> future = scheduler.schedule(
                () -> job.getService().run(), job.getTrigger()
        );
        job.setFuture(future);
    }

    public void stop(AutoTaggingJob job) {
        if (job.getFuture() != null) {
            scheduler.cancel(job.getFuture());
        }
    }
}
