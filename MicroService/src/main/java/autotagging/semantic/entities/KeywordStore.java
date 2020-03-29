package de.funkedigital.autotagging.semantic.entities;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KeywordStore {

    private static final String SEP = ",";

    private List<String> keywords;

    private List<String> organization = new ArrayList<>();

    private Set<String> topics = new HashSet<>();

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getOrganization() {
        return organization;
    }

    public void setOrganization(List<String> organization) {
        this.organization = organization;
    }

    public Set<String> getTopics() {
        return topics;
    }

    public void setTopics(Set<String> topics) {
        this.topics = topics;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        if (this.keywords != null) {
            str.append(StringUtils.join(this.keywords, SEP) + SEP);
        }
        if (this.organization.size() != 0) {
            str.append(StringUtils.join(this.organization, SEP) + SEP);
        }

        if (this.topics.size() != 0) {
            str.append(StringUtils.join(this.topics, SEP));
        }
        return str.toString();
    }
}
