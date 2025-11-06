package com.jguru.vertexai.client;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Client for interacting with Google GenAI and Vertex AI. Supports both API Key and Service Account
 * authentication. Automatically routes MaaS models through Chat Completions API.
 */
public class VertexAiClient {

  private final String apiKey;
  private final String projectId;
  private final String location;
  private final String serviceAccountKeyPath;
  private final boolean isApiKeyAuth;
  private final Properties modelProperties;

  /**
   * Constructor for API Key authentication.
   *
   * @param apiKey
   *          The API key for authentication
   */
  public VertexAiClient(String apiKey) {
    this.apiKey = apiKey;
    this.projectId = null;
    this.location = null;
    this.serviceAccountKeyPath = null;
    this.isApiKeyAuth = true;
    this.modelProperties = loadModelProperties();
  }

  /**
   * Constructor for Service Account authentication via Vertex AI.
   *
   * @param projectId
   *          Google Cloud project ID
   * @param location
   *          Google Cloud location (e.g., us-central1)
   */
  public VertexAiClient(String projectId, String location) {
    this.apiKey = null;
    this.projectId = projectId;
    this.location = location;
    this.serviceAccountKeyPath = null;
    this.isApiKeyAuth = false;
    this.modelProperties = loadModelProperties();
  }

  /**
   * Constructor for Service Account authentication with explicit key path.
   *
   * @param serviceAccountKeyPath
   *          Path to service account JSON key
   * @param projectId
   *          Google Cloud project ID
   * @param location
   *          Google Cloud location
   */
  public VertexAiClient(String serviceAccountKeyPath, String projectId, String location) {
    this.apiKey = null;
    this.projectId = projectId;
    this.location = location;
    this.serviceAccountKeyPath = serviceAccountKeyPath;
    this.isApiKeyAuth = false;
    this.modelProperties = loadModelProperties();
  }

  /**
   * Loads model properties for provider prefix mapping.
   */
  private Properties loadModelProperties() {
    Properties props = new Properties();
    try (InputStream input = getClass().getClassLoader().getResourceAsStream("models.properties")) {
      if (input != null) {
        props.load(input);
      }
    } catch (IOException e) {
      System.err.println("[WARN] Failed to load models.properties: " + e.getMessage());
    }
    return props;
  }

  /**
   * Gets the provider prefix for a MaaS model. Returns the provider prefix or null if not a MaaS
   * model.
   */
  private String getProviderPrefix(String modelName) {
    for (Object key : modelProperties.keySet()) {
      String keyStr = key.toString();
      if (keyStr.endsWith(".provider")) {
        String modelAlias = keyStr.substring(0, keyStr.length() - 9);
        String fullModelName = modelProperties.getProperty(modelAlias);
        if (fullModelName != null && fullModelName.equals(modelName)) {
          return modelProperties.getProperty(keyStr);
        }
      }
    }
    return null;
  }

  /**
   * Calls the GenAI/Vertex AI API to generate content. Automatically routes MaaS models through
   * Chat Completions API.
   *
   * @param modelName
   *          The model to use (e.g., gemini-1.5-pro-001)
   * @param text
   *          The prompt text
   * @return Generated response text
   * @throws IOException
   *           If the API call fails
   */
  public String callVertexAi(String modelName, String text) throws IOException {
    if (isApiKeyAuth) {
      // Use API Key authentication (Gemini API)
      try (Client client = Client.builder().apiKey(apiKey).build()) {
        GenerateContentResponse response = client.models.generateContent(modelName, text, null);
        return response.text();
      }
    } else {
      // Check if this is a MaaS model requiring Chat Completions API
      String providerPrefix = getProviderPrefix(modelName);
      boolean useChatCompletions = providerPrefix != null;

      // Load credentials if explicit key path provided
      GoogleCredentials credentials = null;
      if (serviceAccountKeyPath != null) {
        try {
          credentials = GoogleCredentials.fromStream(new FileInputStream(serviceAccountKeyPath))
              .createScoped("https://www.googleapis.com/auth/cloud-platform");
        } catch (IOException e) {
          throw new IOException("Failed to load service account key from: " + serviceAccountKeyPath
              + ". The file must be a valid JSON service account key. ADC fallback is disabled when --sa-key-file is specified.",
              e);
        }
      }

      if (useChatCompletions) {
        // Use Chat Completions API for MaaS models
        System.err.println("[INFO] Using Chat Completions API for MaaS model: " + modelName);

        if (credentials == null) {
          // Use ADC for Chat Completions
          credentials = GoogleCredentials.getApplicationDefault()
              .createScoped("https://www.googleapis.com/auth/cloud-platform");
        }

        ChatCompletionsClient chatClient = new ChatCompletionsClient(projectId, location,
            credentials);
        String modelWithPrefix = providerPrefix + "/" + modelName;
        System.err.println("[INFO] Model name with provider: " + modelWithPrefix);
        return chatClient.generateContent(modelWithPrefix, text);
      } else {
        // Use standard Vertex AI API for Gemini and Llama models
        System.setProperty("GOOGLE_GENAI_USE_VERTEXAI", "true");
        System.setProperty("GOOGLE_CLOUD_PROJECT", projectId);
        System.setProperty("GOOGLE_CLOUD_LOCATION", location);

        if (credentials != null) {
          // Build client with explicit credentials
          try (Client client = Client.builder().project(projectId).location(location)
              .credentials(credentials).vertexAI(true).build()) {
            GenerateContentResponse response = client.models.generateContent(modelName, text, null);
            return response.text();
          }
        } else {
          // Use ADC
          System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "");

          try (Client client = Client.builder().project(projectId).location(location).vertexAI(true)
              .build()) {
            GenerateContentResponse response = client.models.generateContent(modelName, text, null);
            return response.text();
          }
        }
      }
    }
  }
}
