package de.funke.tools.testutil.prometheus;

import de.funke.tools.testutil.runner.EnvironmentRunListener;
import org.apache.commons.lang3.StringUtils;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * This class works as Wrapper class of {@link PrometheusUtil}
 *
 * @author sraj Created on 09-10-2018
 */
public class PrometheusWrapper {

    /**
     * To log activities.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusWrapper.class);

    private static final long ZERO = 0;

    private static final long ONE = 1;

    private static final String GAUGE_VALUE = "prometheusGaugeValue";

    private static final String COUNTER_VALUE = "prometheusCounterValue";

    private static final String TRUE = "true";

    private static final String stage = System.getProperty("stage");

    private static final String PUSH_TO_PROMETHEUS = System.getProperty("pushToPrometheus");

    private static final String PROMETHEUS_GRAPH_NAME = System.getProperty("prometheusGraphName");


    /**
     * Initialize Prometheus Graph.
     *
     * @param buildGraph if Graph need to be built.
     */
    public static void init(boolean buildGraph) {
        if (buildGraph) {
            String name = PROMETHEUS_GRAPH_NAME;
            PrometheusUtil.initCounter(name);
            PrometheusUtil.initGauge(name);
        }
    }

    /**
     * Destroy Prometheus Graph.
     *
     * @param buildGraph if Graph need to be destroyed.
     */
    public static void destroy(boolean buildGraph) {
        if (buildGraph) {
            PrometheusUtil.regAndDestroy();
        }
    }

    /**
     * This method populates the Prometheus MAP with class name and its value.
     * <p>
     * This method populates the classMap with all the executed TestClasses with error message as Null.
     * This just maintains the list of all the Executed class and dealt with in further impl.
     *
     * @param prometheusMap Map, containing Test class and its value.
     * @param buildGraph    whether the build graph check in True/False {@link #checkGraphConditions(String)}
     * @param testClass     Classes need to be put inthe map.
     * @return Populated prometheusMap.
     */
    public static void populatePrometheusMap(Map<String, Map<String, Long>> prometheusMap, boolean buildGraph, TestClass testClass) {
        if (isPrometheusAnnotated(testClass) && buildGraph) {
            String className = testClass.getJavaClass().getSimpleName();
            /*IMP : This line can be un-commented in case of the generic implementation of Prometheus graph
             * In that case fields from AbstractIT can be populated and used as value to
             * increase or decrease in counter and gauge. This is used in method #graphForPassedTestClass method */
            //Map<String, Long> valueMap = getAbstractClass(testClass);
            prometheusMap.put(className, null /*valueMap*/);
        }
    }


    /**
     * This method creates the prometheus graph for Failed testClasses and then remove those class
     * from classMap to avoid redundant iteration.
     *
     * @param prometheusMap Populated Map, containing Test class and its value.
     * @param suiteName     Name of suite.
     * @param buildGraph    whether the build graph check in True/False {@link #checkGraphConditions(String)}
     * @param failedClass   instance of failed class
     * @throws Exception Exception to be thrown.
     */
    public static void graphForFailedTest(Map<String, Map<String, Long>> prometheusMap, @Nonnull String suiteName, boolean buildGraph,
                                          @Nonnull Class<?> failedClass) throws Exception {
        if (isPrometheusAnnotated(failedClass) && buildGraph) {
            String failedClassName = failedClass.getSimpleName();
            // To ensure that even if multiple method fails in a single test class, it should
            // create only one graph for that class with the first occurrence of Error Message.
            if (prometheusMap.keySet().contains(failedClassName)) {
                LOGGER.debug("Failed Class Name : {}", failedClassName);
                // Creating graph.
                buildCounter(suiteName, ONE);
                // Removing the failed class key to avoid iteration over that class again.
                prometheusMap.remove(failedClassName);
            }
            buildGauge(suiteName, failedClassName, true, false, ONE);
        }
    }


