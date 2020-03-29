package de.funkedigital.autotagging.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column(name = "articleId")
    private Long articleId;

    @Column(name = "publication")
    private String publication;

    public Article(Long articleId, String publication) {
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
