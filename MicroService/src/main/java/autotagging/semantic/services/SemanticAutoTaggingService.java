package de.funkedigital.autotagging.semantic.services;

import de.funkedigital.autotagging.domains.SemanticResponse;
import de.funkedigital.autotagging.semantic.restclients.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class SemanticAutoTaggingService {

    private static final Logger LOG = LoggerFactory.getLogger(SemanticAutoTaggingService.class);

    @Value("${semantic.engine.login}")
    private String login;

    @Value("${semantic.engine.password}")
    private String password;

    @Value("${semantic.engine.url}")
    private String url;

    /**
     * Constructor based Autowired RestClient, using {@link RestClient}.
     * It takes generic type object {@link SemanticResponse}
     */
    private final RestClient<SemanticResponse> restClient;

    @Autowired
    public SemanticAutoTaggingService(RestClient<SemanticResponse> restClient) {
        this.restClient = restClient;
    }

    public String returnKeywords(Long articleId) {
        // Url needs to be formatted before request is sent.
        // Format would be "http://funke.api.lab.watchmi.tv/asset?id=escenic-211714863"
        String url = String.format("%s%d", this.url, articleId);

        // Calling RestClient.getResponse for getting the response in Object type.
        SemanticResponse sr = this.restClient.getResponse(url, login, password, SemanticResponse[].class);
        LOG.debug("Object returned successfuly for article id {}", articleId);

        return sr.getKeyword().getDeu().toString();
    }
}
