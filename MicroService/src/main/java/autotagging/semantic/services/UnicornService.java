package de.funkedigital.autotagging.semantic.services;

import de.funkedigital.autotagging.semantic.entities.UnicornStore;
import de.funkedigital.autotagging.semantic.entities.web.AssetRequest;
import de.funkedigital.autotagging.semantic.entities.web.AssetResponse;
import de.funkedigital.autotagging.semantic.exceptions.UnicornServiceException;
import de.funkedigital.autotagging.semantic.rest.RestClient;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Service to interact with Unicorn Service
 *
 * @author sraj
 */
@Service
public class UnicornService {

    private static final Logger LOG = LoggerFactory.getLogger(UnicornService.class);

    // Properties from Application.yml
    @Value("${unicorn.service.user}")
    private String login;

    @Value("${unicorn.service.home}")
    private String password;

    @Value("${unicorn.service.url}")
    private String url;

    /**
     * Constructor based Autowired RestClient, using {@link RestClient}.
     * It takes generic type object {@link AssetResponse}
     */
    private final RestClient<AssetRequest, AssetResponse> restClient;

    @Autowired
    public UnicornService(RestClient<AssetRequest, AssetResponse> restClient) {
        this.restClient = restClient;
    }

    /**
     * This method gets the Article analyzed by Unicorn service.
     * <p>
     * This is pre-step of {@link SemanticService}.
     * As, {@link SemanticService} needs assetId to fetch KeywordStore
     * and {@link this} service is used to fetch assetId from article URL
     *
     * @param articleUrl url of article to be analyzed by Unicorn service
     * @return {@link UnicornStore} containing returned AssetID
     */
    public UnicornStore analyzeArticle(String articleUrl) {
        LOG.debug("Running analyzeArticle {} : {}", articleUrl, Thread.currentThread().getName());
        // Prepare request
        AssetRequest request = new AssetRequest();
        // Process for UAT
        articleUrl = changeForUat(articleUrl);
        request.setUrl(articleUrl);

        // Calling RestClient.getRequest for getting the response in Object type.
        AssetResponse ar = this.restClient.postRequest(this.url, login, password, request, AssetResponse.class);
        LOG.debug("Object returned successfuly for article id {}", articleUrl);

        return new UnicornStore(ar.getAssetid());
    }

    /**
     * <b> This step is temporary </b>
     * <p>
     * Unicorn service can not access the UAT articles and
     * so to run this service for UAT articles we change the domain to WWW (production)
     * Assuming that UAT datastore is replicated from Production and so content would
     * be same.  This does not pose any threat on Production content, as this is readOnly
     * operation
     *
     * @param articleUrl Url to change
     * @return changed url, from uat to www.
     */
    private String changeForUat(String articleUrl) {
        try {
            if (articleUrl.contains("uat")) {
                articleUrl = articleUrl.replace("uat", "www");
            }
            return articleUrl;
        } catch (Exception e) {
            throw new UnicornServiceException("Error occurred while analyzing Url " + articleUrl + ": " +
                    ExceptionUtils.getRootCauseMessage(e),
                    e,
                    HttpStatus.NO_CONTENT);
        }
    }
}
