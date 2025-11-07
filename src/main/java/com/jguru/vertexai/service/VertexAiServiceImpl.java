package com.jguru.vertexai.service;

import com.jguru.vertexai.client.VertexAiClient;
import com.jguru.vertexai.service.dto.AuthenticationConfig;
import com.jguru.vertexai.service.dto.AuthenticationType;
import com.jguru.vertexai.service.dto.GenerationRequest;
import com.jguru.vertexai.service.dto.GenerationResult;
import com.jguru.vertexai.service.dto.RegionCheckRequest;
import com.jguru.vertexai.service.dto.RegionCheckResult;

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
            System.err.println("[INFO] Loaded models from: " + configPath);
            return modelProperties;
          } catch (IOException e) {
            System.err
                .println("[WARN] Failed to load external models.properties: " + e.getMessage());
          }
        }
      }

      // Fall back to embedded resource
      try (InputStream is = getClass().getResourceAsStream("/models.properties")) {
        if (is != null) {
          modelProperties.load(is);
          System.err.println("[INFO] Loaded embedded models.properties");
        }
      } catch (IOException e) {
        System.err.println("[WARN] Failed to load embedded models.properties: " + e.getMessage());
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
      System.err.println("[INFO] Resolved model alias '" + modelName + "' -> '" + resolved + "'");
      return resolved;
    }
    return modelName;
  }

  /**
   * Generates content based on the provided request.
   */
  @Override
  public GenerationResult generateContent(GenerationRequest request) throws Exception {
    AuthenticationConfig authConfig = request.getAuthenticationConfig();
    String resolvedModel = resolveModelName(request.getModelName());
    String text = request.getText();

    try {
      String response;
      if (authConfig.getType() == AuthenticationType.API_KEY) {
        response = generateContentApiKey(authConfig.getApiKey(), resolvedModel, text);
      } else if (authConfig.getType() == AuthenticationType.SERVICE_ACCOUNT_ADC) {
        response = generateContentServiceAccountAdc(authConfig.getProjectId(),
            authConfig.getLocation(), resolvedModel, text);
      } else if (authConfig.getType() == AuthenticationType.SERVICE_ACCOUNT_EXPLICIT_KEY) {
        response = generateContentServiceAccountKey(authConfig.getSaKeyFile(),
            authConfig.getProjectId(), authConfig.getLocation(), resolvedModel, text);
      } else {
        throw new IllegalArgumentException(
            "Unsupported authentication type: " + authConfig.getType());
      }

      return GenerationResult.success(response);
    } catch (Exception e) {
      return GenerationResult.failure("Error generating content: " + e.getMessage());
    }
  }

  private String generateContentApiKey(String apiKey, String modelName, String text)
      throws IOException {
    VertexAiClient client = new VertexAiClient(apiKey);
    return client.callVertexAi(modelName, text);
  }

  private String generateContentServiceAccountAdc(String projectId, String location,
      String modelName, String text) throws IOException {
    VertexAiClient client = new VertexAiClient(projectId, location);
    return client.callVertexAi(modelName, text);
  }

  private String generateContentServiceAccountKey(String saKeyPath, String projectId,
      String location, String modelName, String text) throws IOException {
    VertexAiClient client = new VertexAiClient(saKeyPath, projectId, location);
    return client.callVertexAi(modelName, text);
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
        VertexAiClient client = new VertexAiClient(saKeyFile, projectId, region);
        String response = client.callVertexAi(resolvedModel, prompt);
        if (response != null && !response.isEmpty()) {
          results.put(region, "SUCCESS");
        } else {
          results.put(region, "ERROR: Empty response");
        }
      } catch (IOException e) {
        String errorMsg = e.getMessage();
        // Extract meaningful error info
        if (errorMsg.contains("404")) {
          results.put(region, "404 Not Found");
        } else if (errorMsg.contains("403")) {
          results.put(region, "403 Permission Denied");
        } else if (errorMsg.contains("400")) {
          results.put(region, "400 Bad Request");
        } else if (errorMsg.contains("500")) {
          results.put(region, "500 Internal Error");
        } else {
          // Truncate long error messages
          String shortError = errorMsg.length() > 100
              ? errorMsg.substring(0, 100) + "..."
              : errorMsg;
          results.put(region, "ERROR: " + shortError);
        }
      } catch (Exception e) {
        results.put(region, "ERROR: " + e.getClass().getSimpleName() + ": " + e.getMessage());
      }
    }

    return new RegionCheckResult(results);
  }

  /**
   * Gets regions for a specified cluster.
   */
  public List<String> getRegionsForCluster(String clusterName) {
    String upperCluster = clusterName.toUpperCase();
    switch (upperCluster) {
      case "US" :
      case "USA" :
        return US_REGIONS;
      case "EU" :
      case "EUROPE" :
        return EUROPE_REGIONS;
      case "ASIA" :
      case "APAC" :
      case "ASIA_PACIFIC" :
        return ASIA_REGIONS;
      case "MIDDLE_EAST" :
      case "ME" :
        return MIDDLE_EAST_REGIONS;
      case "AFRICA" :
        return AFRICA_REGIONS;
      case "CANADA" :
      case "CA" :
        return CANADA_REGIONS;
      case "SOUTH_AMERICA" :
      case "SA" :
        return SOUTH_AMERICA_REGIONS;
      default :
        return null;
    }
  }
}
