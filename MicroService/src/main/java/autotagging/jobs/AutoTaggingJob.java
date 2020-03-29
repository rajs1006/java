package de.funkedigital.autotagging.jobs;

import de.funkedigital.autotagging.services.AutoTaggingServiceInterface;
import org.springframework.scheduling.Trigger;

import java.util.concurrent.ScheduledFuture;

public class AutoTaggingJob {

    private AutoTaggingServiceInterface service;

    private Trigger trigger;

    private ScheduledFuture<?> future;

    public AutoTaggingJob(AutoTaggingServiceInterface service, Trigger trigger) {
        this.service = service;
        this.trigger = trigger;
    }

    public AutoTaggingServiceInterface getService() {
        return service;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public ScheduledFuture<?> getFuture() {
        return future;
    }

    public void setFuture(ScheduledFuture<?> future) {
        this.future = future;
    }
}

