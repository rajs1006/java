package com.test.tools.testutil.testrail;

/**
 * This class is class representation of fields needed to GraphForPassedTest result in Test Rail
 * <p>
 * Created BY s.raj@funkedigital.de on 11-May-18
 */
public class TestRail {

    protected String runId;
    protected String caseId;
    protected String testId;
    protected int assignedUser;
    protected String[] defectList;

    private String publication = "";
    private String stage;

    private String failedPublication = "";


    public TestRail() {
    }


    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public int getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(int assignedUser) {
        this.assignedUser = assignedUser;
    }

    public String[] getDefectList() {
        return defectList;
    }

    public void setDefectList(String[] defectList) {
        this.defectList = defectList;
    }

    public String getPublication() {
        return publication;
    }

    public void setPublication(String publication) {
        this.publication += publication + " ";
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getFailedPublication() {
        return failedPublication;
    }

    public void setFailedPublication(String failedPublication) {
        this.failedPublication += failedPublication + " ";
    }
}
