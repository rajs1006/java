
package de.funkedigital.autotagging.entities.web;

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
        "articleUrl",
        "publication"
})
public class Request {

    @JsonProperty("articleUrl")
    private String articleUrl;
    @JsonProperty("publication")
    private String publication;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();


    @JsonProperty("articleUrl")
    public String getArticleUrl() {
        return articleUrl;
    }

    @JsonProperty("articleUrl")
    public void setArticleUrl(String articleUrl) {
        this.articleUrl = articleUrl;
    }

    @JsonProperty("publication")
    public String getPublication() {
        return publication;
    }

    @JsonProperty("publication")
    public void setPublication(String publication) {
        this.publication = publication;
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
        return new ToStringBuilder(this)
                .append("articleUrl", articleUrl)
                .append("publication", publication)
                .append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(additionalProperties)
                .append(publication)
                .append(articleUrl).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Request)) {
            return false;
        }
        Request rhs = ((Request) other);
        return new EqualsBuilder()
                .append(additionalProperties, rhs.additionalProperties)
                .append(publication, rhs.publication)
                .append(articleUrl, rhs.articleUrl).isEquals();
    }

}