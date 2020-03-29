package de.funkedigital.autotagging.escenic.services;

import de.funkedigital.autotagging.escenic.entities.EscenicStore;
import de.funkedigital.autotagging.escenic.exceptions.EscenicServiceException;
import de.funkedigital.autotagging.escenic.rest.EscenicClient;
import de.funkedigital.autotagging.semantic.entities.UnicornStore;
import de.funkedigital.autotagging.semantic.entities.json.Keyword;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


/**
 * This class interacts with Escenic web service
 *
 * @author sraj
 */
@Service
public class EscenicService {

    private static final Logger LOG = LoggerFactory.getLogger(EscenicService.class);

    /**
     * Field, updated in escenic,
     */
    private static final String KEYWORDS = "auto_Keywords";

    /**
     * Value tag for KEYWORDS
     */
    private static final String KEYWORDS_VALUE_TAG = "vdf:value";

    // Application.yml properties
    @Value("${escenic.tools.login}")
    private String login;

    @Value("${escenic.tools.password}")
    private String password;

    @Value("${escenic.tools.url}")
    private String url;

    /**
     * Autowired instance of {@link EscenicClient}
     */
    @Autowired
    private EscenicClient escenicClient;

    /**
     * This method gets document from Escenic, push keywords and then silently
     * push it back to Escenic using {@link EscenicClient}.
     * <p>
     * In this method, we either silently update the document or we don't update
     * it at all
     * <p>
     * attr("keep-last-modified", "true") is set to update the article silently,
     * however inspite of doing this, <b>Escenic maintains history with this update.</b>
     *
     * @param keywords  Keyword to be pushed,
     *                  {@link Keyword}
     * @param articleId Article, for which keywords need to be pushed.
     *                  {@link UnicornStore}
     */
    public void pushKeywords(String keywords, String articleId) {
        LOG.debug("Running pushKeywords {} : {} : {}", keywords, articleId
                , Thread.currentThread().getName());
        // Append article Id to url
        String contentUrl = String.format("%s%s", this.url, articleId);
        // Get document
        EscenicStore store = escenicClient.get(login, password, contentUrl);
        Document document = store.getDocument();
        // Update keyword
        Element keywordElement = this.getElementAttributeByKeyValue(document, "name", KEYWORDS);
        if (keywordElement != null) {
            // Populate value
            Element valueElement;
            if (keywordElement.getElementsByTag(KEYWORDS_VALUE_TAG).isEmpty()) {
                valueElement = new Element(KEYWORDS_VALUE_TAG);
            } else {
                valueElement = keywordElement.getElementsByTag(KEYWORDS_VALUE_TAG).first();
            }
            valueElement.text(keywords);
            // Add value
            keywordElement.append(valueElement.toString());

            // silent update
            Element lastModElement = this.getElementByTagName(document, "app:control");
            if (lastModElement != null) {
                // Set changed document to object. Do not update if lastModElement is null
                // as silent update is important.
                lastModElement.attr("keep-last-modified", "true");
                store.setDocument(document);
                // Put the document back to escenic
                escenicClient.put(login, password, contentUrl, store);
            } else {
                throw new EscenicServiceException("Error occurred while silently updating elements,  "
                        + "could not found 'app:control' element in " + contentUrl,
                        HttpStatus.NOT_ACCEPTABLE);
            }
        }
    }

    /**
     * Fetch elements using tag name from document.
     *
     * @param element Element from which the tag need to be fetched
     * @param tagName Tag name from document
     * @return {@link Elements}
     */
    private Element getElementByTagName(Element element, String tagName) {
        try {
            if (element != null && tagName != null) {
                Elements elements = element.getElementsByTag(tagName);
                if (elements != null && elements.size() > 0) {
                    return elements.get(0);
                }
            }
            return null;
        } catch (Exception e) {
            throw new EscenicServiceException("Error occurred while getting element by Tagname: "
                    + ExceptionUtils.getRootCauseMessage(e),
                    e,
                    HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Fetch elements using Attribute key-value from document.
     *
     * @param element Element from which the tag need to be fetched
     * @param key     key of attribute
     * @param value   value of attribute
     * @return {@link Elements}
     */
    private Element getElementAttributeByKeyValue(Element element, String key, String value) {
        try {
            if (element != null && key != null && value != null) {
                Elements attrElements = element.getElementsByAttributeValue(key, value);
                if (attrElements != null && attrElements.size() > 0) {
                    return attrElements.get(0);
                }
            }
            return null;
        } catch (Exception e) {
            throw new EscenicServiceException("Error occurred while getting element by attribute key-value: "
                    + ExceptionUtils.getRootCauseMessage(e),
                    e,
                    HttpStatus.NOT_FOUND);
        }
    }
}