    /**
     * This method calls {@link PrometheusUtil} to create Prometheus Graph.
     * <p>
     * This method is only called to update the graph for passed test classes.
     *
     * @param suiteName Name of suite.
     * @param entry     Entry from Prometheus Map.
     * @throws Exception Exception to be thrown.
     */
    public static void GraphForPassedTest(@Nonnull String suiteName, Map.Entry<String, Map<String, Long>> entry) throws Exception {
        String className = entry.getKey();
        Map<String, Long> classValue = entry.getValue();
        if (classValue == null || classValue.isEmpty()) {
            buildCounter(suiteName, ZERO);
            buildGauge(suiteName, className, true, false, ZERO);
        } else {
            buildCounter(suiteName, classValue.get(COUNTER_VALUE));
            buildGauge(suiteName, className, true, false, classValue.get(GAUGE_VALUE));
        }
    }

    /**
     * Checks for availability of {@link Prometheus} annotation on the class
     *
     * @param testClass {@link TestClass} type
     * @return TRUE/FALSE based on annotation present
     */
    private static boolean isPrometheusAnnotated(@Nonnull TestClass testClass) {
        Prometheus prometheusAnnotation = testClass.getAnnotation(Prometheus.class);
        return (prometheusAnnotation != null && prometheusAnnotation.register());
    }

    /**
     * Checks for availability of {@link Prometheus} annotation on the class
     *
     * @param failedClass {@link Class} type
     * @return TRUE/FALSE based on annotation present
     */
    private static boolean isPrometheusAnnotated(@Nonnull Class<?> failedClass) {
        Prometheus prometheusAnnotation = failedClass.getAnnotation(Prometheus.class);
        return (prometheusAnnotation != null && prometheusAnnotation.register());
    }

    /**
     * This method Initiate the gauge and build Gauge graph.
     *
     * @param suiteName Name of Suite.
     * @param className name of class
     * @param inc       Whether the gauge  should be increased
     * @param dec       Whether the gauge  should be decreased
     * @param value     With what value the gauge should increase or decrease.
     * @throws Exception Exception to be thrown
     */
    private static void buildGauge(@Nonnull String suiteName, @Nonnull String className, boolean inc, boolean dec, @Nonnull long value) throws Exception {
        PrometheusUtil.gauge(suiteName, className, stage, inc, dec, value);
    }

    /**
     * This method Initiate the Counter and build Counter graph.
     *
     * @param suiteName Name of Suite.
     * @param value     With what value the counter should increase.
     * @throws Exception Exception to be thrown
     */
    private static void buildCounter(@Nonnull String suiteName, @Nonnull long value) throws Exception {
        PrometheusUtil.counter(suiteName, stage, value);
    }

    /**
     * This method iterates over the super class and get the clas AbstractIT to access the generic Prometheus fields
     *
     * @param testClass Current class.
     * @return Instance of AbstractIT.
     */
    private static Map<String, Long> getAbstractClassFields(TestClass testClass) throws Exception {
        Map<String, Long> prometheusValueMap = new HashMap<>();
        Class<?> cls = testClass.getJavaClass();
        do {
            cls = cls.getSuperclass();
        } while (cls != null && !cls.getSimpleName().equalsIgnoreCase("AbstractIT"));
        if (cls != null) {
            Long counterValue = cls.getDeclaredField(COUNTER_VALUE).getLong(Long.class);
            Long gaugeValue = cls.getDeclaredField(GAUGE_VALUE).getLong(Long.class);
            // Populate the values in Map.

            prometheusValueMap.put(COUNTER_VALUE, counterValue);
            prometheusValueMap.put(GAUGE_VALUE, gaugeValue);
        }
        // return the map.
        return prometheusValueMap;
    }

    /**
     * This method checks whether to build a graph or not.
     *
     * @return True/False
     */
    public static boolean checkGraphConditions(@Nonnull String suiteName) {
        boolean buildGraph = false;
        if (EnvironmentRunListener.isStopped) {
            LOGGER.error("Prometheus graph could not be created, Servers are not available ");
        } else if (!(TRUE.equalsIgnoreCase(PUSH_TO_PROMETHEUS)) || StringUtils.isEmpty(PROMETHEUS_GRAPH_NAME)) {
            // -DprometheusGraphName=<Name> , by default set to Integration_test in stage/gradle.build.
            LOGGER.error("Prometheus Graph is not configured for this suite : " + suiteName +
                    " : -DpushToPrometheus=true configuration required");
        } else {
            buildGraph = true;
        }
        return buildGraph;
    }
}
