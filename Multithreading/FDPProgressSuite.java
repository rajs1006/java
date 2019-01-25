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
 * <p>
 * That test suite is created in order to make the test results available
 * for the test in progress jenkins plugin.
 * <p>
 * This class also register results of {@link Prometheus} annotated test Classes on Prometheus.
 * <p>
 * Created by aherr on 18.10.2016.
 *
 * @link https://sconfluence.com.testmedien.de/display/FUNDigital/Testautomatisierung
 * @link https://wiki.jenkins-ci.org/display/JENKINS/Test+In+Progress+Plugin
 */
public class FDPProgressSuite extends ProgressSuite {

    /**
     * To log activities.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FDPProgressSuite.class);

    private final static String threadCount = System.getProperty("maxParallelTestThreads");

    final String testClassesToInclude = System.getProperty("suiteTest.include", null);

    public FDPProgressSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
        setScheduler(FDPSchedulerFactory.getInstance(FDPSchedulerService.class, threadCount));
    }

    /**
     * Filter out the runners we do not want to execute.
     */
    @Override
    protected List<Runner> getChildren() {
        List<Runner> runners = super.getChildren();
        List<Runner> result = new ArrayList<>();
        for (Runner runner : runners) {
            if (isAllowed(runner)) {
                result.add(runner);
            }
        }
        return result;
    }

    /**
     * Check if a runner for a test class should be executed.
     * That only the case if the system property <code>integrationTest.include</code> was NOT set or
     * if the test class name matches the property value.
     */
    protected boolean isAllowed(Runner testRunner) {
        String testClassName = getTestClassSimpleName(testRunner);
        return testClassesToInclude == null || testClassesToInclude.trim().isEmpty() || testClassesToInclude.contains(testClassName);
    }

    /**
     * @param runner runner to check
     * @return get the test class' simple name form the given runner
     */
    private String getTestClassSimpleName(Runner runner) {
        String testClassName = runner.getDescription().getClassName();
        int startSimpleName = testClassName.lastIndexOf('.');
        if (startSimpleName > 0) {
            testClassName = testClassName.substring(startSimpleName + 1);
        }
        return testClassName;
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
        Result resultFDP = new Result();
        RunListener resultRunListenerFDP = resultFDP.createListener();
        notifier.addListener(resultRunListenerFDP);
        // Listener to capture abrupt stop.
        EnvironmentRunListener environmentRunListener = new EnvironmentRunListener(notifier);
        notifier.addListener(environmentRunListener);
        try {
            // Calling super method {@link ProgressSuite#run}
            super.run(notifier);
        } finally {
            LOGGER.info("Start : Prometheus graph ");
            String suiteName = this.getTestClass().getJavaClass().getSimpleName();
            processResult(notifier, resultFDP, resultRunListenerFDP, environmentRunListener, suiteName, PrometheusWrapper.checkGraphConditions(suiteName));
            LOGGER.info("End : Prometheus graph");
        }
    }

    /**
     * This method calls {@link PrometheusUtil} and builds Prometheus Graph.
     *
     * @param notifier             instance of {@link RunNotifier}
     * @param resultFDP            instance of {@link Result}
     * @param resultRunListenerFDP instance of {@link RunListener}
     */
    private void processResult(RunNotifier notifier, Result resultFDP, RunListener resultRunListenerFDP,
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
            processFailedTestClass(resultFDP, prometheusMap, testRailMap, suiteName, buildGraph);
            // Loop for Tests with status PASS
            processTestClassMap(prometheusMap, testRailMap, suiteName, buildGraph);
        } catch (Exception e) {
            LOGGER.error("Failed to build prometheus graph", e);
        } finally {
            // Removing listener and destroying the instances of Prometheus graph objects.
            notifier.removeListener(resultRunListenerFDP);
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
     * @param resultFDP     Result instance of Notifier.
     * @param prometheusMap Map containing Test class and its value for Prometheus processing.
     * @param testRailMap   Map containing Test class and its value for TestRailFields processing.
     * @param suiteName     Name of suite
     * @param buildGraph    whether the build graph check in True/False {@link PrometheusWrapper#checkGraphConditions(String)}
     * @throws Exception Exception to be thrown.
     */
    private void processFailedTestClass(Result resultFDP, Map<String, Map<String, Long>> prometheusMap, Map<String, TestRailFields> testRailMap, String suiteName, boolean buildGraph) throws Exception {
        for (Failure failure : resultFDP.getFailures()) {
            Description desc = failure.getDescription();
            // Prometheus
            PrometheusWrapper.graphForFailedTest(prometheusMap, suiteName, buildGraph, desc.getTestClass());
            // TestRail
            TestRailWrapper.updateErrorDetails(testRailMap, desc.getMethodName(), failure.getMessage(), resultFDP.getRunTime());
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
