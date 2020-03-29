package de.funkedigital.autotagging.entities.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "url")
@XmlAccessorType(XmlAccessType.FIELD)
public class Url {

    private String loc;

    private String lastmod;

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }
}
