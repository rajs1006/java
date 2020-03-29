package de.funkedigital.autotagging.semantic.restclients;

import de.funkedigital.autotagging.exceptions.RestClientException;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class RestClient<T> {

    private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);

    /**
     * Constructor based Autowired Rest template, using {@link RestTemplateBuilder}
     */
    private final RestTemplate restTemplate;

    @Autowired
    public RestClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * @param url
     * @param login
     * @param password
     * @param klass
     * @return
     */
    public T getResponse(String url, String login, String password, Class<T[]> klass) {

        try {
            // Set header to HTTP entity before making the request.
            HttpEntity<String> entity = new HttpEntity<>(getHttpHeaders(login, password));

            // Request is sent {GET, Headers (Credentials, and ContentType = Json), type of domain class}
            ResponseEntity<T[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, klass);
            LOG.debug("response received : {} : {}", url, response);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody().length != 0) {
                // Response is array of Json.
                return response.getBody()[0];
            } else {
                throw new RestClientException("Empty response received from Semantic engine service : " + url, HttpStatus.UNPROCESSABLE_ENTITY);
            }
        } catch (HttpClientErrorException e) {
            throw new RestClientException("Error occurred while getting results from Semantic engine service : " + url, e,
                    e.getStatusCode());
        } catch (Exception e) {
            throw new RestClientException(e.getMessage() + " : " + url, e, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * @param login
     * @param password
     * @return
     */
    private HttpHeaders getHttpHeaders(String login, String password) {
        HttpHeaders headers = new HttpHeaders();
        // Set headers
        headers.setContentType(MediaType.APPLICATION_JSON);
        // We need to encode the credentials before accessing the Webservice
        encodeAndSetCredentials(login, password, headers);
        return headers;
    }

    /**
     * @param login
     * @param password
     * @param headers
     */
    private void encodeAndSetCredentials(String login, String password, HttpHeaders headers) {
        String encodedString =
                new String(Base64.encodeBase64((login + ":" + password).getBytes()));
        headers.set("Authorization", "Basic " + encodedString);
    }
}
