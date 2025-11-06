package com.jguru.vertexai.client;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Client for interacting with Google GenAI and Vertex AI. Supports both API Key and Service Account
 * authentication.
 */
public class VertexAiClient {

  private final String apiKey;
  private final String projectId;
  private final String location;
  private final String serviceAccountKeyPath;
  private final boolean isApiKeyAuth;

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
  }

  /**
   * Calls the GenAI/Vertex AI API to generate content.
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
      // Use Vertex AI - Set environment variables and use client with project
      System.setProperty("GOOGLE_GENAI_USE_VERTEXAI", "true");
      System.setProperty("GOOGLE_CLOUD_PROJECT", projectId);
      System.setProperty("GOOGLE_CLOUD_LOCATION", location);

      if (serviceAccountKeyPath != null) {
        // Validate and load credentials explicitly - DO NOT fall back to ADC
        GoogleCredentials credentials;
        try {
          credentials = GoogleCredentials.fromStream(new FileInputStream(serviceAccountKeyPath))
              .createScoped("https://www.googleapis.com/auth/cloud-platform");
        } catch (IOException e) {
          throw new IOException("Failed to load service account key from: " + serviceAccountKeyPath
              + ". The file must be a valid JSON service account key. ADC fallback is disabled when --sa-key-file is specified.",
              e);
        }

        // Build client with explicit credentials - ADC will NOT be used
        try (Client client = Client.builder().project(projectId).location(location)
            .credentials(credentials).vertexAI(true).build()) {
          GenerateContentResponse response = client.models.generateContent(modelName, text, null);
          return response.text();
        }
      } else {
        // No explicit key path - use ADC
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
