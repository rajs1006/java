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
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "pending_articles")
public class PendingArticleEntity {

    private static final String separator = ",";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "publication")
    private String publication;

    @Column(name = "url")
    private String url;

    @Column(name = "execution_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date executionDate;

    public PendingArticleEntity() {
    }

    public PendingArticleEntity(String publication, String url) {
        this.publication = publication;
        this.url = url;
        this.executionDate = Calendar.getInstance().getTime();
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

    public Date getExecutionDate() {
        return executionDate;
    }

    @Override
    public String toString() {
        return id + separator + publication + separator + url;
    }

    public PendingArticleEntity fromString(String object) {
        String[] record = object.split(separator);
        int len = this.toString().split(separator).length;
        if (record.length == len) {
            this.id = Long.valueOf(record[0].trim());
            this.publication = record[1].trim();
            this.url = record[3].trim();
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

        if (!(o instanceof PendingArticleEntity)) return false;

        PendingArticleEntity that = (PendingArticleEntity) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(publication, that.publication)
                .append(url, that.url)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(publication)
                .append(url)
                .toHashCode();
    }
}
