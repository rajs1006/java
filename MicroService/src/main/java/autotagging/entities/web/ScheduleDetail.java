package de.funkedigital.autotagging.entities.web;

import de.funkedigital.autotagging.enums.ServiceEnum;

public class ScheduleDetail {

    private ServiceEnum serviceEnum;

    private int delay;

    private boolean enable;

    public ScheduleDetail(){

    }

    public ScheduleDetail(ServiceEnum serviceName, int delay, boolean enable) {
        this.serviceEnum = serviceName;
        this.delay = delay;
        this.enable = enable;
    }

    public void setServiceEnum(ServiceEnum serviceEnum) {
        this.serviceEnum = serviceEnum;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public ServiceEnum getServiceEnum() {
        return serviceEnum;
    }

    public int getDelay() {
        return delay;
    }

    public boolean isEnable() {
        return enable;
    }
}
