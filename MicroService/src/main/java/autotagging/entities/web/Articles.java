package de.funkedigital.autotagging.entities.web;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

import static de.funkedigital.autotagging.utils.Utils.getFormattedDate;

public class Articles {

    private Long id;

    private String publication;

    private String assetId;

    private String url;

    private String executionDate;

    public Articles(Long id, String publication, String assetId, String url, Date executionDate) {
        this.id = id;
        this.publication = publication;
        this.assetId = assetId;
        this.url = url;
        if (executionDate != null) {
            this.executionDate = getFormattedDate(executionDate);
        } else {
            this.executionDate = StringUtils.EMPTY;
        }
    }

    public Articles(Long id, String publication, String url, Date executionDate) {
        this.id = id;
        this.publication = publication;
        this.url = url;
        if (executionDate != null) {
            this.executionDate = getFormattedDate(executionDate);
        } else {
            this.executionDate = StringUtils.EMPTY;
        }
    }

    public Long getId() {
        return id;
    }

    public String getPublication() {
        return publication;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getUrl() {
        return url;
    }

    public String getExecutionDate() {
        return executionDate;
    }

}
