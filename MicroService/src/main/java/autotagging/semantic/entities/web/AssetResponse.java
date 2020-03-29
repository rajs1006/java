package de.funkedigital.autotagging.semantic.entities.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "url"
})
public class AssetResponse {

    @JsonProperty("assetid")
    private String assetid;

    @JsonProperty("assetid")
    public String getAssetid() {
        return assetid;
    }

    @JsonProperty("assetid")
    public void setAssetid(String assetid) {
        this.assetid = assetid;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this).append("assetid", assetid).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(assetid).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AssetResponse) == false) {
            return false;
        }
        AssetResponse rhs = ((AssetResponse) other);
        return new EqualsBuilder().append(assetid, rhs.assetid).isEquals();
    }
}
