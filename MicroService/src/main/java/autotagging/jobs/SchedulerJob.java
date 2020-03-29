package de.funkedigital.autotagging.jobs;

import de.funkedigital.autotagging.services.interfaces.ServiceInterface;
import de.funkedigital.autotagging.triggers.ServiceTrigger;

import java.util.concurrent.ScheduledFuture;

public class SchedulerJob {

    private ServiceInterface service;

    private ServiceTrigger trigger;

    private ScheduledFuture<?> future;

    public SchedulerJob(ServiceInterface service, ServiceTrigger trigger) {
        this.service = service;
        this.trigger = trigger;
    }

    public ServiceInterface getService() {
        return service;
    }

    public ServiceTrigger getTrigger() {
        return trigger;
    }

    public ScheduledFuture<?> getFuture() {
        return future;
    }

    public void setFuture(ScheduledFuture<?> future) {
        this.future = future;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "SchedulerJob{" +
                "service=" + service.toString() +
                ", trigger=" + trigger.toString() +
                '}';
    }
}

