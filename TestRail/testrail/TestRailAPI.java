package de.funke.tools.testutil.testrail;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * This class acts as wrapper for original TestRail API class {@link APIClient}.
 * <p>
 * For more details : "http://docs.gurock.com/testrail-api2/bindings-java"
 * <p>
 * Created BY s.raj@funkedigital.de on 24-April-18
 */
public class TestRailAPI extends APIClient {

    /**
     * To log the activities.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TestRailAPI.class);

    /**
     * This holds the property of Test Rail containing Username,Pass,URL
     */
    private static Properties prop = new Properties();

    // Loading the property file, it will be loaded only once at the time of class loading
    static {
        try {
            prop.load(TestRailAPI.class.getClassLoader().getResourceAsStream("testrailcredentials.properties"));
            LOGGER.debug("Property loaded ");
        } catch (IOException ex) {
            LOGGER.error("Property file could not be loaded", ex);
        }
    }

    /**
     * Stores Publication.
     */
    private String publication;

    /**
     * Stores Stage
     */
    private String stage;

    /**
     * Stores Jenkins Build Version
     */
    private String buildVersion;

    /**
     * Constructor
     *
     * @param url contain URL of test rail.
     */
    public TestRailAPI(String url) {
        super(url);
    }

    /**
     * This constructor will load the URL,USER and PASSWORD
     *
     * @param publication Publication
     * @param stage       Stage
     */
    public TestRailAPI(String publication, String stage) {
        super(prop.get("test.rail.url").toString());
        super.setUser(prop.get("test.rail.user").toString());
        super.setPassword(prop.get("test.rail.password").toString());
        this.buildVersion = System.getenv("BUILD_TAG");
        this.publication = publication;
        this.stage = stage;

        LOGGER.debug("Constructor has been initialized");
    }

    /**
     * This method load the object of testRail fields and update the results.
     *
     * @param testRailFields Object of @{@link TestRailFields }
     */
    public void addResultForTest(TestRailFields testRailFields) {
        String msg = "addResultForTest(TestRailFields) : ";
        try {
            testRailFields.setVersion(this.buildVersion);
            testRailFields.setComment(addComment(testRailFields));
            JSONObject c = (JSONObject) super.sendPost("add_result/" + testRailFields.getTestId(), testRailFields.toMap());
            //Logging
            msg += "TestRail successfully updated for Test ID : " + c.get("test_id");
            LOGGER.info(msg);
        } catch (Exception e) {
            msg += "Add Result to TestRail failed : " + e.getMessage();
            LOGGER.error(msg, e);

        }
    }


    /**
     * Adds a new test result, comment or assigns a test.
     * <p>
     * Reference : "http://docs.gurock.com/testrail-api2/reference-results#add_result"
     *
     * @param testId       Id of the test
     * @param status       Status belongs to @{@link TestRailStatus }
     * @param comment      Comment to be added as part of result.
     * @param timeElapsed  Execution time of method
     * @param assignedUser user the test to be assigned, pass 0 or use overloaded method if not to be used.
     */
    public void addResultForTest(String testId, boolean status, String comment, long timeElapsed, String[] defectList, int assignedUser) {
        String msg = "addResultForTest() : ";
        try {
            TestRailFields testRailFields = new TestRailFields(status, comment, timeElapsed, this.buildVersion, defectList, assignedUser);
            JSONObject c = (JSONObject) super.sendPost("add_result/" + testId, testRailFields.toMap());
            //Logging
            msg += "TestRail successfully updated for Test ID : " + c.get("test_id");
            LOGGER.info(msg);
        } catch (Exception e) {
            msg += "Add Result to TestRail failed : " + e.getMessage();
            LOGGER.error(msg, e);
        }

    }

    /**
     * This is an overloaded method.
     *
     * @param testId Id of the test
     */
    public void addResultForTest(String testId) {
        addResultForTest(testId, true);
    }

