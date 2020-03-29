package de.funkedigital.autotagging.scheduler;

import de.funkedigital.autotagging.jobs.SchedulerJob;
import de.funkedigital.autotagging.services.ExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;


/**
 * This class controls (Start/stop) the execution of Services.
 *
 * @author sraj
 */
@Service
public class ScheduleManager {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleManager.class);

    /**
     * Autowired instance of {@link Scheduler}
     */
    @Autowired
    private Scheduler scheduler;

    /**
     * This method is used to start the execution of Service.
     * <p>
     * Way to use
     * ----> @Autowire
     * ----> {@link ScheduleManager} scheduleManager
     * ----> Inside method<----
     * ----> set the details in {@link SchedulerJob} schedulerJob
     * ----> call method : scheduleManager.start(schedulerJob)
     * <p>
     * for usage check {@link ExecutionService#getDetails()}
     *
     * @param job contains details of Service, Trigger and Future,
     *            to start the service execution
     */
    public void start(SchedulerJob job) {
        LOG.info("Service started {} : {}", job.toString(), Thread.currentThread().getName());
        ScheduledFuture<?> future = scheduler.schedule(
                () -> job.getService().run(), job.getTrigger()
        );
        job.setFuture(future);
    }

    /**
     * This method is used to stop the execution of Service.
     * <p>
     * Way to use
     * ----> @Autowire
     * ----> {@link ScheduleManager} scheduleManager
     * ----> Inside method<----
     * ----> set the details in {@link SchedulerJob} schedulerJob
     * ----> call method : scheduleManager.stop(schedulerJob)
     * <p>
     * for usage check {@link ExecutionService#getDetails()}
     *
     * @param job contains details of Service, Trigger and Future,
     *            to start the service execution
     */
    public void stop(SchedulerJob job) {
        LOG.info("Service stopped {} : {}", job.toString(), Thread.currentThread().getName());
        if (job.getFuture() != null) {
            scheduler.cancel(job.getFuture());
        }
    }
}
