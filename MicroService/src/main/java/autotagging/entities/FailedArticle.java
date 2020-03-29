package de.funkedigital.autotagging.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "failed_articles")
public class FailedArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "articleId")
    private Long articleId;

    @Column(name = "publication")
    private String publication;

    public FailedArticle() {
    }

    public FailedArticle(Long articleId, String publication) {
        this.articleId = articleId;
        this.publication = publication;
    }


    public Long getArticleId() {
        return articleId;
    }

    public String getPublication() {
        return publication;
    }
}
