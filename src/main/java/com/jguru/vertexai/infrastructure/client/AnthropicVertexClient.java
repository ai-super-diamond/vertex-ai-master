package com.jguru.vertexai.infrastructure.client;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Client for Anthropic Claude models on Vertex AI. Anthropic models are not exposed through the standard GenAI SDK {@code generateContent}
 * surface (Vertex rejects it with "not supported in the generateContent API"); they must be called through the native {@code rawPredict}
 * REST endpoint using Anthropic's Messages request/response format.
 */
public class AnthropicVertexClient {

  private static final Logger logger = LoggerFactory.getLogger(AnthropicVertexClient.class);
  private static final String ANTHROPIC_VERSION = "vertex-2023-10-16";
  private static final int MAX_TOKENS = 1024;
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

  private final String projectId;
  private final String location;
  private final GoogleCredentials credentials;
  private final HttpClient httpClient;

  public AnthropicVertexClient(String projectId, String location, GoogleCredentials credentials) {
    this.projectId = projectId;
    this.location = location;
    this.credentials = credentials;
    this.httpClient = HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build();
  }

  /**
   * Calls the Anthropic Messages API via Vertex AI's rawPredict endpoint.
   *
   * @param modelName
   *          The Anthropic model resource name (e.g., "claude-sonnet-5@default")
   * @param prompt
   *          User prompt text
   * @return Generated response text
   * @throws IOException
   *           If the request fails or returns an error
   */
  public String generateContent(String modelName, String prompt) throws IOException {
    String host = resolveHost(location);
    String url = String.format("https://%s/v1/projects/%s/locations/%s/publishers/anthropic/models/%s:rawPredict", host, projectId,
        location, modelName);

    JsonObject message = new JsonObject();
    message.addProperty("role", "user");
    message.addProperty("content", prompt);
    JsonArray messages = new JsonArray();
    messages.add(message);

    JsonObject requestBody = new JsonObject();
    requestBody.addProperty("anthropic_version", ANTHROPIC_VERSION);
    requestBody.add("messages", messages);
    requestBody.addProperty("max_tokens", MAX_TOKENS);

    credentials.refreshIfExpired();
    String accessToken = credentials.getAccessToken().getTokenValue();

    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(REQUEST_TIMEOUT)
        .header("Authorization", "Bearer " + accessToken).header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString())).build();

    logger.debug("Calling Anthropic rawPredict endpoint: {}", url);

    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new IOException(response.statusCode() + " " + response.body());
      }
      return extractText(response.body());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Anthropic rawPredict request was interrupted", e);
    }
  }

  /**
   * Resolves the Vertex AI REST host for a given location. The "global" endpoint and the "us"/"eu" multi-regional endpoints use different
   * host formats than concrete regions (e.g., "us-central1").
   */
  private static String resolveHost(String location) {
    if ("global".equalsIgnoreCase(location)) {
      return "aiplatform.googleapis.com";
    }
    if ("us".equalsIgnoreCase(location) || "eu".equalsIgnoreCase(location)) {
      return String.format("aiplatform.%s.rep.googleapis.com", location.toLowerCase());
    }
    return location + "-aiplatform.googleapis.com";
  }

  private String extractText(String responseBody) throws IOException {
    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
    JsonArray content = json.getAsJsonArray("content");
    if (content == null) {
      throw new IOException("Unexpected Anthropic response, missing 'content': " + responseBody);
    }

    StringBuilder text = new StringBuilder();
    for (int i = 0; i < content.size(); i++) {
      JsonObject block = content.get(i).getAsJsonObject();
      if (block.has("text")) {
        text.append(block.get("text").getAsString());
      }
    }
    return text.toString();
  }
}
