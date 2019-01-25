package de.funke.tools.testutil.testrail;

import de.funke.tools.testutil.prometheus.PrometheusUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.runners.model.FrameworkMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * This class works as Wrapper class of {@link PrometheusUtil}
 *
 * @author sraj Created on 09-10-2018
 */
public class TestRailWrapper {

    /**
     * To log the activities.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TestRailWrapper.class);


    /**
     * This method pupulates the map with mthod names and its values if Annotations {@link TestResultTestRail} or
     * {@link CaseResultTestRail} is present over the method.
     *
     * @param testRailMap Map containing test class and details of object {@link TestRailFields}
     * @param methods     List of {@link FrameworkMethod} methods.
     */
    public static void populateTestRailMap(Map<String, TestRailFields> testRailMap, @Nonnull List<FrameworkMethod> methods) {
        TestRailFields testRail;
        try {
            for (@Nonnull FrameworkMethod fMethods : methods) {
                LOGGER.debug(" Processing method {}", fMethods);
                String[] nameData = TestRailUtils.getMethodDescription(fMethods.getName());
                testRail = testRailMap.get(nameData[0]);
                if (testRail == null) {
                    TestResultTestRail testRailTest = fMethods.getAnnotation(TestResultTestRail.class);
                    CaseResultTestRail caseRailTest = fMethods.getAnnotation(CaseResultTestRail.class);
                    if (testRailTest != null) {
                        testRail = new TestRailFields(testRailTest.testId(), testRailTest.assignedUser(), testRailTest.defectList());
                    } else if (caseRailTest != null) {
                        testRail = new TestRailFields(caseRailTest.runId(), caseRailTest.caseId(), caseRailTest.assignedUser(), caseRailTest.defectList());
                    }
                }
                // Set publication and stage
                if (testRail != null) {
                    testRail.setPublication(nameData[1]);
                    testRail.setStage(nameData[2]);
                    // Put in map.
                    testRailMap.put(nameData[0], testRail);
                }
            }
        } catch (Exception e) {
            // Exception is not rethrown as test case should not fail even if TestRailWrapper integration fails.
            LOGGER.error("TestRail result populate failed ", e);
        }
        LOGGER.debug("Data is populated");
    }

    /**
     * This method will register the method results with TestRailWrapper.
     * <p>
     * This method runs for multiple annotations. eg. if someone wants to register the result for
     * Case as well as Test they can graphForFailedTest both the annotations over the method.
     *
     * @param testRail instance of object {@link TestRailFields}
     */
    public static void pushToTestRail(TestRailFields testRail) {
        try {
            if (testRail.getTestId() != null) {
                LOGGER.debug("Rail test integration for Test Results started");
                TestRailAPI testRailAPI = new TestRailAPI(testRail.getPublication(), testRail.getStage());
                testRailAPI.addResultForTest(testRail);
                LOGGER.debug("Rail test integration for Test Results finished");
            } else if (testRail.getCaseId() != null && testRail.getRunId() != null) {
                LOGGER.debug("Rail test integration for Case Results started");
                TestRailAPI testRailAPI = new TestRailAPI(testRail.getPublication(), testRail.getStage());
                testRailAPI.addResultForCase(testRail);
                LOGGER.debug("Rail test integration for Case Results finished");
            }
        } catch (Exception e) {
            // Exception is not rethrown as test case should not fail even if TestRailWrapper integration fails.
            LOGGER.error("TestRail result push failed ", e);
        }

    }

    /**
     * Update the object of {@link TestRailFields} as per the failed test Methods.
     *
     * @param testRailMap  Map containing test class and details of object {@link TestRailFields}
     * @param methodName   Name of method.
     * @param errorMessage Error message
     * @param timeTaken    Total time taken for execution.
     */
    public static void updateErrorDetails(Map<String, TestRailFields> testRailMap, String methodName, String errorMessage, long timeTaken) {
        try {
            String[] nameData = TestRailUtils.getMethodDescription(methodName);
            // Process Map.
            TestRailFields testRail = testRailMap.get(nameData[0]);
            if (testRail != null) {
                testRail.setFailedPublication(nameData[1]);
                testRail.setFailedMessage((!StringUtils.isEmpty(testRail.getFailedMessage()) ? testRail.getFailedMessage() : "Error/s : \n")
                        + nameData[1] + " : " + TestRailUtils.getFirstErrorLine(errorMessage));
                testRail.setStatus(false);
                testRail.setElapsedTime(timeTaken);
            }
        } catch (Exception e) {
            // Exception is not rethrown as test case should not fail even if TestRailWrapper integration fails.
            LOGGER.error("TestRail result update failed ", e);
        }
        LOGGER.debug("Data is set");
    }

}
