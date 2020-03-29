package de.funkedigital.autotagging.entities.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "urlset", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
public class UrlSet {

    private List<Url> urls;

    public List<Url> getUrls() {
        return urls;
    }

    @XmlElement(name = "url")
    public void setUrls(List<Url> urls) {
        this.urls = urls;
    }
}
