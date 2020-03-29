package de.funkedigital.autotagging.entities.web;

import java.util.List;

public class DetailForm {


    private List<Detail> details;

    public DetailForm(){

    }

    public DetailForm(List<Detail> details) {
        this.details = details;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public void setDetails(List<Detail> details) {
        this.details = details;
    }
}
