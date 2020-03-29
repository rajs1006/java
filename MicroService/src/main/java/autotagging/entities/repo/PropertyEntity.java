package de.funkedigital.autotagging.entities.repo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "properties")
public class PropertyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    private PublicationEntity publication;

    @Column(name = "sitemap_file")
    private String sitemapFiles;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PublicationEntity getPublication() {
        return publication;
    }

    public void setPublication(PublicationEntity publication) {
        this.publication = publication;
    }

    public String getSitemapFiles() {
        return sitemapFiles;
    }

    public void setSitemapFiles(String sitemapFiles) {
        this.sitemapFiles = sitemapFiles;
    }
}
