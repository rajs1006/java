package com.test.tools.testutil.prometheus;


import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * @Created By s.raj@com.testdigital.de, Date : 08-June-18
 * <p>
 * This is Utility class to create graph on Prometheus.
 */
public class PrometheusUtil {

    /**
     * To log the activities.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusUtil.class);

    /**
     * This array contains the names of label needed to be show in the graph.
     */
    private static final String[] COUNTER_LABEL_NAME = {"Suite", "Stage"};
    private static final String[] GAUGE_LABEL_NAME = {"Suite", "Test", "Stage"};
    private static final String PROPERTY = "prometheus.properties";

    /**
     * Instance of {@link Gauge} prometheus graph type
     */
    private static Gauge gauge;
    /**
     * Instance of {@link Counter} prometheus graph type
     */
    private static Counter counter;
    /**
     * Contains the instance of Registry {@link CollectorRegistry}.
     */
    private static CollectorRegistry registry = new CollectorRegistry();

    // Fields to load Property file and Jenkins ENV var.
    private static String prometheusDNS;
    private static String prometheusPort;
    private static String prometheusRegName;


    /**
     * This holds the property of Prometheus IP Address and Port.
     */
    private static Properties prop = new Properties();

    // Loading the property file, it will be loaded only once at the time of class loading
    static {
        try {
            prop.load(PrometheusUtil.class.getClassLoader().getResourceAsStream(PROPERTY));
            // Populating property values.
            populatePropFields();
            LOGGER.debug("Property loaded ");
        } catch (IOException ex) {
            LOGGER.error("Property file could not be loaded", ex);
        }
    }

    /**
     * This method populates the Property variables.
     */
    private static void populatePropFields() {
        prometheusDNS = prop.get("prometheus.dns").toString();
        prometheusPort = prop.get("prometheus.port").toString();
        prometheusRegName = prop.get("prometheus.registry.name").toString();
    }

    /**
     * This overloaded generic method Initializes the instance of Counter Graph.
     */
    public static void initCounter(String name) {
        // Initiating the graph based on the Suite.
        name = name + "_counter";
        LOGGER.info("Counter Graph : " + name);
        counter = Counter.build()
                .name(name).help("This counter graph tracks the failed test classes together")
                .labelNames(COUNTER_LABEL_NAME).register(registry);
    }

    /**
     * This overloaded generic method Initializes the instance of gauge Graph.
     */
    public static void initGauge(String name) {
        // Initiating the graph based on the Suite.
        name = name + "_gauge";
        LOGGER.info("Gauge Graph : " + name);
        gauge = Gauge.build()
                .name(name).help("This counter graph tracks the failed test classes separately")
                .labelNames(GAUGE_LABEL_NAME).register(registry);
    }


    /**
     * This is overloaded generic counter graph method.
     *
     * @param name  name label value
     * @param stage stage label value
     * @throws Exception Throws exception.
     */
    public static void counter(String name, String stage, double value) throws Exception {
        String[] labelValue = {name, stage};
        // Increase Counter for test class based on status.
        counter.labels(labelValue).inc(value);
        LOGGER.debug("Counter :-: " + name + " , " + stage);
    }


    /**
     * This is overloaded generic Gauge graph method.
     *
     * @param inc       To increase a value or keep it 0
     * @param dec       To decrease a value or keep it 0
     * @param name      name label value
     * @param className name of class label value
     * @param stage     stage label value
     * @throws Exception Throws exception.
     */
    public static void gauge(String name, String className, String stage, boolean inc, boolean dec, double value) throws Exception {
        String[] labelValue = {name, className, stage};
        // Add test class based on status to Gauge.
        if (inc) {
            gauge.labels(labelValue).inc(value);
        } else if (dec) {
            gauge.labels(labelValue).dec(value);
        }
        LOGGER.debug("Gauge :-: " + name + " , " + className + " , " + stage);
    }


    /**
     * This method registers and push the data on Prometheus.
     *
     * @throws Exception to be thrown
     */
    public static void regAndDestroy() {
        try {
            PushGateway pg = new PushGateway(prometheusDNS + ":" + prometheusPort);
            pg.push(registry, prometheusRegName);
        } catch (Exception e) {
            LOGGER.error("Error occurred while registering the data ", e);
        } finally {
            destroy();
        }
    }

    /**
     * This method clears the reference of the Graphs.
     */
    private static void destroy() {
        try {
            if (counter != null) {
                counter.clear();
                counter = null;
            }
            if (gauge != null) {
                gauge.clear();
                gauge = null;
            }
            if (registry != null) {
                registry.clear();
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred while destroying the data ", e);
        }
    }

}
