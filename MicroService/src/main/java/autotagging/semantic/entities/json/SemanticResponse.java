package de.funkedigital.autotagging.semantic.entities.json;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "assetType",
        "assetId",
        "sourceId",
        "availabilityStartTime",
        "availabilityEndTime",
        "publicationTime",
        "lastUpdateTime",
        "durationSeconds",
        "titles",
        "programType",
        "genreBroad",
        "genreMedium",
        "keywords",
        "persons",
        "synopses",
        "clientCustomProperties"
})
public class SemanticResponse {

    @JsonProperty("assetType")
    private String assetType;
    @JsonProperty("assetId")
    private String assetId;
    @JsonProperty("sourceId")
    private String sourceId;
    @JsonProperty("availabilityStartTime")
    private String availabilityStartTime;
    @JsonProperty("availabilityEndTime")
    private String availabilityEndTime;
    @JsonProperty("publicationTime")
    private String publicationTime;
    @JsonProperty("lastUpdateTime")
    private String lastUpdateTime;
    @JsonProperty("durationSeconds")
    private Integer durationSeconds;
    @JsonProperty("titles")
    private Title title;
    @JsonProperty("programType")
    private String programType;
    @JsonProperty("genreBroad")
    private List<String> genreBroad = null;
    @JsonProperty("genreMedium")
    private List<String> genreMedium = null;
    @JsonProperty("keywords")
    private Keyword keyword;
    @JsonProperty("persons")
    private Person person;
    @JsonProperty("synopses")
    private Synopses synopses;
    @JsonProperty("clientCustomProperties")
    private ClientCustomProperties clientCustomProperties;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("assetType")
    public String getAssetType() {
        return assetType;
    }

    @JsonProperty("assetType")
    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    @JsonProperty("assetId")
    public String getAssetId() {
        return assetId;
    }

    @JsonProperty("assetId")
    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    @JsonProperty("sourceId")
    public String getSourceId() {
        return sourceId;
    }

    @JsonProperty("sourceId")
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    @JsonProperty("availabilityStartTime")
    public String getAvailabilityStartTime() {
        return availabilityStartTime;
    }

    @JsonProperty("availabilityStartTime")
    public void setAvailabilityStartTime(String availabilityStartTime) {
        this.availabilityStartTime = availabilityStartTime;
    }

    @JsonProperty("availabilityEndTime")
    public String getAvailabilityEndTime() {
        return availabilityEndTime;
    }

    @JsonProperty("availabilityEndTime")
    public void setAvailabilityEndTime(String availabilityEndTime) {
        this.availabilityEndTime = availabilityEndTime;
    }

    @JsonProperty("publicationTime")
    public String getPublicationTime() {
        return publicationTime;
    }

    @JsonProperty("publicationTime")
    public void setPublicationTime(String publicationTime) {
        this.publicationTime = publicationTime;
    }

    @JsonProperty("lastUpdateTime")
    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    @JsonProperty("lastUpdateTime")
    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @JsonProperty("durationSeconds")
    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    @JsonProperty("durationSeconds")
    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    @JsonProperty("titles")
    public Title getTitle() {
        return title;
    }

    @JsonProperty("titles")
    public void setTitle(Title title) {
        this.title = title;
    }

    @JsonProperty("programType")
    public String getProgramType() {
        return programType;
    }

    @JsonProperty("programType")
    public void setProgramType(String programType) {
        this.programType = programType;
    }

    @JsonProperty("genreBroad")
    public List<String> getGenreBroad() {
        return genreBroad;
    }

    @JsonProperty("genreBroad")
    public void setGenreBroad(List<String> genreBroad) {
        this.genreBroad = genreBroad;
    }

    @JsonProperty("genreMedium")
    public List<String> getGenreMedium() {
        return genreMedium;
    }

    @JsonProperty("genreMedium")
    public void setGenreMedium(List<String> genreMedium) {
        this.genreMedium = genreMedium;
    }

    @JsonProperty("keywords")
    public Keyword getKeyword() {
        return keyword;
    }

    @JsonProperty("keywords")
    public void setKeyword(Keyword keyword) {
        this.keyword = keyword;
    }

    @JsonProperty("persons")
    public Person getPerson() {
        return person;
    }

    @JsonProperty("persons")
    public void setPerson(Person person) {
        this.person = person;
    }

    @JsonProperty("synopses")
    public Synopses getSynopses() {
        return synopses;
    }

    @JsonProperty("synopses")
    public void setSynopses(Synopses synopses) {
        this.synopses = synopses;
    }

    @JsonProperty("clientCustomProperties")
    public ClientCustomProperties getClientCustomProperties() {
        return clientCustomProperties;
    }

    @JsonProperty("clientCustomProperties")
    public void setClientCustomProperties(ClientCustomProperties clientCustomProperties) {
        this.clientCustomProperties = clientCustomProperties;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("assetType", assetType)
                .append("assetId", assetId).append("sourceId", sourceId)
                .append("availabilityStartTime", availabilityStartTime)
                .append("availabilityEndTime", availabilityEndTime)
                .append("publicationTime", publicationTime)
                .append("lastUpdateTime", lastUpdateTime)
                .append("durationSeconds", durationSeconds)
                .append("titles", title)
                .append("programType", programType)
                .append("genreBroad", genreBroad)
                .append("genreMedium", genreMedium)
                .append("keywords", keyword)
                .append("persons", person)
                .append("synopses", synopses)
                .append("clientCustomProperties", clientCustomProperties)
                .append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(clientCustomProperties)
                .append(keyword)
                .append(lastUpdateTime)
                .append(person)
                .append(availabilityStartTime)
                .append(sourceId)
                .append(assetId)
                .append(durationSeconds)
                .append(publicationTime)
                .append(availabilityEndTime)
                .append(assetType)
                .append(additionalProperties)
                .append(title)
                .append(synopses)
                .append(programType)
                .append(genreBroad)
                .append(genreMedium).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SemanticResponse) == false) {
            return false;
        }
        SemanticResponse rhs = ((SemanticResponse) other);
        return new EqualsBuilder()
                .append(clientCustomProperties, rhs.clientCustomProperties)
                .append(keyword, rhs.keyword)
                .append(lastUpdateTime, rhs.lastUpdateTime)
                .append(person, rhs.person)
                .append(availabilityStartTime, rhs.availabilityStartTime)
                .append(sourceId, rhs.sourceId)
                .append(assetId, rhs.assetId)
                .append(durationSeconds, rhs.durationSeconds)
                .append(publicationTime, rhs.publicationTime)
                .append(availabilityEndTime, rhs.availabilityEndTime)
                .append(assetType, rhs.assetType)
                .append(additionalProperties, rhs.additionalProperties)
                .append(title, rhs.title)
                .append(synopses, rhs.synopses)
                .append(programType, rhs.programType)
                .append(genreBroad, rhs.genreBroad)
                .append(genreMedium, rhs.genreMedium).isEquals();
    }

}
