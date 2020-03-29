package de.funkedigital.autotagging.escenic.entities;

import org.jsoup.nodes.Document;

public class EscenicStore {

    private Document document;

    private String eTag;

    public EscenicStore(Document document, String eTag) {
        this.document = document;
        this.eTag = eTag;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }

    public String geteTag() {
        return eTag;
    }
}
