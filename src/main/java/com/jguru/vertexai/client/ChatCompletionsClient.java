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
 * Client for interacting with Vertex AI OpenAI-compatible Chat Completions API. Used for MaaS
 * (Model-as-a-Service) models that require the /chat/completions endpoint.
 */
public class ChatCompletionsClient {

  private static final Logger logger = LoggerFactory.getLogger(ChatCompletionsClient.class);

  private final String projectId;
  private final String location;
  private final GoogleCredentials credentials;
  private final Gson gson;

  /**
   * Constructor for Chat Completions client.
   *
   * @param projectId
   *          Google Cloud project ID
   * @param location
   *          Google Cloud location (e.g., us-central1)
   * @param credentials
   *          Google credentials for authentication
   */
  public ChatCompletionsClient(String projectId, String location, GoogleCredentials credentials) {
    this.projectId = projectId;
    this.location = location;
    this.credentials = credentials;
    this.gson = new Gson();
    logger.debug("ChatCompletionsClient initialized with projectId: {}, location: {}", projectId,
        location);
  }

  /**
   * Calls the Chat Completions API endpoint.
   *
   * @param modelName
   *          Full model name with provider prefix (e.g.,
   *          "qwen/qwen3-coder-480b-a35b-instruct-maas")
   * @param prompt
   *          User prompt text
   * @return Response text from the model
   * @throws IOException
   *           If the API call fails
   */
  public String generateContent(String modelName, String prompt) throws IOException {
    logger.debug("Generating content with model: {}", modelName);

    // Build the endpoint URL
    String host = "aiplatform.googleapis.com";
    String endpointLocation = location;
    if (location != null && !location.equalsIgnoreCase("global")) {
      host = location + "-aiplatform.googleapis.com";
    } else if (location != null && location.equalsIgnoreCase("global")) {
      endpointLocation = "global"; // Explicitly set for global endpoint
    }
    String endpoint = String.format(
        "https://%s/v1/projects/%s/locations/%s/endpoints/openapi/chat/completions", host,
        projectId, endpointLocation);

    logger.debug("Using endpoint: {}", endpoint);

    // Refresh credentials if needed
    AccessToken accessToken = credentials.getAccessToken();
    if (accessToken == null || accessToken.getExpirationTime().before(new Date())) {
      logger.debug("Refreshing credentials");
      credentials.refresh();
      accessToken = credentials.getAccessToken();
    }

    // Build request body
    JsonObject requestBody = new JsonObject();
    requestBody.addProperty("model", modelName);
    requestBody.addProperty("stream", false);

    JsonArray messages = new JsonArray();
    JsonObject message = new JsonObject();
    message.addProperty("role", "user");
    message.addProperty("content", prompt);
    messages.add(message);
    requestBody.add("messages", messages);

    // Make HTTP request
    URL url = URI.create(endpoint).toURL();
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    try {
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Authorization", "Bearer " + accessToken.getTokenValue());
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setDoOutput(true);

      // Write request body
      try (OutputStream os = conn.getOutputStream()) {
        byte[] input = gson.toJson(requestBody).getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
      }

      // Read response
      int responseCode = conn.getResponseCode();
      logger.debug("Received response code: {}", responseCode);

      if (responseCode >= 200 && responseCode < 300) {
        return parseSuccessResponse(conn);
      } else {
        throw new IOException(parseErrorResponse(conn, responseCode, modelName));
      }
    } finally {
      conn.disconnect();
    }
  }

  /**
   * Parses successful response from the API.
   */
  private String parseSuccessResponse(HttpURLConnection conn) throws IOException {
    StringBuilder response = new StringBuilder();
    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
      String responseLine;
      while ((responseLine = br.readLine()) != null) {
        response.append(responseLine.trim());
      }
    }

    logger.debug("Received successful response: {}", response.toString());

    // Parse JSON response
    JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);

    // Extract content from choices[0].message.content
    if (jsonResponse.has("choices") && jsonResponse.get("choices").isJsonArray()) {
      JsonArray choices = jsonResponse.getAsJsonArray("choices");
      if (choices.size() > 0) {
        JsonObject firstChoice = choices.get(0).getAsJsonObject();
        if (firstChoice.has("message")) {
          JsonObject message = firstChoice.getAsJsonObject("message");
          if (message.has("content")) {
            String content = message.get("content").getAsString();
            logger.debug("Extracted content from response: {}", content);
            return content;
          }
        }
      }
    }

    logger.warn("Unexpected response format: {}", response);
    throw new IOException("Unexpected response format: " + response);
  }

  /**
   * Parses error response from the API.
   */
  private String parseErrorResponse(HttpURLConnection conn, int responseCode, String modelName)
      throws IOException {
    StringBuilder error = new StringBuilder();
    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
      String responseLine;
      while ((responseLine = br.readLine()) != null) {
        error.append(responseLine.trim());
      }
    }

    logger.error("Received error response code {}: {}", responseCode, error.toString());

    String errorMessage = String.format("HTTP %d: %s", responseCode, error);

    // Try to extract error message from JSON
    try {
      JsonObject jsonError = gson.fromJson(error.toString(), JsonObject.class);
      if (jsonError.has("error")) {
        JsonObject errorObj = jsonError.getAsJsonObject("error");
        if (errorObj.has("message")) {
          String message = errorObj.get("message").getAsString();
          errorMessage = String.format("HTTP %d: %s", responseCode, message);
          // Add hint for MaaS model access issues
          if (responseCode == 404 && modelName != null && modelName.contains("-maas")) {
            errorMessage += " (Hint: Model may not be enabled in your GCP project. Check the model card and click 'Enable'.)";
          }
        }
      }
    } catch (Exception e) {
      // Keep the original error message if JSON parsing fails
      logger.debug("Failed to parse error response as JSON", e);
    }

    return errorMessage;
  }
}
