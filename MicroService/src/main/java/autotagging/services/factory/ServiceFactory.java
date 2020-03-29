package de.funkedigital.autotagging.services.factory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;

/**
 * This class is uses to dynamically create the Prototype instance of service using
 * {@link ApplicationContext}
 *
 * @param <T> child of {@link de.funkedigital.autotagging.services.interfaces.ServiceInterface}
 */
public class ServiceFactory<T> implements FactoryBean<T> {

    /**
     * Instance of {@link ApplicationContext}
     */
    private ApplicationContext ctx;

    /**
     * Class of {@link de.funkedigital.autotagging.services.interfaces.ServiceInterface}
     */
    private Class<T> service;


    public ServiceFactory(Class<T> service) {
        this.service = service;
    }

    public ServiceFactory(Class<T> service, ApplicationContext ctx) {
        this.service = service;
        this.ctx = ctx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getObject() {
        try {
            if (this.ctx != null) {
                // Initializing bean using application context as using New keyword
                // to instantiate prototype bean leads to have null sigleton beans in prototype class.
                // repository was null in AutoTaggingArticleRepositoryService if initialize using New.
                return (T) ctx.getBean(this.service);
            }
            return this.service.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSingleton() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getObjectType() {
        return this.service;
    }
}
