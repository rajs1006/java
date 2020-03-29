package de.funkedigital.autotagging.escenic.rest;

import de.funkedigital.autotagging.escenic.entities.EscenicStore;
import de.funkedigital.autotagging.escenic.exceptions.EscenicClientException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.BufferedInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link Jsoup} rest client to interact with Escenic web-service
 *
 * @author sraj
 */
@Component
public class EscenicClient {


    /**
     * This method is to get the document from Escenic web service
     *
     * @param login    user name to access Escenic.
     * @param password Password to access escenic
     * @param url      url of escenic web service
     * @return XML document fro escenic web service
     */
    public EscenicStore get(String login, String password, String url) {
        // Connect through Jsoup and GET the data.

        Connection.Response response = execute(login, url, null,
                getHeaders(login, password), Connection.Method.GET);
        // Same value is used in header "if-match" while put/post operation.
        String eTag = response.header("Etag");
        Document doc = null;
        try (BufferedInputStream content = response.bodyStream()) {
            // OS is the output setting of for XML file.
            doc = Jsoup.parse(content, "UTF-8", url, Parser.xmlParser())
                    .outputSettings(setOutputSetting());
        } catch (Exception e) {
            if (doc == null) {
                throw new EscenicClientException("URL : " + url + " : " + ExceptionUtils.getRootCauseMessage(e)
                        , e, HttpStatus.NO_CONTENT);
            }
        }
        return new EscenicStore(doc, eTag);
    }

    /**
     * This method it put the changed document back on Escenic.
     *
     * @param login    user name to access Escenic.
     * @param password Password to access escenic
     * @param url      url of escenic web service
     * @param store    {@link EscenicStore} to contain Document and header
     */
    public void put(String login, String password, String url, EscenicStore store) {
        if (store.getDocument() == null || store.getDocument().html().isEmpty()) {
            throw new EscenicClientException("Document can not be null/empty for PUT, (URL) : " + url
                    , HttpStatus.BAD_REQUEST);
        }
        // Connect through Jsoup and PUT the data.
        execute(login, url, store.getDocument(), putHeaders(login, password, store.geteTag()), Connection.Method.PUT);
    }

    /**
     * This method is used to put/post data on the webservice after processing
     *
     * @param url     url of the document.
     * @param doc     processed Doc, need to be pushed
     * @param headers Headers needed for the Posting/Putting the data.
     * @param method  Whether you are performing POST/PUT/GET/etc {@link Connection.Method}
     * @return The response {@link Connection.Response}
     */
    private Connection.Response execute(String login, String url, Document doc, Map<String, String> headers,
                                        Connection.Method method) {
        try {
            Connection conn = Jsoup.connect(url).method(method)
                    //ignoreContentType Only Application-XML/APPLICATION
                    // mimetype is supported, for any other mimetype mark this as TRUE.
                    .ignoreContentType(Boolean.TRUE);
            // Set header
            if (headers != null && !headers.isEmpty()) {
                conn.headers(headers);
            }
            // Set body
            if (doc != null && !doc.html().isEmpty()) {
                conn.requestBody(doc.toString());
            }
            return conn.execute();
        } catch (Exception ie) {
            throw new EscenicClientException("Could not execute '" + method + "' request for user '" + login
                    + "' and url " + url + " :" + ExceptionUtils.getRootCauseMessage(ie),
                    ie, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * This output setting is specific to parse the document from escenic.
     */
    @NotNull
    private Document.OutputSettings setOutputSetting() {
        Document.OutputSettings os = new Document.OutputSettings();
        os.prettyPrint(false);
        os.escapeMode(Entities.EscapeMode.xhtml);
        os.syntax(Document.OutputSettings.Syntax.xml);
        return os;
    }

    /**
     * Headers need to {@link #get(String, String, String)}
     *
     * @return {@link Map<String, String>} header map
     */
    @NotNull
    private Map<String, String> getHeaders(String login, String password) {
        return headers(login, password);
    }

    /**
     * Headers need to be passe while executing PUT method {@link #put(String, String, String, EscenicStore)}
     *
     * @param eTag this tag is received in {@link #get(String, String, String)} and used same value while
     *             pushing the request body to Escenic in header <b>if-match</b>.
     *             If <b>if-match</b> does not match with the recieved Etag, the push will fail.
     * @return {@link Map<String, String>} header map
     */
    @NotNull
    private Map<String, String> putHeaders(String login, String password, String eTag) {

        Map<String, String> headers = headers(login, password);
        headers.put("If-Match", eTag);
        return headers;
    }

    /**
     * Common Headers
     */
    private Map<String, String> headers(String login, String password) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic " + encodeAndSetCredentials(login, password));
        headers.put("Content-Type", "application/atom+xml");
        return headers;
    }

    /**
     * BASE64 encoded username, password
     */
    private String encodeAndSetCredentials(String login, String password) {
        return new String(Base64.encodeBase64((login + ":" + password).getBytes()));
    }
}
