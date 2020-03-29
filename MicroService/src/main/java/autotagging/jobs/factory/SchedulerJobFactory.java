package de.funkedigital.autotagging.jobs.factory;


import de.funkedigital.autotagging.jobs.SchedulerJob;
import de.funkedigital.autotagging.services.interfaces.ServiceInterface;
import de.funkedigital.autotagging.triggers.ServiceTrigger;
import org.springframework.beans.factory.FactoryBean;

/**
 * This class is used to create prototype instance of {@link SchedulerJob}
 *
 * @author sraj
 */
public class SchedulerJobFactory implements FactoryBean<SchedulerJob> {

    /**
     * instance of chiled class of {@link ServiceInterface}, used to create the instance
     * {@link SchedulerJob}
     * <p>
     * This child class is used to schedule the {@link #service}
     */
    private ServiceInterface service;

    /**
     * instance of child class of {@link ServiceTrigger}, used to create the instance
     * {@link SchedulerJob}
     * <p>
     * This child class is used to trigger the {@link #service}
     */
    private ServiceTrigger trigger;

    public SchedulerJobFactory(ServiceInterface service, ServiceTrigger trigger) {
        this.service = service;
        this.trigger = trigger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SchedulerJob getObject() {
        return new SchedulerJob(service, trigger);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSingleton() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getObjectType() {
        return SchedulerJob.class;
    }
}
