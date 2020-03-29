package de.funkedigital.autotagging.enums;

/**
 * This enum contains the Repository type
 * <p>
 * And used in {@link "auto-tagging-articles.html"} and
 * {@link de.funkedigital.autotagging.controllers.ExecutionController}
 */
public enum RepositoryEnum {

    /**
     * {@link de.funkedigital.autotagging.repositories.ExecutedArticleRepository}
     */
    Executed,

    /**
     * {@link de.funkedigital.autotagging.repositories.PendingArticleRepository}
     */
    Pending,

    /**
     * {@link de.funkedigital.autotagging.repositories.FailedArticleRepository}
     */
    Failed

}
