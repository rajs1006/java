package de.funkedigital.autotagging.entities.web;

import de.funkedigital.autotagging.utils.Utils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class Sitemaps {

    private Long id;

    private String publication;

    private String url;

    private String lastModDate;

    private Boolean executionStatus;

    private String siteMapLoadDate;

    public Sitemaps(Long id, String publication, String url, String lastModDate, Boolean executionStatus, Date siteMapLoadDate) {
        this.id = id;
        this.publication = publication;
        this.url = url;
        this.lastModDate = lastModDate;
        this.executionStatus = executionStatus;
        if (siteMapLoadDate != null) {
            this.siteMapLoadDate = Utils.getFormattedDate(siteMapLoadDate);
        } else {
            this.siteMapLoadDate = StringUtils.EMPTY;
        }
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

    public String getLastModDate() {
        return lastModDate;
    }

    public Boolean getExecutionStatus() {
        return executionStatus;
    }

    public String getSiteMapLoadDate() {
        return siteMapLoadDate;
    }
}
