package de.funke.tools.testutil.prometheus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by sraj , 08-Jun-18
 * <p>
 * This annotation is used to store the result of Test Class on Prometheus.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Prometheus {
    // Register on Prometheus.
    boolean register() default true;
}
