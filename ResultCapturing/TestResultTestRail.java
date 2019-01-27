package com.test.tools.testutil.testrail;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to add results for Test ID on Test Rail.
 * <p>
 * Use this annotation over the method ie. {@TestResultTestRail(testId = "163008")}
 * <p>
 * For more details : "http://docs.gurock.com/testrail-api2/reference-results"
 * <p>
 * Created BY s.raj@.de on 23-May-18
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestResultTestRail{

    /**
     * Accepts testID of test from TestRail. Should be not null
     * <p>
     * Remove the prefix 'T' before passing the ID ie. for test ID 'T163008' pass only '163008'
     */
    @Nonnull String testId();

    /**
     * List of defects that needed to be linked with the test
     */
    String[] defectList() default {};

    /**
     * Int ID of user to assign the test.
     */
    int assignedUser() default 0;

}
