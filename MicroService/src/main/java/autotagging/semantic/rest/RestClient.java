package de.funkedigital.autotagging.semantic.rest;

import de.funkedigital.autotagging.semantic.exceptions.RestClientException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.exception.ExceptionUtils;
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

/**
 * This class gives a facade to work with {@link RestTemplate}
 *
 * @param <U> Type of Request
 * @param <T> Type of Response
 */
@Component
public class RestClient<U, T> {

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
     * This method uses {@link RestTemplate} to GET request
     */
    public T getRequest(String url, String login, String password, Class<T[]> resClass) {

        try {
            // Set header to HTTP entity before making the request.
            HttpEntity<String> entity = new HttpEntity<>(getHttpHeaders(login, password));

            // Request is sent {GET, Headers (Credentials, and ContentType = Json), type of domain class}
            ResponseEntity<T[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, resClass);
            LOG.debug("response received : {} : {}", url, response);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody().length != 0) {
                // Response is array of Json.
                return response.getBody()[0];
            } else {
                throw new RestClientException("Empty response received from rest service : " + url,
                        HttpStatus.UNPROCESSABLE_ENTITY);
            }
        } catch (HttpClientErrorException e) {
            throw new RestClientException("Error occurred while getting results from Rest service : " + url, e,
                    e.getStatusCode());
        } catch (Exception e) {
            throw new RestClientException("URL : " + url + " : " + ExceptionUtils.getRootCauseMessage(e)
                    , e
                    , HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * This method uses {@link RestTemplate} to POST request
     */
    public T postRequest(String url, String user, String home, U request, Class<T> resClass) {

        try {
            // Set header to HTTP entity before making the request.
            HttpEntity<U> entity = new HttpEntity<>(request, postHttpHeaders(user, home));

            // Request is sent {GET, Headers (Credentials, and ContentType = Json), type of domain class}
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, entity, resClass);
            LOG.debug("response received : {} : {}", url, response);
            // Response is array of Json.
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RestClientException("Error occurred while posting results to rest service : " + url +
                    " and request " + request.toString(), e,
                    e.getStatusCode());
        } catch (Exception e) {
            throw new RestClientException("URL : " + url + " Request : " + request.toString() + " : "
                    + ExceptionUtils.getRootCauseMessage(e)
                    , e
                    , HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * Headers for GET request
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
     * Headers for POST request
     */
    private HttpHeaders postHttpHeaders(String user, String home) {
        HttpHeaders headers = new HttpHeaders();
        // Set headers
        headers.setContentType(MediaType.APPLICATION_JSON);
        // We need to encode the credentials before accessing the Webservice
        headers.set("Esc-User", user);
        headers.set("Esc-Home", home);
        return headers;
    }

    /**
     * encode credentials with BASE64
     */
    private void encodeAndSetCredentials(String login, String password, HttpHeaders headers) {
        String encodedString =
                new String(Base64.encodeBase64((login + ":" + password).getBytes()));
        headers.set("Authorization", "Basic " + encodedString);
    }
}
