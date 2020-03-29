package de.funkedigital.autotagging.scheduler;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledFuture;

@Component
public class AutoTaggingTaskScheduler extends ThreadPoolTaskScheduler {


    public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
        ScheduledFuture<?> future = super.schedule(task, trigger);

        return future;
    }

    public void cancel(ScheduledFuture<?> future) {
        future.cancel(true);
    }

}
