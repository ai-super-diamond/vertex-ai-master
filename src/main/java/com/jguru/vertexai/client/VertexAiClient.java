package com.jguru.vertexai.client;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.jguru.vertexai.service.dto.AuthenticationConfig;
import com.jguru.vertexai.service.dto.AuthenticationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Client for interacting with Google GenAI and Vertex AI. Supports both API Key and Service Account
 * authentication. Automatically routes MaaS models through Chat Completions API.
 */
public class VertexAiClient {

  private final AuthenticationConfig authConfig;
  private final Properties modelProperties;

  private static final Logger logger = LoggerFactory.getLogger(VertexAiClient.class);

  public VertexAiClient(AuthenticationConfig authConfig) {
    this.authConfig = authConfig;
    this.modelProperties = loadModelProperties();
  }

  /**
   * Constructor for API Key authentication.
   *
   * @param apiKey
   *          The API key for authentication
   * @deprecated Use {@link #VertexAiClient(AuthenticationConfig)} instead.
   */
  @Deprecated
  public VertexAiClient(String apiKey) {
    this(AuthenticationConfig.builder().withType(AuthenticationType.API_KEY).withApiKey(apiKey)
        .build());
  }

  /**
   * Constructor for Service Account authentication via Vertex AI.
   *
   * @param projectId
   *          Google Cloud project ID
   * @param location
   *          Google Cloud location (e.g., us-central1)
   * @deprecated Use {@link #VertexAiClient(AuthenticationConfig)} instead.
   */
  @Deprecated
  public VertexAiClient(String projectId, String location) {
    this(AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_ADC)
        .withProjectId(projectId).withLocation(location).build());
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
   * @deprecated Use {@link #VertexAiClient(AuthenticationConfig)} instead.
   */
  @Deprecated
  public VertexAiClient(String serviceAccountKeyPath, String projectId, String location) {
    this(AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_EXPLICIT_KEY)
        .withSaKeyFile(serviceAccountKeyPath).withProjectId(projectId).withLocation(location)
        .build());
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
      logger.warn("Failed to load models.properties: {}", e.getMessage());
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
    if (authConfig.getType() == AuthenticationType.API_KEY) {
      // Use API Key authentication (Gemini API)
      try (Client client = Client.builder().apiKey(authConfig.getApiKey()).build()) {
        GenerateContentResponse response = client.models.generateContent(modelName, text, null);
        return response.text();
      }
    } else {
      // Check if this model should use Chat Completions API
      // First check for explicit OpenAI flag
      String openAiFlag = modelProperties.getProperty(modelName + ".openai");
      boolean useChatCompletions = "true".equalsIgnoreCase(openAiFlag);

      // If not explicitly set, check for MaaS models (backward compatibility)
      String providerPrefix = null;
      if (!useChatCompletions) {
        providerPrefix = getProviderPrefix(modelName);
        useChatCompletions = providerPrefix != null;
      }

      // Load credentials if explicit key path provided
      GoogleCredentials credentials = null;
      if (authConfig.getType() == AuthenticationType.SERVICE_ACCOUNT_EXPLICIT_KEY) {
        try {
          credentials = GoogleCredentials.fromStream(new FileInputStream(authConfig.getSaKeyFile()))
              .createScoped("https://www.googleapis.com/auth/cloud-platform");
        } catch (IOException e) {
          throw new IOException("Failed to load service account key from: "
              + authConfig.getSaKeyFile()
              + ". The file must be a valid JSON service account key. ADC fallback is disabled when --sa-key-file is specified.",
              e);
        }
      }

      if (useChatCompletions) {
        // Use Chat Completions API for MaaS models
        logger.info("Using Chat Completions API for model: {}", modelName);

        if (credentials == null) {
          // Use ADC for Chat Completions
          credentials = GoogleCredentials.getApplicationDefault()
              .createScoped("https://www.googleapis.com/auth/cloud-platform");
        }

        ChatCompletionsClient chatClient = new ChatCompletionsClient(authConfig.getProjectId(),
            authConfig.getLocation(), credentials);

        // For models with OpenAI flag, use the model name directly
        // For MaaS models, prepend the provider prefix
        String modelWithPrefix = modelName;
        if (providerPrefix != null) {
          modelWithPrefix = providerPrefix + "/" + modelName;
          logger.info("Model name with provider: {}", modelWithPrefix);
        }
        return chatClient.generateContent(modelWithPrefix, text);
      } else {
        // Use standard Vertex AI API for Gemini and Llama models
        System.setProperty("GOOGLE_GENAI_USE_VERTEXAI", "true");
        System.setProperty("GOOGLE_CLOUD_PROJECT", authConfig.getProjectId());
        System.setProperty("GOOGLE_CLOUD_LOCATION", authConfig.getLocation());

        if (credentials != null) {
          // Build client with explicit credentials
          try (Client client = Client.builder().project(authConfig.getProjectId())
              .location(authConfig.getLocation()).credentials(credentials).vertexAI(true).build()) {
            GenerateContentResponse response = client.models.generateContent(modelName, text, null);
            return response.text();
          }
        } else {
          // Use ADC
          System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "");

          try (Client client = Client.builder().project(authConfig.getProjectId())
              .location(authConfig.getLocation()).vertexAI(true).build()) {
            GenerateContentResponse response = client.models.generateContent(modelName, text, null);
            return response.text();
          }
        }
      }
    }
  }
}