    /**
     * This is an overloaded method.
     *
     * @param testId Id of the test
     * @param status Status belongs to @{@link TestRailStatus }
     */
    public void addResultForTest(String testId, boolean status) {
        addResultForTest(testId, status, null, 0, null, 0);
    }

    /**
     * This is an overloaded method.
     *
     * @param testId  Id of the test
     * @param status  Status belongs to @{@link TestRailStatus }
     * @param comment Comment to be added as part of result.
     */
    public void addResultForTest(String testId, boolean status, String comment) {
        addResultForTest(testId, status, comment, 0, null, 0);
    }

    /**
     * This is an overloaded method.
     *
     * @param testId      Id of the test
     * @param status      Status belongs to @{@link TestRailStatus }
     * @param comment     Comment to be added as part of result.
     * @param timeElapsed Execution time of method
     */
    public void addResultForTest(String testId, boolean status, String comment, long timeElapsed) {
        addResultForTest(testId, status, comment, timeElapsed, null, 0);
    }

    /**
     * This is an overloaded method.
     *
     * @param testId      Id of the test
     * @param status      Status belongs to @{@link TestRailStatus }
     * @param comment     Comment to be added as part of result.
     * @param timeElapsed Execution time of method
     * @param defectLists list of defects to be added.
     */
    public void addResultForTest(String testId, boolean status, String comment, long timeElapsed, String[] defectLists) {
        addResultForTest(testId, status, comment, timeElapsed, defectLists, 0);
    }


    /**
     * This method adds the result of case on testRail
     * <p>
     * "http://docs.gurock.com/testrail-api2/reference-results#add_result_for_case"
     *
     * @param testRailFields Object of @{@link TestRailFields }
     */
    public void addResultForCase(TestRailFields testRailFields) {
        String msg = "addResultForCase() : ";
        try {
            testRailFields.setVersion(this.buildVersion);
            testRailFields.setComment(addComment(testRailFields));
            JSONObject c = (JSONObject) super.sendPost("add_result_for_case/" + testRailFields.getRunId() + "/"
                    + testRailFields.getCaseId(), testRailFields.toMap());
            // Logging
            msg += "TestRail : " + c.get("test_id") + " successfully updated for Run ID : " + testRailFields.getRunId()
                    + " and Case ID : " + testRailFields.getCaseId();
            LOGGER.info(msg);
        } catch (Exception e) {
            msg += "Add Result to TestRail failed : " + e.getMessage();
            LOGGER.error(msg, e);
        }
    }

    /**
     * This method adds the result of case on testRail
     * <p>
     * "http://docs.gurock.com/testrail-api2/reference-results#add_result_for_case"
     *
     * @param runId       Id of testRail RUN
     * @param caseId      Id of Case
     * @param status      Status belongs to @{@link TestRailStatus }
     * @param comment     Comment to be added as part of result.
     * @param timeElapsed Execution time of method
     */
    public void addResultForCase(String runId, String caseId, boolean status, String comment, long timeElapsed, String[] defectList, int assignedUser) {
        String msg = "addResultForCase() : ";
        try {
            TestRailFields testRailFields = new TestRailFields(status, comment, timeElapsed, this.buildVersion, defectList, assignedUser);
            JSONObject c = (JSONObject) super.sendPost("add_result_for_case/" + runId + "/" + caseId, testRailFields.toMap());
            //Logging
            msg += "TestRail : " + c.get("test_id") + " successfully updated for Run ID : " + runId + " and Case ID : " + caseId;
            LOGGER.info(msg);
        } catch (Exception e) {
            msg += "Add Result to TestRail failed : " + e.getMessage();
            LOGGER.error(msg, e);
        }
    }

    /**
     * Overloaded method
     *
     * @param runId  Id of testRail RUN
     * @param caseId Id of Case
     */
    public void addResultForCase(String runId, String caseId) {
        addResultForCase(runId, caseId, true);
    }


