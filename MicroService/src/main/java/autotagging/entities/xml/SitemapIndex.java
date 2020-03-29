package de.funkedigital.autotagging.entities.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "sitemapindex", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
public class SitemapIndex {


    private List<Sitemap> sitemaps;

    public List<Sitemap> getSitemaps() {
        return sitemaps;
    }

    @XmlElement(name = "sitemap")
    public void setSitemaps(List<Sitemap> sitemaps) {
        this.sitemaps = sitemaps;
    }
}
