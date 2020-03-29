package de.funkedigital.autotagging.enums;


/**
 * This enum contains the service types. This enum is bound with
 * {@link de.funkedigital.autotagging.entities.repo.ServiceEntity}
 * and so when inserting data in "service" table, we need to match
 * the names.
 * <p>
 * Loads, service as per their sequence in
 * {@link de.funkedigital.autotagging.controllers.SchedulerController}
 * <p>
 * data.sql
 * --->insert into services(service, sequence) VALUES
 * ('LoadArticleService', 1),
 * ('PendingArticleService', 2),
 * ('FailedArticleService', 3);
 * <p>
 * And used in {@link "auto-tagging-details.html"} and
 * {@link de.funkedigital.autotagging.controllers.ExecutionController}
 */
public enum ServiceEnum {


    /**
     * {@link de.funkedigital.autotagging.services.LoadArticleService}
     */
    LoadArticleService,

    /**
     * {@link de.funkedigital.autotagging.services.PendingArticleService}
     */
    PendingArticleService,

    /**
     * {@link de.funkedigital.autotagging.services.FailedArticleService}
     */
    FailedArticleService;


    /**
     * Return service enum based on class type
     *
     * @param service Instance of service class.
     * @return Enum
     */
    public static ServiceEnum getServiceEnum(String serviceName) {

        if (serviceName.equalsIgnoreCase(LoadArticleService.name())) {
            return LoadArticleService;
        } else if (serviceName.equalsIgnoreCase(PendingArticleService.name())) {
            return PendingArticleService;
        } else if (serviceName.equalsIgnoreCase(FailedArticleService.name())) {
            return FailedArticleService;
        }
        return null;

    }

}
