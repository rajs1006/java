package de.funkedigital.autotagging.scheduler;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;

/**
 * This class extends spring inbuilt {@link ThreadPoolTaskScheduler} and use to
 * schedule and cancel the runs of Service component in class {@link ScheduleManager}
 */
@Service
public class Scheduler extends ThreadPoolTaskScheduler {

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
        ScheduledFuture<?> future = super.schedule(task, trigger);

        return future;
    }

    /**
     * This method used to cancel the future run
     *
     * @param future Future component of service to cancel.
     */
    public void cancel(ScheduledFuture<?> future) {
        future.cancel(true);
    }

}
