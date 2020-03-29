package de.funkedigital.autotagging.entities.web;

import java.util.List;

public class Detail {

    private Long id;

    private String publication;

    private String url;

    private List<ScheduleDetail> scheduleDetails;

    public Detail(){

    }

    public Detail(Long id, String publication, String url
            , List<ScheduleDetail> scheduleDetails) {
        this.id = id;
        this.publication = publication;
        this.url = url;
        this.scheduleDetails = scheduleDetails;
    }

    public Long getId() {
        return id;
    }

    public String getPublication() {
        return publication;
    }

    public String getUrl() {
        return url;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPublication(String publication) {
        this.publication = publication;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setScheduleDetails(List<ScheduleDetail> scheduleDetails) {
        this.scheduleDetails = scheduleDetails;
    }

    public List<ScheduleDetail> getScheduleDetails() {
        return scheduleDetails;
    }
}
