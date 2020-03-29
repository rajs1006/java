package de.funkedigital.autotagging.entities.repo;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "sitemaps")
public class SitemapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "publication")
    private String publication;

    @Column(name = "url")
    private String url;

    @Column(name = "last_mod_date")
    private String lastModDate;

    @Column(name = "execution_status")
    private Boolean executionStatus;

    @Column(name = "load_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date siteMapLoadDate;

    public SitemapEntity() {

    }

    public SitemapEntity(String publication, String url, String lastModDate, Boolean executionStatus) {
        this.publication = publication;
        this.url = url;
        this.lastModDate = lastModDate;
        this.executionStatus = executionStatus;
        this.siteMapLoadDate = Calendar.getInstance().getTime();
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

    public void setPublication(String publication) {
        this.publication = publication;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setLastModDate(String lastModDate) {
        this.lastModDate = lastModDate;
    }

    public void setExecutionStatus(Boolean executionStatus) {
        this.executionStatus = executionStatus;
    }

    public Boolean getExecutionStatus() {
        return executionStatus;
    }

    public Date getSiteMapLoadDate() {
        return siteMapLoadDate;
    }

    public void setSiteMapLoadDate(Date siteMapLoadDate) {
        this.siteMapLoadDate = siteMapLoadDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof SitemapEntity)) return false;

        SitemapEntity that = (SitemapEntity) o;

        return new EqualsBuilder()
                .append(publication, that.publication)
                .append(url, that.url)
                .append(lastModDate, that.lastModDate)
                .append(executionStatus, that.executionStatus)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(publication)
                .append(url)
                .append(lastModDate)
                .append(executionStatus)
                .toHashCode();
    }
}
