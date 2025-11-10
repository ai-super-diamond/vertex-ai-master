package com.jguru.vertexai.utils;

import com.jguru.vertexai.client.VertexAiClient;
import com.jguru.vertexai.service.RegionCatalog;
import com.jguru.vertexai.service.RegionCatalog.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Utility class for generating content using Google GenAI and Vertex AI.
 */
public class VertexUtils {

  private static final Logger logger = LoggerFactory.getLogger(VertexUtils.class);

  // Region lists exposed for legacy callers while sourcing data from the shared catalogue.
  public static final List<String> US_REGIONS = List.copyOf(RegionCatalog.getRegions(Cluster.US));
  public static final List<String> EUROPE_REGIONS = List
      .copyOf(RegionCatalog.getRegions(Cluster.EUROPE));
  public static final List<String> ASIA_REGIONS = List
      .copyOf(RegionCatalog.getRegions(Cluster.ASIA));
  public static final List<String> MIDDLE_EAST_REGIONS = List
      .copyOf(RegionCatalog.getRegions(Cluster.MIDDLE_EAST));
  public static final List<String> AFRICA_REGIONS = List
      .copyOf(RegionCatalog.getRegions(Cluster.AFRICA));
  public static final List<String> CANADA_REGIONS = List
      .copyOf(RegionCatalog.getRegions(Cluster.CANADA));
  public static final List<String> SOUTH_AMERICA_REGIONS = List
      .copyOf(RegionCatalog.getRegions(Cluster.SOUTH_AMERICA));

  private static Properties modelProperties = null;

  /**
   * Loads model properties from external file or embedded resource.
   */
  private static Properties getModelProperties() {
    if (modelProperties == null) {
      modelProperties = PropertiesLoader.load(logger, "models.config", "models.properties");
    }
    return modelProperties;
  }

  /**
   * Resolves a model name, checking if it's an alias in models.properties.
   *
   * @param modelName
   *          The model name or alias
   * @return The resolved model name
   */
  private static String resolveModelName(String modelName) {
    Properties props = getModelProperties();
    String resolved = props.getProperty(modelName);
    if (resolved != null) {
      logger.info("Resolved model alias '{}' -> '{}'", modelName, resolved);
      return resolved;
    }
    return modelName;
  }

  /**
   * Generates content using API Key authentication.
   *
   * @param apiKey
   *          The API key for authentication
   * @param modelName
   *          The model name to use
   * @param text
   *          The prompt text
   * @return Generated response text
   * @throws IOException
   *           If the API call fails
   */
  public static String generateContent(String apiKey, String modelName, String text)
      throws IOException {
    String resolvedModel = resolveModelName(modelName);
    VertexAiClient client = new VertexAiClient(apiKey);
    return client.callVertexAi(resolvedModel, text);
  }

  /**
   * Generates content using Service Account authentication (Application Default Credentials).
   *
   * @param projectId
   *          Google Cloud project ID
   * @param location
   *          Google Cloud location (e.g., us-central1)
   * @param modelName
   *          The model name to use
   * @param text
   *          The prompt text
   * @return Generated response text
   * @throws IOException
   *           If the API call fails
   */
  public static String generateContent(String projectId, String location, String modelName,
      String text) throws IOException {
    String resolvedModel = resolveModelName(modelName);
    VertexAiClient client = new VertexAiClient(projectId, location);
    return client.callVertexAi(resolvedModel, text);
  }

  /**
   * Generates content using Service Account JSON key file.
   *
   * @param saKeyPath
   *          Path to Service Account JSON key file
   * @param projectId
   *          Google Cloud project ID
   * @param location
   *          Google Cloud location (e.g., us-central1)
   * @param modelName
   *          The model name to use
   * @param text
   *          The prompt text
   * @return Generated response text
   * @throws IOException
   *           If the API call fails
   */
  public static String generateContent(String saKeyPath, String projectId, String location,
      String modelName, String text) throws IOException {
    String resolvedModel = resolveModelName(modelName);
    VertexAiClient client = new VertexAiClient(saKeyPath, projectId, location);
    return client.callVertexAi(resolvedModel, text);
  }

  /**
   * Checks connectivity and availability of a model across multiple regions.
   *
   * @param saKeyPath
   *          Path to Service Account JSON key file
   * @param projectId
   *          Google Cloud project ID
   * @param modelName
   *          The model name to test
   * @param regions
   *          List of regions to test
   * @param testPrompt
   *          Simple test prompt (default: "Hello")
   * @return Map of region -> result ("SUCCESS" or error message)
   */
  public static Map<String, String> checkConnectivityAvailability(String saKeyPath,
      String projectId, String modelName, List<String> regions, String testPrompt) {
    Map<String, String> results = new HashMap<>();
    String prompt = (testPrompt != null && !testPrompt.isEmpty()) ? testPrompt : "Hello";
    String resolvedModel = resolveModelName(modelName);

    for (String region : regions) {
      try {
        VertexAiClient client = new VertexAiClient(saKeyPath, projectId, region);
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

    return results;
  }

  /**
   * Checks connectivity and availability of a model across multiple regions with default prompt.
   *
   * @param saKeyPath
   *          Path to Service Account JSON key file
   * @param projectId
   *          Google Cloud project ID
   * @param modelName
   *          The model name to test
   * @param regions
   *          List of regions to test
   * @return Map of region -> result ("SUCCESS" or error message)
   */
  public static Map<String, String> checkConnectivityAvailability(String saKeyPath,
      String projectId, String modelName, List<String> regions) {
    return checkConnectivityAvailability(saKeyPath, projectId, modelName, regions, "Hello");
  }
}
