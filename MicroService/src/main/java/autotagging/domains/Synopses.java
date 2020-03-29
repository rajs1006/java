package de.funkedigital.autotagging.domains;

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
        "short",
        "long"
})
public class Synopses {

    @JsonProperty("short")
    private Short _short;
    @JsonProperty("long")
    private Long _long;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("short")
    public Short getShort() {
        return _short;
    }

    @JsonProperty("short")
    public void setShort(Short _short) {
        this._short = _short;
    }

    @JsonProperty("long")
    public Long getLong() {
        return _long;
    }

    @JsonProperty("long")
    public void setLong(Long _long) {
        this._long = _long;
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
        return new ToStringBuilder(this).append("_short", _short).append("_long", _long).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(_long).append(additionalProperties).append(_short).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Synopses) == false) {
            return false;
        }
        Synopses rhs = ((Synopses) other);
        return new EqualsBuilder().append(_long, rhs._long).append(additionalProperties, rhs.additionalProperties).append(_short, rhs._short).isEquals();
    }

}
