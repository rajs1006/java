package de.funkedigital.autotagging.triggers;

import de.funkedigital.autotagging.services.interfaces.ServiceInterface;
import de.funkedigital.autotagging.utils.Utils;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;

import java.util.Calendar;
import java.util.Date;

/**
 * This is trigger to run service bean {@link de.funkedigital.autotagging.services.LoadArticleService}
 */
public class ServiceTrigger implements Trigger {

    /**
     * Time interval to run {@link ServiceInterface#run()} after completion of last run
     */
    private int interval;

    public ServiceTrigger(int interval) {
        this.interval = interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date nextExecutionTime(TriggerContext triggerContext) {
        return Utils.getNextExecution(triggerContext.lastScheduledExecutionTime(), Calendar.SECOND, this.interval);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ServiceTrigger{" +
                "interval=" + interval +
                '}';
    }
}
