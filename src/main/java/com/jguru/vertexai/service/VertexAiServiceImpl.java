package com.jguru.vertexai.service;

import com.jguru.vertexai.client.VertexAiClient;
import com.jguru.vertexai.service.dto.AuthenticationConfig;
import com.jguru.vertexai.service.dto.ErrorType;
import com.jguru.vertexai.service.dto.GenerationRequest;
import com.jguru.vertexai.service.dto.GenerationResult;
import com.jguru.vertexai.service.dto.RegionCheckRequest;
import com.jguru.vertexai.service.dto.RegionCheckResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

  // US Regions (source: https://cloud.google.com/about/locations - Nov 2025)
  public static final List<String> US_REGIONS = Arrays.asList("us-central1", "us-east1", "us-east4",
      "us-east5", "us-south1", "us-west1", "us-west2", "us-west3", "us-west4");

  // Europe Regions (source: https://cloud.google.com/about/locations - Nov 2025)
  public static final List<String> EUROPE_REGIONS = Arrays.asList("europe-central2",
      "europe-north1", "europe-southwest1", "europe-west1", "europe-west2", "europe-west3",
      "europe-west4", "europe-west6", "europe-west8", "europe-west9", "europe-west12");

  // Asia Pacific Regions (source: https://cloud.google.com/about/locations - Nov 2025)
  public static final List<String> ASIA_REGIONS = Arrays.asList("asia-east1", "asia-east2",
      "asia-northeast1", "asia-northeast2", "asia-northeast3", "asia-south1", "asia-south2",
      "asia-southeast1", "asia-southeast2", "australia-southeast1", "australia-southeast2");

  // Middle East Regions (source: https://cloud.google.com/about/locations - Nov 2025)
  public static final List<String> MIDDLE_EAST_REGIONS = Arrays.asList("me-central1", "me-central2",
      "me-west1");

  // Africa Regions (source: https://cloud.google.com/about/locations - Nov 2025)
  public static final List<String> AFRICA_REGIONS = Arrays.asList("africa-south1");

  // North America (Canada) Regions (source: https://cloud.google.com/about/locations - Nov 2025)
  public static final List<String> CANADA_REGIONS = Arrays.asList("northamerica-northeast1",
      "northamerica-northeast2");

  // South America Regions (source: https://cloud.google.com/about/locations - Nov 2025)
  public static final List<String> SOUTH_AMERICA_REGIONS = Arrays.asList("southamerica-east1",
      "southamerica-west1");

  private static Properties modelProperties = null;

  /**
   * Loads model properties from external file or embedded resource.
   */
  private Properties getModelProperties() {
    if (modelProperties == null) {
      modelProperties = new Properties();

      // Try external file first (from -Dmodels.config system property)
      String externalConfig = System.getProperty("models.config");
      if (externalConfig != null) {
        Path configPath = Paths.get(externalConfig);
        if (Files.exists(configPath)) {
          try (InputStream is = new FileInputStream(configPath.toFile())) {
            modelProperties.load(is);
            logger.info("Loaded models from: {}", configPath);
            return modelProperties;
          } catch (IOException e) {
            logger.warn("Failed to load external models.properties: {}", e.getMessage());
          }
        }
      }

      // Fall back to embedded resource
      try (InputStream is = getClass().getResourceAsStream("/models.properties")) {
        if (is != null) {
          modelProperties.load(is);
          logger.info("Loaded embedded models.properties");
        }
      } catch (IOException e) {
        logger.warn("Failed to load embedded models.properties: {}", e.getMessage());
      }
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
    String prompt = (request.getTestPrompt() != null && !request.getTestPrompt().isEmpty())
        ? request.getTestPrompt()
        : "Hello";
    String resolvedModel = resolveModelName(request.getModelName());
    String saKeyFile = request.getAuthenticationConfig().getSaKeyFile();
    String projectId = request.getAuthenticationConfig().getProjectId();

    for (String region : request.getRegions()) {
      try {
        AuthenticationConfig regionAuthConfig = AuthenticationConfig.builder()
            .withType(request.getAuthenticationConfig().getType()).withSaKeyFile(saKeyFile)
            .withProjectId(projectId).withLocation(region).build();
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
        results.put(region, errorType.formatMessage(errorMsg));
      } catch (Exception e) {
        String errorMsg = e.getMessage();
        ErrorType errorType = ErrorType.fromMessage(errorMsg);
        results.put(region, errorType.formatMessage(errorMsg));
      }
    }

    return new RegionCheckResult(results);
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
}
