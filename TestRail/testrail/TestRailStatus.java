package com.test.tools.testutil.testrail;

/**
 * This is enum to support the required status from Test rail.
 * <p>
 * For more details : "http://docs.gurock.com/testrail-api2/reference-results#add_result"
 * <p>
 * Created BY s.raj@funkedigital.de on 11-May-18
 */
public enum TestRailStatus {

    /**
     * Status indicating the test case is passed
     */
    PASSED(1, "passed"),
    /**
     * Status indicating the test case is blocked for execution
     */
    BLOCKED(2, "Blocked"),
    /**
     * Status indicating the test case is untested
     */
    UNTESTED(3, "Untested"),
    /**
     * Status indicating the test case is in Re-test status
     */
    RETEST(4, "Retest"),
    /**
     * Status indicating the test case is passed
     */
    FAILED(5, "Failed");

    private int statusVal;

    private String status;

    /**
     * Constructor to populate enum fields.
     *
     * @param statusVal The value {@link TestRailStatus#statusVal}
     * @param status    The value {@link TestRailStatus#status}
     */
    TestRailStatus(int statusVal, String status) {
        this.statusVal = statusVal;
        this.status = status;
    }

    /**
     * Method to return val.
     *
     * @return The value {@link TestRailStatus#statusVal}
     */
    public int val() {
        return statusVal;
    }

    /**
     * Method to return status
     *
     * @return The value {@link TestRailStatus#status}
     */
    public String status() {
        return status;
    }

}
