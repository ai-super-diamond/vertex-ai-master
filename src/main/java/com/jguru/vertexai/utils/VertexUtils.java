package com.jguru.vertexai.utils;

import com.jguru.vertexai.client.VertexAiClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Utility class for generating content using Google GenAI and Vertex AI.
 */
public class VertexUtils {

  private static Properties modelProperties = null;

  /**
   * Loads model properties from external file or embedded resource.
   */
  private static Properties getModelProperties() {
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
      try (InputStream is = VertexUtils.class.getResourceAsStream("/models.properties")) {
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
   *
   * @param modelName
   *          The model name or alias
   * @return The resolved model name
   */
  private static String resolveModelName(String modelName) {
    Properties props = getModelProperties();
    String resolved = props.getProperty(modelName);
    if (resolved != null) {
      System.err.println("[INFO] Resolved model alias '" + modelName + "' -> '" + resolved + "'");
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
}
