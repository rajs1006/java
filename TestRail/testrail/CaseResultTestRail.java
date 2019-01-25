package com.test.tools.testutil.testrail;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to add results for Case ID on Test Rail.
 * <p>
 * Use this annotation over the method ie. {@TestResultTestRail(testId = "163008")}
 * <p>
 * For more details : "http://docs.gurock.com/testrail-api2/reference-results"
 * <p>
 * Created BY s.raj@funkedigital.de on 23-May-18
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CaseResultTestRail {
    /**
     * Accepts runID of TestRail RUN. Should be not null.
     */
    @Nonnull String runId();

    /**
     * Accepts caseID of case from TestRail. Should be not null.
     * <p>
     * Remove the prefix 'C' before passing the ID ie. for case ID 'C17718' pass only '17718'
     */
    @Nonnull String caseId();

    /**
     * List of defects that needed to be linked with the case
     */
    String[] defectList() default {};

    /**
     * Int ID of user to assign the case.
     */
    int assignedUser() default 0;
}
