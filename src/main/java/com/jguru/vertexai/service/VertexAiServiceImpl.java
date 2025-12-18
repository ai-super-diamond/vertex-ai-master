package com.jguru.vertexai.service;

import com.jguru.vertexai.client.VertexAiClient;
import com.jguru.vertexai.service.dto.AuthenticationConfig;
import com.jguru.vertexai.service.dto.ErrorType;
import com.jguru.vertexai.service.dto.GenerationRequest;
import com.jguru.vertexai.service.dto.GenerationResult;
import com.jguru.vertexai.service.dto.RegionCheckRequest;
import com.jguru.vertexai.service.dto.RegionCheckResult;
import com.jguru.vertexai.utils.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Implementation of VertexAiService containing all business logic.
 */
public class VertexAiServiceImpl implements VertexAiService {

  private static final Logger logger = LoggerFactory.getLogger(VertexAiServiceImpl.class);

  private final RegionProvider regionProvider;

  public VertexAiServiceImpl() {
    this.regionProvider = new RegionProviderImpl();
  }

  public VertexAiServiceImpl(RegionProvider regionProvider) {
    this.regionProvider = regionProvider;
  }

  private static Properties modelProperties = null;

  /**
   * Loads model properties from external file or embedded resource.
   */
  private Properties getModelProperties() {
    if (modelProperties == null) {
      modelProperties = PropertiesLoader.load(logger, "models.config", "models.properties");
    }
    return modelProperties;
  }

  /**
   * Resolves a model name, checking if it's an alias in models.properties.
   */
  @Override
  public String resolveModelName(String modelName) {
    Properties props = getModelProperties();
    String resolved = props.getProperty(modelName);
    if (resolved != null) {
      logger.info("Resolved model alias '{}' -> '{}'", modelName, resolved);
      return resolved;
    }
    return modelName;
  }

  /**
   * Generates content based on the provided request.
   */
  @Override
  public GenerationResult generateContent(GenerationRequest request) throws Exception {
    String resolvedModel = resolveModelName(request.getModelName());

    try {
      VertexAiClient client = new VertexAiClient(request.getAuthenticationConfig());
      String response = client.callVertexAi(resolvedModel, request.getText());
      return GenerationResult.success(response);
    } catch (Exception e) {
      return GenerationResult.failure("Error generating content: " + e.getMessage());
    }
  }

  /**
   * Checks model availability across multiple regions.
   */
  @Override
  public RegionCheckResult checkRegionAvailability(RegionCheckRequest request) throws Exception {
    Map<String, String> results = new HashMap<>();
    String prompt = (request.getTestPrompt() != null && !request.getTestPrompt().isEmpty()) ? request.getTestPrompt() : "Hello";
    String resolvedModel = resolveModelName(request.getModelName());
    String saKeyFile = request.getAuthenticationConfig().getSaKeyFile();
    String projectId = request.getAuthenticationConfig().getProjectId();
    boolean debugMode = request.isDebug();

    for (String region : request.getRegions()) {
      try {
        AuthenticationConfig regionAuthConfig = buildRegionAuthenticationConfig(request.getAuthenticationConfig(), projectId, saKeyFile,
            region);
        VertexAiClient client = new VertexAiClient(regionAuthConfig);
        String response = client.callVertexAi(resolvedModel, prompt);
        if (response != null && !response.isEmpty()) {
          results.put(region, "SUCCESS");
        } else {
          results.put(region, "ERROR: Empty response");
        }
      } catch (IOException e) {
        String errorMsg = e.getMessage();
        // Extract meaningful error info using ErrorType enum
        ErrorType errorType = ErrorType.fromMessage(errorMsg);
        String formattedError = errorType.formatMessage(errorMsg);
        if (debugMode) {
          formattedError = buildDebugError(formattedError, e);
        }
        results.put(region, formattedError);
      } catch (Exception e) {
        String errorMsg = e.getMessage();
        ErrorType errorType = ErrorType.fromMessage(errorMsg);
        String formattedError = errorType.formatMessage(errorMsg);
        if (debugMode) {
          formattedError = buildDebugError(formattedError, e);
        }
        results.put(region, formattedError);
      }
    }

    return new RegionCheckResult(results);
  }

  private AuthenticationConfig buildRegionAuthenticationConfig(AuthenticationConfig baseConfig, String projectId, String saKeyFile,
      String region) {
    AuthenticationConfig.Builder builder = AuthenticationConfig.builder().withType(baseConfig.getType());

    switch (baseConfig.getType()) {
      case API_KEY :
        builder.withApiKey(baseConfig.getApiKey());
        break;
      case SERVICE_ACCOUNT_ADC :
        builder.withProjectId(projectId).withLocation(region);
        break;
      case SERVICE_ACCOUNT_EXPLICIT_KEY :
        builder.withSaKeyFile(saKeyFile).withProjectId(projectId).withLocation(region);
        break;
      default :
        throw new IllegalStateException("Unsupported authentication type: " + baseConfig.getType());
    }
    return builder.build();
  }

  /**
   * Gets regions for a specified cluster.
   */
  public List<String> getRegionsForCluster(String clusterName) {
    return regionProvider.getRegionsForCluster(clusterName);
  }

  /**
   * Gets all regions across all clusters.
   */
  public List<String> getAllRegions() {
    return regionProvider.getAllRegions();
  }

  private String buildDebugError(String base, Exception e) {
    String exceptionPart = String.format("%s | Exception: %s", base, e.getClass().getSimpleName());

    // Summarize cause chain classes, e.g., ApiException -> IOException -> SocketTimeoutException
    java.util.List<Throwable> chain = ExceptionUtils.getThrowableList(e);
    String chainSummary = chain.stream().map(t -> t.getClass().getSimpleName()).distinct()
        .collect(java.util.stream.Collectors.joining(" -> "));

    String result = String.format("%s | CauseChain: %s", exceptionPart, chainSummary);

    // Root cause details (class, message, and first stack location)
    Throwable root = ExceptionUtils.getRootCause(e);
    if (root != null) {
      StackTraceElement[] st = root.getStackTrace();
      String at = (st != null && st.length > 0) ? String.format("%s:%d", st[0].getClassName(), st[0].getLineNumber()) : "unknown";
      result = String.format("%s | RootCause: %s: %s | At: %s", result, root.getClass().getSimpleName(), ExceptionUtils.getMessage(root),
          at);
    }

    return result;
  }
}
