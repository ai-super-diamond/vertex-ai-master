package com.jguru.vertexai.infrastructure.client;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Client for interacting with Vertex AI using the official Google Gen AI SDK. Replaces manual HTTP/JSON handling with idiomatic SDK calls.
 */
public class ChatCompletionsClient {

  private static final Logger logger = LoggerFactory.getLogger(ChatCompletionsClient.class);
  private static final int REQUEST_TIMEOUT_MILLIS = 30_000;
  private final Client client;

  /**
   * Constructor for Chat Completions client using the Gen AI SDK.
   *
   * @param projectId
   *          Google Cloud project ID
   * @param location
   *          Google Cloud location (e.g., us-central1)
   * @param credentials
   *          Google credentials for authentication
   */
  public ChatCompletionsClient(String projectId, String location, GoogleCredentials credentials) {
    this.client = Client.builder().project(projectId).location(location).credentials(credentials).vertexAI(true)
        .httpOptions(HttpOptions.builder().timeout(REQUEST_TIMEOUT_MILLIS).build()).build();
    logger.debug("ChatCompletionsClient initialized with Gen AI SDK (Project: {}, Location: {})", projectId, location);
  }

  /**
   * Calls the Vertex AI API via the Gen AI SDK.
   *
   * @param modelName
   *          Full model name (e.g., "gemini-1.5-flash")
   * @param prompt
   *          User prompt text
   * @return Response text from the model
   * @throws IOException
   *           If the SDK call fails or returns an error
   */
  public String generateContent(String modelName, String prompt) throws IOException {
    logger.debug("Generating content with model: {}", modelName);

    try {
      // The SDK handles endpoint construction, credential refreshing, and JSON
      // serialization
      GenerateContentResponse response = client.models.generateContent(modelName, prompt, null);
      String content = response.text();

      logger.debug("Extracted content from SDK response: {}", content);
      return content;
    } catch (Exception e) {
      logger.error("SDK generation failed for model {}: {}", modelName, e.getMessage());

      // Add MaaS specific hint for 404 errors similar to original implementation
      String message = e.getMessage();
      if (message != null && message.contains("404") && modelName.contains("-maas")) {
        message += " (Hint: Model may not be enabled in your GCP project. Check the model card and click 'Enable'.)";
      }

      throw new IOException("Failed to generate content: " + message, e);
    }
  }
}
