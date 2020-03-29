package de.funkedigital.autotagging.entities.repo;

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
import java.util.Objects;

@Entity
@Table(name = "executed_articles")
public class ExecutedArticleEntity {

    private static final String separator = ",";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "publication")
    private String publication;

    @Column(name = "asset_id")
    private String assetId;

    @Column(name = "url")
    private String url;

    @Column(name = "execution_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date executionDate;

    public ExecutedArticleEntity() {
    }

    public ExecutedArticleEntity(String publication, String assetId, String url) {
        this.publication = publication;
        this.assetId = assetId;
        this.url = url;
        this.executionDate = Calendar.getInstance().getTime();
    }

    public Long getId() {
        return id;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getPublication() {
        return publication;
    }

    public String getUrl() {
        return url;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    @Override
    public String toString() {
        return publication + " , " + assetId + " , " + url;
    }

    public ExecutedArticleEntity fromString(String object) {
        String[] record = object.split(separator);
        int len = this.toString().split(separator).length;
        if (record.length == len) {
            this.publication = record[0].trim();
            this.assetId = record[1].trim();
            this.url = record[2].trim();
            return this;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExecutedArticleEntity)) return false;
        ExecutedArticleEntity that = (ExecutedArticleEntity) o;
        return Objects.equals(publication, that.publication) &&
                Objects.equals(assetId, that.assetId) &&
                Objects.equals(url, that.url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(publication, assetId, url);
    }
}
