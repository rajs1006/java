package com.test.tools.testutil.runner;

import com.test.tools.testutil.prometheus.Prometheus;
import com.test.tools.testutil.prometheus.PrometheusUtil;
import com.test.tools.testutil.prometheus.PrometheusWrapper;
import com.test.tools.testutil.testrail.TestRailFields;
import com.test.tools.testutil.testrail.TestRailWrapper;
import org.jenkinsci.testinprogress.runner.ProgressSuite;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Integration of the our test suites into gradle jenkins build environment.
 */
public class ProgressSuite extends ProgressSuite {

    /**
     * To log activities.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProgressSuite.class);


    public ProgressSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
        setScheduler(SchedulerFactory.getInstance(SchedulerService.class, threadCount));
    }


    /**
     * This method override the run method {@link ProgressSuite#run(RunNotifier)}
     * and create the graph after the suite execution completes.
     *
     * @param notifier instance of {@link RunNotifier}
     */
    @Override
    public void run(RunNotifier notifier) {
        // Creating a listener to listen to all the results.
        Result result = new Result();
        RunListener resultRunListener = result.createListener();
        notifier.addListener(resultRunListener);
        // Listener to capture abrupt stop.
        EnvironmentRunListener environmentRunListener = new EnvironmentRunListener(notifier);
        notifier.addListener(environmentRunListener);
        try {DP
            // Calling super method {@link ProgressSuite#run}
            super.run(notifier);
        } finally {
            LOGGER.info("Start : Prometheus graph ");
            String suiteName = this.getTestClass().getJavaClass().getSimpleName();
            processResult(notifier, result, resultRunListener, environmentRunListener, suiteName, PrometheusWrapper.checkGraphConditions(suiteName));
            LOGGER.info("End : Prometheus graph");
        }
    }

    /**
     * This method calls {@link PrometheusUtil} and builds Prometheus Graph.
     *
     * @param notifier             instance of {@link RunNotifier}
     * @param result            instance of {@link Result}
     * @param resultRunListener instance of {@link RunListener}
     */
    private void processResult(RunNotifier notifier, Result result, RunListener resultRunListener,
                               EnvironmentRunListener environmentRunListener, String suiteName, boolean buildGraph) {
        try {
            Map<String, TestRailFields> testRailMap = new HashMap<>();
            Map<String, Map<String, Long>> prometheusMap = new HashMap<>();
            PrometheusWrapper.init(buildGraph);
            // Using MAP to avoid nested loop as Nested loop for 15 classes will execute 15(children loop)*15(Failure loop)
            // = 225 in worst case scenario, but MAP cost will be 15(children loop) + 15(Failure loop) + 15 (Map loop) = 45
            // in worst case scenario.
            processTestClass(prometheusMap, testRailMap, buildGraph);
            // Iterating over failure and removing the key from MAP for failureCLassName.
            processFailedTestClass(result, prometheusMap, testRailMap, suiteName, buildGraph);
            // Loop for Tests with status PASS
            processTestClassMap(prometheusMap, testRailMap, suiteName, buildGraph);
        } catch (Exception e) {
            LOGGER.error("Failed to build prometheus graph", e);
        } finally {
            // Removing listener and destroying the instances of Prometheus graph objects.
            notifier.removeListener(resultRunListener);
            notifier.removeListener(environmentRunListener);
            PrometheusWrapper.destroy(buildGraph);
        }
    }


    /**
     * This method regulates the executed Test classes for further processing
     *
     * @param prometheusMap Map containing Test class and its value for Prometheus processing.
     * @param testRailMap   Map containing Test class and its value {@link TestRailFields} for TestRailFields processing.
     * @param buildGraph    whether the build graph check in True/False {@link PrometheusWrapper#checkGraphConditions(String)}
     */
    private void processTestClass(Map<String, Map<String, Long>> prometheusMap, Map<String, TestRailFields> testRailMap, boolean buildGraph) {
        for (Runner runner : this.getChildren()) {
            TestClass testClass = ((EnvironmentTestRunner) runner).getTestClass();
            PrometheusWrapper.populatePrometheusMap(prometheusMap, buildGraph, testClass);
            // Returns map of MethodName and Corresponding annotation details.
            TestRailWrapper.populateTestRailMap(testRailMap, ((EnvironmentTestRunner) runner).getChildren());
        }
    }


    /**
     * This class process Failed test class for {@link PrometheusWrapper} and {@link TestRailWrapper}
     *
     * @param result     Result instance of Notifier.
     * @param prometheusMap Map containing Test class and its value for Prometheus processing.
     * @param testRailMap   Map containing Test class and its value for TestRailFields processing.
     * @param suiteName     Name of suite
     * @param buildGraph    whether the build graph check in True/False {@link PrometheusWrapper#checkGraphConditions(String)}
     * @throws Exception Exception to be thrown.
     */
    private void processFailedTestClass(Result result, Map<String, Map<String, Long>> prometheusMap, Map<String, TestRailFields> testRailMap, String suiteName, boolean buildGraph) throws Exception {
        for (Failure failure : result.getFailures()) {
            Description desc = failure.getDescription();
            // Prometheus
            PrometheusWrapper.graphForFailedTest(prometheusMap, suiteName, buildGraph, desc.getTestClass());
            // TestRail
            TestRailWrapper.updateErrorDetails(testRailMap, desc.getMethodName(), failure.getMessage(), result.getRunTime());
        }
    }


    /**
     * This method process the Final map for {@link PrometheusWrapper} and {@link TestRailWrapper}
     *
     * @param prometheusMap Updated instance of PrometheusMap
     * @param testRailMap   Updated instance of TestRailMap
     * @param suiteName     Name of suite
     * @param buildGraph    whether the build graph check in True/False {@link PrometheusWrapper#checkGraphConditions(String)}
     * @throws Exception Exception to be thrown.
     */
    private void processTestClassMap(Map<String, Map<String, Long>> prometheusMap, Map<String, TestRailFields> testRailMap, String suiteName, boolean buildGraph) throws Exception {
        if (buildGraph) {
            // Process all the pass results.
            for (Map.Entry<String, Map<String, Long>> entry : prometheusMap.entrySet()) {
                PrometheusWrapper.GraphForPassedTest(suiteName, entry);
            }
        }
        // push results to TestRail.
        for (Map.Entry<String, TestRailFields> entry : testRailMap.entrySet()) {
            TestRailWrapper.pushToTestRail(entry.getValue());
        }
    }
}
