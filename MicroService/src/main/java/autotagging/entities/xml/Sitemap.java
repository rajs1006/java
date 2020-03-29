package de.funkedigital.autotagging.entities.xml;

import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "sitemap")
@XmlAccessorType(XmlAccessType.FIELD)
public class Sitemap {

    private String loc;
    private String lastmod;

    public String getLoc() {
        return loc;
    }

    //@XmlElement
    public void setLoc(String loc) {
        this.loc = loc;
    }

    public String getLastmod() {
        return lastmod;
    }

    //@XmlElement
    public void setLastmod(String lastmod) {
        this.lastmod = lastmod;
    }

    public boolean isEmpty() {
        return StringUtils.isEmpty(this.getLoc());
    }
}
