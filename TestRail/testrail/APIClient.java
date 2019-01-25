package com.test.tools.testutil.testrail;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;


/**
 * This class is provided by the testrail-api2 for JAVA binding and downloaded from
 * location "http://docs.gurock.com/testrail-api2/bindings-java".
 * <p>
 * There are few changes made in this class to allign this with Funke Digital code Standards.
 * <p>
 * TestRail API binding for Java (API v2, available since TestRail 3.0)
 * <p>
 * Learn more:
 * <p>
 * http://docs.gurock.com/testrail-api2/start
 * http://docs.gurock.com/testrail-api2/accessing
 * <p>
 * Copyright Gurock Software GmbH. See license.md for details.
 */
public class APIClient {

    private static final String UTF = "UTF-8";

    private String m_user;
    private String m_password;
    private String m_url;


    public APIClient(String base_url) {
        if (!base_url.endsWith("/")) {
            base_url += "/";
        }

        this.m_url = base_url + "index.php?/api/v2/";
    }

    /**
     * Get/Set User
     * <p>
     * Returns/sets the user used for authenticating the API requests.
     */
    public String getUser() {
        return this.m_user;
    }

    public void setUser(String user) {
        this.m_user = user;
    }

    /**
     * Get/Set Password
     * <p>
     * Returns/sets the password used for authenticating the API requests.
     */
    public String getPass() {
        return this.m_password;
    }

    public void setPassword(String password) {
        this.m_password = password;
    }

    /**
     * Send Get
     * <p>
     * Issues a GET request (read) against the API and returns the result
     * (as Object, see below).
     * <p>
     * Arguments:
     * <p>
     * uri                  The API method to call including parameters
     * (e.g. get_case/1)
     * <p>
     * Returns the parsed JSON response as standard object which can
     * either be an instance of JSONObject or JSONArray (depending on the
     * API method). In most cases, this returns a JSONObject instance which
     * is basically the same as java.util.Map.
     */
    public Object sendGet(String uri) throws IOException, TestRailAPIException {
        return this.sendRequest("GET", uri, null);
    }

    /**
     * Send POST
     * <p>
     * Issues a POST request (write) against the API and returns the result
     * (as Object, see below).
     * <p>
     * Arguments:
     * <p>
     * uri                  The API method to call including parameters
     * (e.g. add_case/1)
     * data                 The data to submit as part of the request (e.g.,
     * a map)
     * <p>
     * Returns the parsed JSON response as standard object which can
     * either be an instance of JSONObject or JSONArray (depending on the
     * API method). In most cases, this returns a JSONObject instance which
     * is basically the same as java.util.Map.
     */
    public Object sendPost(String uri, Object data) throws IOException, TestRailAPIException {
        return this.sendRequest("POST", uri, data);
    }

    /**
     * Tis method handles the Request and HTTPResponse
     *
     * @param method Post/Get etc.
     * @param uri    URI of TestRail
     * @param data   data to be sent in post request.
     * @return JsonObject
     * @throws IOException          throws {@link IOException}
     * @throws TestRailAPIException Throws Customized exception {@link TestRailAPIException}
     */
    private Object sendRequest(String method, String uri, Object data) throws IOException, TestRailAPIException {

        HttpURLConnection conn = null;
        InputStream iStream = null;
        Object result;

        try {
            URL url = new URL(this.m_url + uri);
            // Create the connection object and set the required HTTP method
            // (GET/POST) and headers (content type and basic auth).
            conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("Content-Type", "application/json");

            String auth = getAuthorization(this.m_user, this.m_password);
            conn.addRequestProperty("Authorization", "Basic " + auth);
            // Handles post request.
            handlePostRequest(method, conn, data);

            // Execute the actual web request (if it wasn't already initiated
            // by getOutputStream above) and record any occurred errors (we use
            // the error stream in this case).
            int status = conn.getResponseCode();

            if (status != 200) {
                iStream = conn.getErrorStream();
                if (iStream == null) {
                    throw new TestRailAPIException(
                            "TestRail API return HTTP " + status +
                                    " (No additional error message received)"
                    );
                }
            } else {
                iStream = conn.getInputStream();
            }

            // Read the response body, if any, and deserialize it from JSON.
            String text = readJsonResponse(iStream);

            result = getJsonResult(text);
            // Check for any occurred errors and add additional details to
            // the exception message, if any (e.g. the error message returned
            // by TestRail).
            checkForError(status, result);
        } finally {
            if (conn != null) conn.disconnect();
            if (iStream != null) iStream.close();
        }

        return result;
    }

    /**
     * This method handles the POST reques to TestRail
     *
     * @param conn Object of {@link HttpURLConnection}
     * @throws IOException throws {@link IOException}
     */
    private void handlePostRequest(String method, HttpURLConnection conn, Object data) throws IOException {
        conn.setDoOutput(true);
        try (OutputStream oStream = conn.getOutputStream();) {
            if ("POST".equalsIgnoreCase(method)) {
                // Add the POST arguments, if any. We just serialize the passed
                // data object (i.e. a dictionary) and then add it to the
                // request body.
                if (data != null) {
                    byte[] block = JSONValue.toJSONString(data).getBytes(UTF);

                    oStream.write(block);
                    oStream.flush();
                }
            }
        }
    }

    /**
     * This method reads the response body, if any, and deserialize it from JSON.
     *
     * @param istream Input Stream
     * @return De serialized json String
     * @throws IOException throws {@link IOException}
     */
    private String readJsonResponse(InputStream istream) throws IOException {
        StringBuilder text = new StringBuilder();
        if (istream != null) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(istream, UTF))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    text.append(line).append(System.getProperty("line.separator"));
                }
            }
        }
        return text.toString();
    }

    /**
     * This method return the JSon Object received from Testrail
     *
     * @param text Text it the input stream received from test rail
     * @return JasonObject.
     */
    private Object getJsonResult(String text) {
        if (!StringUtils.isEmpty(text)) {
            return JSONValue.parse(text);
        }
        return new JSONObject();
    }

    /**
     * This method checks if status is not 200, then extract the error message from TestRail
     * and throw it forwards
     *
     * @param status Status from TestRail 200/401 etc.
     * @param result Json Object received from TestRail.
     * @throws TestRailAPIException Throws the extracted Error.
     */
    private void checkForError(int status, Object result) throws TestRailAPIException {
        if (status != 200) {
            StringBuilder error = new StringBuilder("No additional error message received");
            if (result instanceof JSONObject) {
                JSONObject obj = (JSONObject) result;
                if (obj.containsKey("error")) {
                    error.append('"').append((String) obj.get("error")).append('"');
                }
            }

            throw new TestRailAPIException(
                    "TestRail API returned HTTP " + status + "(" + error.toString() + ")"
            );
        }
    }

    /**
     * This method returns the encrypted version of UserName and Password.
     *
     * @param user     UserName
     * @param password Password
     * @return Encrypted credentials
     * @throws UnsupportedEncodingException Throws exception if String can not be converted in UTF-* encoding.
     */
    private static String getAuthorization(String user, String password) throws UnsupportedEncodingException {
        return Base64.getEncoder().encodeToString((user + ":" + password).getBytes(UTF));
    }

}
