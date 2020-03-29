package de.funkedigital.autotagging.semantic.services;

import com.google.common.base.Splitter;
import de.funkedigital.autotagging.semantic.entities.KeywordStore;
import de.funkedigital.autotagging.semantic.entities.json.ClientCustomProperties;
import de.funkedigital.autotagging.semantic.entities.json.Keyword;
import de.funkedigital.autotagging.semantic.entities.json.Mntd;
import de.funkedigital.autotagging.semantic.entities.json.PerLanguage;
import de.funkedigital.autotagging.semantic.entities.json.SemanticResponse;
import de.funkedigital.autotagging.semantic.entities.json.Topic;
import de.funkedigital.autotagging.semantic.exceptions.SemanticServiceException;
import de.funkedigital.autotagging.semantic.rest.RestClient;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


/**
 * This service is to interact with Semantic Service
 *
 * @author sraj
 */
@Service
public class SemanticService {

    private static final Logger LOG = LoggerFactory.getLogger(SemanticService.class);

    // Properties from Application.yml
    @Value("${semantic.engine.login}")
    private String login;

    @Value("${semantic.engine.password}")
    private String password;

    @Value("${semantic.engine.url}")
    private String url;

    /**
     * Constructor based Autowired RestClient, using {@link RestClient}.
     * It takes generic type object {@link String} and {@link SemanticResponse}
     */
    private final RestClient<String, SemanticResponse> restClient;

    @Autowired
    public SemanticService(RestClient<String, SemanticResponse> restClient) {
        this.restClient = restClient;
    }

    /**
     * This method fetch keywords from Semantic Service using {@link RestClient}
     * which takes String as Request and return {@link SemanticResponse} as response.
     * <p>
     * {@link SemanticResponse} is json mapping of reponse returned by Semantic Service
     * which contains multiple fields.
     * <p>
     * Then we manipulate values from {@link SemanticResponse} and create comma separated
     * list of keywords.
     * <p>
     * This keyword consist of
     * <ul>
     * ----><li>{@link Keyword#getDeu()}, which contains keywords</li>
     * ----><li>{@link Mntd#getPerLanguage()} and then {@link PerLanguage#getDeu()}
     * which contains the Organisations returned by Service
     * ----></li>
     * ----><li>{@link ClientCustomProperties#getTopics()} and then {@link Topic#getName()}
     * which contains the topics returned by Service
     * ----></li>
     * </ul>
     *
     * @param assetId assetID returned from {@link UnicornService}
     * @return Collected keywords from Semantic service
     */
    public KeywordStore returnKeywords(String assetId) {
        // Url needs to be formatted before request is sent.
        // Format would be "http://funke.api.lab.watchmi.tv/asset?id=escenic-211714863"
        String url = String.format("%s%s", this.url, assetId);

        // Calling RestClient.getRequest for getting the response in Object type.
        SemanticResponse semanticResponse = this.restClient.getRequest(url, login, password, SemanticResponse[].class);
        LOG.debug("Object returned successfully for article id {}", assetId);

        // Create keywordStore to return
        return getKeywordStore(semanticResponse);
    }

    /**
     * Manipulate {@link SemanticResponse} and then return {@link KeywordStore}
     */
    private KeywordStore getKeywordStore(SemanticResponse semanticResponse) {
        try {
            KeywordStore keywordStore = new KeywordStore();
            if (semanticResponse.getKeyword() != null) {
                keywordStore.setKeywords(semanticResponse.getKeyword().getDeu());
            }
            // set Organization
            if (semanticResponse.getPerson() != null) {
                for (Mntd mntd : semanticResponse.getPerson().getMntd()) {
                    keywordStore.getOrganization().add(mntd.getPerLanguage().getDeu());
                }
            }
            // set Topics
            if (semanticResponse.getClientCustomProperties() != null) {
                for (Topic topic : semanticResponse.getClientCustomProperties().getTopics()) {
                    keywordStore.getTopics().addAll(Splitter.on("/").splitToList(topic.getName()));
                }
            }
            return keywordStore;
        } catch (Exception e) {
            throw new SemanticServiceException("Error occurred while processing keywords for asset-id '"
                    + semanticResponse.getAssetId()+"' : " +
                    ExceptionUtils.getRootCauseMessage(e),
                    e,
                    HttpStatus.NO_CONTENT);
        }
    }
}
