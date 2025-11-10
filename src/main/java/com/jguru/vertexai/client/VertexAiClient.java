package com.jguru.vertexai.client;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.jguru.vertexai.service.dto.AuthenticationConfig;
import com.jguru.vertexai.service.dto.AuthenticationType;
import com.jguru.vertexai.service.dto.GenerationResult;
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

        // Check if the input modelName matches either the full model name or the model alias
        if (fullModelName != null
            && (fullModelName.equals(modelName) || modelAlias.equals(modelName))) {
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
      // Check for MaaS models (including google-openai provider) - prioritize .provider property
      String providerPrefix = getProviderPrefix(modelName);
      boolean useChatCompletions = providerPrefix != null;
      if (providerPrefix != null) {
        logger.debug("Detected provider '{}' for model '{}'", providerPrefix, modelName);
      }

      // Also check for explicit OpenAI flag as fallback
      if (!useChatCompletions) {
        String openAiFlag = modelProperties.getProperty(modelName + ".openai");
        useChatCompletions = "true".equalsIgnoreCase(openAiFlag);
      }

      if (useChatCompletions) {
        // Use Chat Completions API for MaaS models
        // Get the full model name from the alias for API calls
        String fullModelName = modelProperties.getProperty(modelName, modelName);

        // If we have a providerPrefix from .provider property, use that
        // Otherwise, fall back to "openai" for models with .openai=true
        String provider = providerPrefix != null ? providerPrefix : "openai";
        GenerationResult result = callChatCompletionsApi(provider, fullModelName, text);
        return result.getText();
      } else {
        // Use standard Vertex AI API for Gemini and Llama models
        // Get the full model name from the alias for API calls
        String fullModelName = modelProperties.getProperty(modelName, modelName);
        logger.debug("Routing model '{}' to standard Vertex AI API as '{}'", modelName,
            fullModelName);
        GenerationResult result = callStandardVertexAi(fullModelName, text);
        return result.getText();
      }
    }
  }

  /**
   * Calls the standard Vertex AI API for Gemini and Llama models.
   *
   * @param modelName
   *          The model to use
   * @param textPrompt
   *          The prompt text
   * @return GenerationResult containing the response
   * @throws IOException
   *           If the API call fails
   */
  protected GenerationResult callStandardVertexAi(String modelName, String textPrompt)
      throws IOException {
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

    // Use standard Vertex AI API for Gemini and Llama models
    System.setProperty("GOOGLE_GENAI_USE_VERTEXAI", "true");
    System.setProperty("GOOGLE_CLOUD_PROJECT", authConfig.getProjectId());
    System.setProperty("GOOGLE_CLOUD_LOCATION", authConfig.getLocation());

    if (credentials != null) {
      // Build client with explicit credentials
      try (Client client = Client.builder().project(authConfig.getProjectId())
          .location(authConfig.getLocation()).credentials(credentials).vertexAI(true).build()) {
        logger.debug(
            "Invoking Vertex AI model '{}' with explicit credentials in project '{}' / location '{}'",
            modelName, authConfig.getProjectId(), authConfig.getLocation());
        GenerateContentResponse response = client.models.generateContent(modelName, textPrompt,
            null);
        return GenerationResult.builder().withText(response.text()).build();
      }
    } else {
      // Use ADC
      System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "");

      try (Client client = Client.builder().project(authConfig.getProjectId())
          .location(authConfig.getLocation()).vertexAI(true).build()) {
        logger.debug("Invoking Vertex AI model '{}' in project '{}' / location '{}'", modelName,
            authConfig.getProjectId(), authConfig.getLocation());
        GenerateContentResponse response = client.models.generateContent(modelName, textPrompt,
            null);
        return GenerationResult.builder().withText(response.text()).build();
      }
    }
  }

  /**
   * Calls the Chat Completions API for MaaS and OpenAI models.
   *
   * @param provider
   *          The provider name (e.g., "deepseek-ai", "openai")
   * @param modelName
   *          The model to use
   * @param textPrompt
   *          The prompt text
   * @return GenerationResult containing the response
   * @throws IOException
   *           If the API call fails
   */
  protected GenerationResult callChatCompletionsApi(String provider, String modelName,
      String textPrompt) throws IOException {
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

    // Use Chat Completions API for MaaS models
    logger.info("Using Chat Completions API for model: {} with provider: {}", modelName, provider);

    if (credentials == null) {
      // Use ADC for Chat Completions
      credentials = GoogleCredentials.getApplicationDefault()
          .createScoped("https://www.googleapis.com/auth/cloud-platform");
    }

    ChatCompletionsClient chatClient = new ChatCompletionsClient(authConfig.getProjectId(),
        authConfig.getLocation(), credentials);

    // For models with OpenAI flag, use the model name directly
    // For MaaS models, prepend the provider prefix
    // For google-openai models, use the model name as-is (it already contains "google/" prefix)
    String modelWithPrefix = modelName;
    if (!"openai".equalsIgnoreCase(provider) && !"google-openai".equalsIgnoreCase(provider)) {
      modelWithPrefix = provider + "/" + modelName;
      logger.info("Model name with provider: {}", modelWithPrefix);
    }

    String response = chatClient.generateContent(modelWithPrefix, textPrompt);
    return GenerationResult.builder().withText(response).build();
  }
}