    /**
     * Overloaded method @{@link TestRailAPI#addResultForCase}
     *
     * @param runId  Id of testRail RUN
     * @param caseId Id of Case
     * @param status Status belongs to @{@link TestRailStatus }
     */
    public void addResultForCase(String runId, String caseId, boolean status) {
        addResultForCase(runId, caseId, status, null, 0, null, 0);
    }


    /**
     * Overloaded method
     *
     * @param runId   Id of testRail RUN
     * @param caseId  Id of Case
     * @param status  Status belongs to @{@link TestRailStatus }
     * @param comment Comment to be added as part of result.
     */
    public void addResultForCase(String runId, String caseId, boolean status, String comment) {
        addResultForCase(runId, caseId, status, comment, 0, null, 0);
    }

    /**
     * Overloaded method
     *
     * @param runId       Id of testRail RUN
     * @param caseId      Id of Case
     * @param status      Status belongs to @{@link TestRailStatus }
     * @param comment     Comment to be added as part of result.
     * @param timeElapsed Execution time of method
     */
    public void addResultForCase(String runId, String caseId, boolean status, String comment, long timeElapsed) {
        addResultForCase(runId, caseId, status, comment, timeElapsed, null, 0);
    }

    /**
     * Overloaded method
     *
     * @param runId       Id of testRail RUN
     * @param caseId      Id of Case
     * @param status      Status belongs to @{@link TestRailStatus }
     * @param comment     Comment to be added as part of result.
     * @param timeElapsed Execution time of method
     * @param defectLists List of defects to be added.
     */
    public void addResultForCase(String runId, String caseId, boolean status, String comment, long timeElapsed, String[] defectLists) {
        addResultForCase(runId, caseId, status, comment, timeElapsed, defectLists, 0);
    }


    /**
     * This method adds results of multiple tests in testRail
     * <p>
     * "http://docs.gurock.com/testrail-api2/reference-results#add_results"
     *
     * @param runId       Run ID
     * @param testResults List of test ID
     */
    public void addResultForTests(String runId, List<Map> testResults) {
        String msg = "addResultForTests() : ";
        try {
            JSONObject c = (JSONObject) super.sendPost("add_results/" + runId, testResults);
            // Logging
            msg += "TestRail successfully updated for multiple tests with Run ID : " + c.get("run_id");
            LOGGER.info(msg);
        } catch (Exception e) {
            msg += "Add Result to TestRail failed : " + e.getMessage();
            LOGGER.error(msg, e);
        }
    }

    /**
     * This method adds results of multiple cases in testRail
     * <p>
     * http://docs.gurock.com/testrail-api2/reference-results#add_results_for_cases"
     *
     * @param runId       Run ID.
     * @param caseResults List of case Id
     */
    public void addResultForCases(String runId, List<Map> caseResults) {
        String msg = "addResultForCases() : ";
        try {
            JSONObject c = (JSONObject) super.sendPost("add_results_for_cases/" + runId, caseResults);
            // Logging
            msg += "TestRail successfully updated for Multiple cases with Run ID : " + c.get("run_id");
            LOGGER.info(msg);
        } catch (Exception e) {
            msg += "Add Result to TestRail failed : " + e.getMessage();
            LOGGER.error(msg, e);
        }
    }


    /**
     * This method adds comment.
     *
     * @param testRailFields Object of {@link TestRailFields}
     * @return comment appended with publication and stage
     */
    private String addComment(TestRailFields testRailFields) {
        StringBuilder comment = new StringBuilder(testRailFields.getComment()).append("Publication/s : ").append(testRailFields.getPublication())
                .append("  Stage : ").append(testRailFields.getStage()).append("\n\n");
        // Check for failed message.
        if (!StringUtils.isEmpty(testRailFields.getFailedPublication())) {
            StringBuilder error = new StringBuilder("Failed for : ").append("Publication/s : ").append(testRailFields.getFailedPublication())
                    .append(" \n ").append(testRailFields.getFailedMessage());
            comment.append(error);
        }
        return comment.toString();
    }

}
