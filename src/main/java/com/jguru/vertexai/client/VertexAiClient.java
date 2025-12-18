package com.jguru.vertexai.client;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.jguru.vertexai.service.dto.AuthenticationConfig;
import com.jguru.vertexai.service.dto.AuthenticationType;
import com.jguru.vertexai.service.dto.GenerationResult;
import com.jguru.vertexai.utils.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Client for interacting with Google GenAI and Vertex AI. Supports both API Key and Service Account authentication. Automatically routes
 * MaaS models through Chat Completions API.
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
    this(AuthenticationConfig.builder().withType(AuthenticationType.API_KEY).withApiKey(apiKey).build());
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
    this(AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_ADC).withProjectId(projectId).withLocation(location)
        .build());
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
    this(AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_EXPLICIT_KEY).withSaKeyFile(serviceAccountKeyPath)
        .withProjectId(projectId).withLocation(location).build());
  }

  /**
   * Loads model properties for provider prefix mapping.
   */
  private Properties loadModelProperties() {
    return PropertiesLoader.load(logger, "models.config", "models.properties");
  }

  /**
   * Gets the provider prefix for a MaaS model. Returns the provider prefix or null if not a MaaS model.
   */
  private String getProviderPrefix(String modelName) {
    for (Object key : modelProperties.keySet()) {
      String keyStr = key.toString();
      if (keyStr.endsWith(".provider")) {
        String modelAlias = keyStr.substring(0, keyStr.length() - 9);
        String fullModelName = modelProperties.getProperty(modelAlias);

        // Check if the input modelName matches either the full model name or the model alias
        if (fullModelName != null && (fullModelName.equals(modelName) || modelAlias.equals(modelName))) {
          return modelProperties.getProperty(keyStr);
        }
      }
    }
    return null;
  }

  /**
   * Gets the preferred region for a model from properties (e.g., "global"). Returns the region value or null if not specified.
   */
  private String getModelRegion(String modelName) {
    for (Object key : modelProperties.keySet()) {
      String keyStr = key.toString();
      if (keyStr.endsWith(".region")) {
        String modelAlias = keyStr.substring(0, keyStr.length() - 7);
        String fullModelName = modelProperties.getProperty(modelAlias);
        if (fullModelName != null && (fullModelName.equals(modelName) || modelAlias.equals(modelName))) {
          return modelProperties.getProperty(keyStr);
        }
      }
    }
    return null;
  }

  /**
   * Gets the API routing flag for a model (e.g., "rawPredict"). Returns the value or null if not specified.
   */
  private String getModelApi(String modelName) {
    for (Object key : modelProperties.keySet()) {
      String keyStr = key.toString();
      if (keyStr.endsWith(".api")) {
        String modelAlias = keyStr.substring(0, keyStr.length() - 4);
        String fullModelName = modelProperties.getProperty(modelAlias);
        if (fullModelName != null && (fullModelName.equals(modelName) || modelAlias.equals(modelName))) {
          return modelProperties.getProperty(keyStr);
        }
      }
    }
    return null;
  }

  /**
   * Loads Google credentials based on authentication configuration.
   *
   * @return GoogleCredentials with cloud-platform scope, or null if ADC should be used
   * @throws IOException
   *           if explicit key file cannot be loaded
   */
  private GoogleCredentials loadCredentials() throws IOException {
    if (authConfig.getType() == AuthenticationType.SERVICE_ACCOUNT_EXPLICIT_KEY) {
      try {
        return GoogleCredentials.fromStream(new FileInputStream(authConfig.getSaKeyFile()))
            .createScoped("https://www.googleapis.com/auth/cloud-platform");
      } catch (IOException e) {
        throw new IOException("Failed to load service account key from: " + authConfig.getSaKeyFile()
            + ". The file must be a valid JSON service account key. ADC fallback is disabled when --sa-key-file is specified.", e);
      }
    }
    return null;
  }

  /**
   * Resolves the effective location for a model, considering per-model region overrides.
   *
   * @param modelName
   *          The model name to check for region override
   * @return The effective location (either from model properties or CLI config)
   */
  private String resolveEffectiveLocation(String modelName) {
    String effectiveLocation = authConfig.getLocation();
    String modelRegion = getModelRegion(modelName);
    if (modelRegion != null && !modelRegion.isBlank()) {
      effectiveLocation = modelRegion;
    }
    return effectiveLocation;
  }

  /**
   * Builds a Vertex AI client with the appropriate configuration.
   *
   * @param credentials
   *          The credentials to use (null for ADC)
   * @param effectiveLocation
   *          The location to use for the client
   * @return Configured Client instance
   */
  private Client buildVertexAiClient(GoogleCredentials credentials, String effectiveLocation) {
    System.setProperty("GOOGLE_GENAI_USE_VERTEXAI", "true");
    System.setProperty("GOOGLE_CLOUD_PROJECT", authConfig.getProjectId());
    System.setProperty("GOOGLE_CLOUD_LOCATION", effectiveLocation);

    if (credentials != null) {
      return Client.builder().project(authConfig.getProjectId()).location(effectiveLocation).credentials(credentials).vertexAI(true)
          .build();
    } else {
      System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "");
      return Client.builder().project(authConfig.getProjectId()).location(effectiveLocation).vertexAI(true).build();
    }
  }

  /**
   * Calls the GenAI/Vertex AI API to generate content. Automatically routes MaaS models through Chat Completions API.
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

      // Resolve full model name early
      String fullModelName = modelProperties.getProperty(modelName, modelName);
      // Check for rawPredict API routing
      String apiFlag = getModelApi(fullModelName);
      boolean useRawPredict = "rawPredict".equalsIgnoreCase(apiFlag);

      // Also check for explicit OpenAI flag as fallback
      if (!useChatCompletions) {
        String openAiFlag = modelProperties.getProperty(modelName + ".openai");
        useChatCompletions = "true".equalsIgnoreCase(openAiFlag);
      }

      if (useRawPredict) {
        GenerationResult result = callRawPredictApi(fullModelName, text);
        return result.getText();
      } else if (useChatCompletions) {
        // Use Chat Completions API for MaaS models
        // If we have a providerPrefix from .provider property, use that
        // Otherwise, fall back to "openai" for models with .openai=true
        String provider = providerPrefix != null ? providerPrefix : "openai";
        GenerationResult result = callChatCompletionsApi(provider, fullModelName, text);
        return result.getText();
      } else {
        // Use standard Vertex AI API for Gemini and Llama models
        logger.debug("Routing model '{}' to standard Vertex AI API as '{}'", modelName, fullModelName);
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
  protected GenerationResult callStandardVertexAi(String modelName, String textPrompt) throws IOException {
    GoogleCredentials credentials = loadCredentials();
    String effectiveLocation = resolveEffectiveLocation(modelName);

    try (Client client = buildVertexAiClient(credentials, effectiveLocation)) {
      logger.debug("Invoking Vertex AI model '{}' with {} in project '{}' / location '{}'", modelName,
          credentials != null ? "explicit credentials" : "ADC", authConfig.getProjectId(), effectiveLocation);
      GenerateContentResponse response = client.models.generateContent(modelName, textPrompt, null);
      return GenerationResult.builder().withText(response.text()).build();
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
  protected GenerationResult callChatCompletionsApi(String provider, String modelName, String textPrompt) throws IOException {
    GoogleCredentials credentials = loadCredentials();

    // Use Chat Completions API for MaaS models
    logger.info("Using Chat Completions API for model: {} with provider: {}", modelName, provider);

    if (credentials == null) {
      // Use ADC for Chat Completions
      credentials = GoogleCredentials.getApplicationDefault().createScoped("https://www.googleapis.com/auth/cloud-platform");
    }

    ChatCompletionsClient chatClient = new ChatCompletionsClient(authConfig.getProjectId(), authConfig.getLocation(), credentials);

    // For models with OpenAI flag, prepend the provider prefix
    // For MaaS models, prepend the provider prefix
    // For google-openai models, use the model name as-is (it already contains "google/" prefix)
    String modelWithPrefix = modelName;
    if (!"google-openai".equalsIgnoreCase(provider)) {
      modelWithPrefix = provider + "/" + modelName;
      logger.info("Model name with provider: {}", modelWithPrefix);
    }

    String response = chatClient.generateContent(modelWithPrefix, textPrompt);
    return GenerationResult.builder().withText(response).build();
  }

  /**
   * Calls the rawPredict API endpoint for models configured with .api=rawPredict.
   *
   * @param fullModelName
   *          The full model name (e.g., "mistralai/codestral-2@001")
   * @param textPrompt
   *          The prompt text
   * @return GenerationResult containing the response
   * @throws IOException
   *           If the API call fails
   */
  protected GenerationResult callRawPredictApi(String fullModelName, String textPrompt) throws IOException {
    GoogleCredentials credentials = loadCredentials();

    if (credentials == null) {
      credentials = GoogleCredentials.getApplicationDefault().createScoped("https://www.googleapis.com/auth/cloud-platform");
    }

    String effectiveLocation = resolveEffectiveLocation(fullModelName);

    RawPredictClient rpc = new RawPredictClient(authConfig.getProjectId(), effectiveLocation, credentials);
    String response = rpc.rawPredict(fullModelName, textPrompt, 0);
    return GenerationResult.builder().withText(response).build();
  }
}
