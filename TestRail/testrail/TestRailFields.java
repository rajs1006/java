package de.funke.tools.testutil.testrail;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is class representation of fields needed to GraphForPassedTest result in Test Rail
 * <p>
 * Created BY s.raj@funkedigital.de on 11-May-18
 */
public class TestRailFields extends TestRail {

    private boolean status = true;
    private TestRailStatus statusEnum;
    private String comment = "";
    protected String failedMessage = "";
    private String version;
    private long elapsedTime;
    private List<Map<String, ?>> customFields;


    public TestRailFields(String testId, int assignedUser, String[] defectList) {
        this.testId = testId;
        this.assignedUser = assignedUser;
        this.defectList = defectList;
    }

    public TestRailFields(String runId, String caseId, int assignedUser, String[] defectList) {
        this.runId = runId;
        this.caseId = caseId;
        this.assignedUser = assignedUser;
        this.defectList = defectList;
    }


    public TestRailFields(boolean status, String comment, long timeElapsed, String buildVersion, String[] defectList, int assignedUser) {
        this.setStatus(status);
        this.comment = comment;
        this.elapsedTime = timeElapsed;
        this.version = buildVersion;
        this.defectList = defectList;
        this.assignedUser = assignedUser;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
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

    public TestRailStatus getStatusEnum() {
        return statusEnum;
    }

    public void setStatusEnum(TestRailStatus statusEnum) {
        this.statusEnum = statusEnum;
    }


    public String getFailedMessage() {
        return failedMessage;
    }

    public void setFailedMessage(String failedMessage) {
        this.failedMessage = failedMessage;
    }

    public String getComment() {
        return  StringUtils.isEmpty(comment) ? " Executed for : " + comment  :  comment;
    }

    public void setComment(String comment) {
        this.comment = comment ;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public String[] getDefectList() {
        return defectList;
    }

    public void setDefectList(String[] defectList) {
        this.defectList = defectList;
    }

    public int getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(int assignedUser) {
        this.assignedUser = assignedUser;
    }

    public List<Map<String, ?>> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(List<Map<String, ?>> customFields) {
        this.customFields = customFields;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    /**
     * This method return map of all the data to be passed to Test Rail.
     *
     * @return Map of field values.
     */
    public Map toMap() {
        Map data = new HashMap();

        if (this.isStatus()) {
            data.put("status_id", TestRailStatus.PASSED.val());
        } else {
            data.put("status_id", TestRailStatus.FAILED.val());
        }
        if (this.statusEnum != null) {
            data.put("status_id", statusEnum.val());
        }
        if (!StringUtils.isEmpty(this.comment)) {
            data.put("comment", this.comment);
        }
        if (!StringUtils.isEmpty(this.version)) {
            data.put("version", this.version);
        }
        if (this.elapsedTime != 0) {
            data.put("elapsed", TestRailUtils.milliSecToTimeSpan(this.elapsedTime));
        }
        if (!TestRailUtils.isArrayEmpty(this.defectList)) {
            data.put("defects", TestRailUtils.arrayToString(this.defectList));
        }
        if (this.assignedUser != 0) {
            data.put("assignedto_id", this.assignedUser);
        }
        if (!TestRailUtils.isListEmpty(this.customFields)) {
            data.put("custom_fields", this.customFields);
        }
        return data;
    }

}
