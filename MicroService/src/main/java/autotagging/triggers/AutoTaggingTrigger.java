package de.funkedigital.autotagging.triggers;

import de.funkedigital.autotagging.utils.Utils;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

@Component
public class AutoTaggingTrigger implements Trigger {


    @Override
    public Date nextExecutionTime(TriggerContext triggerContext) {
        return Utils.getNextExecution(triggerContext.lastScheduledExecutionTime(), Calendar.SECOND,1);
    }
}
