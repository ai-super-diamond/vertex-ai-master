package com.jguru.vertexai.infrastructure.client;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.jguru.vertexai.service.ModelClient;
import com.jguru.vertexai.domain.dto.AuthenticationConfig;
import com.jguru.vertexai.domain.dto.AuthenticationType;
import com.jguru.vertexai.domain.dto.GenerationResult;
import com.jguru.vertexai.domain.exception.ApiCallException;
import com.jguru.vertexai.utils.PropertiesLoader;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for interacting with Google GenAI and Vertex AI. Supports both API Key and Service Account authentication. Automatically routes
 * MaaS models through Chat Completions API.
 */
public class VertexAiClient implements ModelClient {

  private final AuthenticationConfig authConfig;
  private final Properties modelProperties;

  private static final Logger logger = LoggerFactory.getLogger(VertexAiClient.class);
  private static final int REQUEST_TIMEOUT_MILLIS = 30_000;

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

        // Check if the input modelName matches either the full model name or the model
        // alias
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
   * Loads Google credentials based on authentication configuration.
   *
   * @return GoogleCredentials with cloud-platform scope, or null if ADC should be used
   * @throws IOException
   *           if explicit key file cannot be loaded
   */
  protected GoogleCredentials loadCredentials() throws IOException {
    if (authConfig.getType() == AuthenticationType.SERVICE_ACCOUNT_EXPLICIT_KEY) {
      String keyFilePath = authConfig.getSaKeyFile();

      // Validate file path
      if (keyFilePath == null || keyFilePath.isBlank()) {
        throw new IllegalArgumentException("Service account key file path cannot be null or empty");
      }

      // Validate file exists and is readable
      java.nio.file.Path keyFile = java.nio.file.Paths.get(keyFilePath);
      if (!java.nio.file.Files.exists(keyFile)) {
        throw new IOException("Service account key file does not exist: " + keyFilePath);
      }

      if (!java.nio.file.Files.isReadable(keyFile)) {
        throw new IOException("Service account key file is not readable: " + keyFilePath);
      }

      // Additional security: Check file size (reasonable limits for JSON key files)
      try {
        long fileSize = java.nio.file.Files.size(keyFile);
        if (fileSize > 100 * 1024) {
          // 100KB limit
          throw new IOException("Service account key file appears to be too large: " + keyFilePath);
        }
      } catch (java.io.IOException e) {
        logger.warn("Could not determine file size for validation: {}", keyFilePath, e);
      }

      try {
        return GoogleCredentials.fromStream(java.nio.file.Files.newInputStream(keyFile))
            .createScoped("https://www.googleapis.com/auth/cloud-platform");
      } catch (IOException e) {
        throw new IOException(
            "Failed to load service account key. The file must be a valid JSON service account key. ADC fallback is disabled when --sa-key-file is specified.",
            e);
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
    if (authConfig.isSkipModelRegionOverride()) {
      return effectiveLocation;
    }
    String modelRegion = getModelRegion(modelName);
    if (modelRegion != null && !modelRegion.isBlank()) {
      effectiveLocation = modelRegion;
    }
    return effectiveLocation;
  }

  /**
   * Resolves the project ID to use, falling back to the project embedded in service account credentials when none is configured explicitly.
   *
   * @param credentials
   *          The credentials to use (may be null)
   * @return The resolved, non-blank project ID
   */
  private String resolveProjectId(GoogleCredentials credentials) {
    String projectId = authConfig.getProjectId();
    if ((projectId == null || projectId.isBlank()) && credentials instanceof com.google.auth.oauth2.ServiceAccountCredentials) {
      projectId = ((com.google.auth.oauth2.ServiceAccountCredentials) credentials).getProjectId();
      logger.debug("Extracted project ID from service account credentials: {}", projectId);
    }
    if (projectId == null || projectId.isBlank()) {
      throw new IllegalStateException("Project ID is required for Vertex AI client. "
          + "Use a service account key file that contains project_id or configure ADC with a project ID.");
    }
    return projectId;
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
    // Handle "global" location properly
    String clientLocation = effectiveLocation;
    if (effectiveLocation != null && effectiveLocation.equalsIgnoreCase("global")) {
      // For global location, we need to use the global endpoint
      clientLocation = "global";
    }

    String projectId = resolveProjectId(credentials);

    if (clientLocation == null || clientLocation.isBlank()) {
      throw new IllegalStateException("Location is required for Vertex AI client");
    }

    Client.Builder clientBuilder = Client.builder().project(projectId).location(clientLocation).vertexAI(true)
        .httpOptions(HttpOptions.builder().timeout(REQUEST_TIMEOUT_MILLIS).build());
    if (credentials != null) {
      clientBuilder = clientBuilder.credentials(credentials);
    }
    return clientBuilder.build();
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
  public String callVertexAi(String modelName, String text) throws ApiCallException {
    if (authConfig.getType() == AuthenticationType.API_KEY) {
      // Use API Key authentication (Gemini API)
      try (Client client = Client.builder().apiKey(authConfig.getApiKey())
          .httpOptions(HttpOptions.builder().timeout(REQUEST_TIMEOUT_MILLIS).build()).build()) {
        GenerateContentResponse response = client.models.generateContent(modelName, text, null);
        return response.text();
      } catch (Exception e) {
        ApiCallException.ErrorType errorType = ApiCallException.categorizeError(e.getMessage());
        throw new ApiCallException("API call failed: " + e.getMessage(), e, modelName, errorType);
      }
    } else {
      // Check for MaaS models (including google-openai provider) - prioritize
      // .provider property
      String providerPrefix = getProviderPrefix(modelName);
      boolean useChatCompletions = providerPrefix != null;
      if (providerPrefix != null) {
        logger.debug("Detected provider '{}' for model '{}'", providerPrefix, modelName);
      }

      // Resolve full model name early
      String fullModelName = modelProperties.getProperty(modelName, modelName);

      if ("anthropic".equalsIgnoreCase(providerPrefix)) {
        // Anthropic models are not exposed through the standard generateContent surface; they require the
        // native rawPredict/Messages API.
        GenerationResult result = callAnthropicVertexAi(fullModelName, text);
        return result.getText();
      }

      // Also check for explicit OpenAI flag as fallback
      if (!useChatCompletions) {
        String openAiFlag = modelProperties.getProperty(modelName + ".openai");
        useChatCompletions = "true".equalsIgnoreCase(openAiFlag);
      }

      if (useChatCompletions) {
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
  public GenerationResult callStandardVertexAi(String modelName, String textPrompt) throws ApiCallException {
    try {
      GoogleCredentials credentials = loadCredentials();
      String effectiveLocation = resolveEffectiveLocation(modelName);

      try (Client client = buildVertexAiClient(credentials, effectiveLocation)) {
        logger.debug("Invoking Vertex AI model '{}' with {} in project '{}' / location '{}'", modelName,
            credentials != null ? "explicit credentials" : "ADC", authConfig.getProjectId(), effectiveLocation);
        GenerateContentResponse response = client.models.generateContent(modelName, textPrompt, null);
        return GenerationResult.builder().withText(response.text()).build();
      }
    } catch (Exception e) {
      // Provide helpful error hints for common errors
      String errorMessage = e.getMessage();
      if (errorMessage != null) {
        if (errorMessage.contains("404") || errorMessage.contains("not found")) {
          errorMessage += " (Hint: Model may not be enabled in your GCP project. Check the model card and click 'Enable'.)";
        } else if (errorMessage.contains("403") || errorMessage.contains("permission denied")) {
          errorMessage += " (Hint: Check that your credentials have been granted the necessary IAM permissions for Vertex AI.)";
        }
      }
      ApiCallException.ErrorType errorType = ApiCallException.categorizeError(errorMessage);
      throw new ApiCallException(errorMessage != null ? errorMessage : "Unknown error", e, modelName, errorType);
    }
  }

  /**
   * Calls the Anthropic Claude models via Vertex AI's native rawPredict endpoint.
   *
   * @param modelName
   *          The model to use
   * @param textPrompt
   *          The prompt text
   * @return GenerationResult containing the response
   * @throws IOException
   *           If the API call fails
   */
  protected GenerationResult callAnthropicVertexAi(String modelName, String textPrompt) throws ApiCallException {
    try {
      GoogleCredentials credentials = loadCredentials();

      logger.info("Using Anthropic rawPredict API for model: {}", modelName);

      if (credentials == null) {
        // Use ADC for the Anthropic client
        credentials = GoogleCredentials.getApplicationDefault().createScoped("https://www.googleapis.com/auth/cloud-platform");
      }

      String effectiveLocation = resolveEffectiveLocation(modelName);
      String projectId = resolveProjectId(credentials);
      AnthropicVertexClient anthropicClient = new AnthropicVertexClient(projectId, effectiveLocation, credentials);
      String response = anthropicClient.generateContent(modelName, textPrompt);
      return GenerationResult.builder().withText(response).build();
    } catch (Exception e) {
      // Provide helpful error hints for common errors
      String errorMessage = e.getMessage();
      if (errorMessage != null) {
        if (errorMessage.contains("404") || errorMessage.contains("not found")) {
          errorMessage += " (Hint: Model may not be enabled in your GCP project. Check the model card and click 'Enable'.)";
        } else if (errorMessage.contains("403") || errorMessage.contains("permission denied")) {
          errorMessage += " (Hint: Check that your credentials have been granted necessary IAM permissions for Vertex AI.)";
        }
      }
      ApiCallException.ErrorType errorType = ApiCallException.categorizeError(errorMessage);
      throw new ApiCallException(errorMessage != null ? errorMessage : "Unknown error", e, modelName, errorType);
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
  public GenerationResult callChatCompletionsApi(String provider, String modelName, String textPrompt) throws ApiCallException {
    try {
      GoogleCredentials credentials = loadCredentials();

      // Use Chat Completions API for MaaS models
      logger.info("Using Chat Completions API for model: {} with provider: {}", modelName, provider);

      if (credentials == null) {
        // Use ADC for Chat Completions
        credentials = GoogleCredentials.getApplicationDefault().createScoped("https://www.googleapis.com/auth/cloud-platform");
      }

      String projectId = resolveProjectId(credentials);

      // For models with OpenAI flag, prepend the provider prefix
      // For MaaS models, prepend the provider prefix
      // For google-openai models, use the model name as-is (it already contains
      // "google/" prefix)
      String modelWithPrefix = modelName;
      if (!"google-openai".equalsIgnoreCase(provider)) {
        modelWithPrefix = provider + "/" + modelName;
        logger.info("Model name with provider: {}", modelWithPrefix);
      }

      String response;
      try (ChatCompletionsClient chatClient = new ChatCompletionsClient(projectId, authConfig.getLocation(), credentials)) {
        response = chatClient.generateContent(modelWithPrefix, textPrompt);
      }
      return GenerationResult.builder().withText(response).build();
    } catch (Exception e) {
      // Provide helpful error hints for common errors
      String errorMessage = e.getMessage();
      if (errorMessage != null) {
        if (errorMessage.contains("404") || errorMessage.contains("not found")) {
          errorMessage += " (Hint: Model may not be enabled in your GCP project. Check the model card and click 'Enable'.)";
        } else if (errorMessage.contains("403") || errorMessage.contains("permission denied")) {
          errorMessage += " (Hint: Check that your credentials have been granted necessary IAM permissions for Vertex AI.)";
        }
      }
      ApiCallException.ErrorType errorType = ApiCallException.categorizeError(errorMessage);
      throw new ApiCallException(errorMessage != null ? errorMessage : "Unknown error", e, modelName, errorType);
    }
  }
}
