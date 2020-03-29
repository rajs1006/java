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
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "canonical",
        "perLanguage"
})
public class Mntd {

    @JsonProperty("canonical")
    private String canonical;
    @JsonProperty("perLanguage")
    private PerLanguage perLanguage;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("canonical")
    public String getCanonical() {
        return canonical;
    }

    @JsonProperty("canonical")
    public void setCanonical(String canonical) {
        this.canonical = canonical;
    }

    @JsonProperty("perLanguage")
    public PerLanguage getPerLanguage() {
        return perLanguage;
    }

    @JsonProperty("perLanguage")
    public void setPerLanguage(PerLanguage perLanguage) {
        this.perLanguage = perLanguage;
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
        return new ToStringBuilder(this).append("canonical", canonical).append("perLanguage", perLanguage).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(additionalProperties).append(perLanguage).append(canonical).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Mntd) == false) {
            return false;
        }
        Mntd rhs = ((Mntd) other);
        return new EqualsBuilder().append(additionalProperties, rhs.additionalProperties).append(perLanguage, rhs.perLanguage).append(canonical, rhs.canonical).isEquals();
    }
}