package com.jguru.vertexai.client;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Client for calling Vertex AI rawPredict endpoint for Mistral AI models. Endpoint format:
 * https://{region}-aiplatform.googleapis.com/v1/projects/{project}/locations/{region}/publishers/mistralai/models/{model}:rawPredict
 */
public class RawPredictClient {

  private static final Logger logger = LoggerFactory.getLogger(RawPredictClient.class);

  private final String projectId;
  private final String location;
  private final GoogleCredentials credentials;
  private final Gson gson;

  public RawPredictClient(String projectId, String location, GoogleCredentials credentials) {
    this.projectId = projectId;
    this.location = location;
    this.credentials = credentials;
    this.gson = new Gson();
    logger.debug("RawPredictClient initialized with projectId: {}, location: {}", projectId, location);
  }

  /**
   * Calls rawPredict for the given Mistral model.
   *
   * @param fullModelName
   *          Full model identifier, e.g. "mistralai/codestral-2@001"
   * @param prompt
   *          User prompt text
   * @param temperature
   *          Temperature setting (nullable, default 0)
   * @return Raw JSON string of the response
   * @throws IOException
   *           if the HTTP request fails
   */
  public String rawPredict(String fullModelName, String prompt, Integer temperature) throws IOException {
    logger.debug("rawPredict with model: {}", fullModelName);

    // Derive short model id after provider prefix
    String modelId = fullModelName;
    int slashIdx = fullModelName.indexOf('/');
    if (slashIdx >= 0 && slashIdx < fullModelName.length() - 1) {
      modelId = fullModelName.substring(slashIdx + 1);
    }

    String host = location + "-aiplatform.googleapis.com";
    String endpoint = String.format("https://%s/v1/projects/%s/locations/%s/publishers/mistralai/models/%s:rawPredict", host, projectId,
        location, modelId);

    logger.debug("Using endpoint: {}", endpoint);

    // Refresh credentials if needed
    AccessToken accessToken = credentials.getAccessToken();
    if (accessToken == null || accessToken.getExpirationTime() == null || accessToken.getExpirationTime().before(new Date())) {
      logger.debug("Refreshing credentials");
      credentials.refresh();
      accessToken = credentials.getAccessToken();
    }

    // Build request body per model card example (messages format)
    JsonObject requestBody = new JsonObject();
    requestBody.addProperty("model", modelId);
    requestBody.addProperty("temperature", temperature != null ? temperature : 0);

    JsonArray messages = new JsonArray();
    JsonObject userMsg = new JsonObject();
    userMsg.addProperty("role", "user");
    userMsg.addProperty("content", prompt);
    messages.add(userMsg);
    requestBody.add("messages", messages);

    URL url = URI.create(endpoint).toURL();
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    try {
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Authorization", "Bearer " + accessToken.getTokenValue());
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setDoOutput(true);

      try (OutputStream os = conn.getOutputStream()) {
        byte[] input = gson.toJson(requestBody).getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
      }

      int responseCode = conn.getResponseCode();
      logger.debug("rawPredict response code: {}", responseCode);

      if (responseCode >= 200 && responseCode < 300) {
        return parseSuccessResponse(conn);
      } else {
        throw new IOException(parseErrorResponse(conn, responseCode));
      }
    } finally {
      conn.disconnect();
    }
  }

  private String parseSuccessResponse(HttpURLConnection conn) throws IOException {
    StringBuilder response = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = br.readLine()) != null) {
        response.append(line.trim());
      }
    }
    logger.debug("rawPredict success response: {}", response);
    return response.toString();
  }

  private String parseErrorResponse(HttpURLConnection conn, int responseCode) throws IOException {
    StringBuilder error = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = br.readLine()) != null) {
        error.append(line.trim());
      }
    }
    logger.error("rawPredict error {}: {}", responseCode, error);
    return String.format("HTTP %d: %s", responseCode, error);
  }
}
