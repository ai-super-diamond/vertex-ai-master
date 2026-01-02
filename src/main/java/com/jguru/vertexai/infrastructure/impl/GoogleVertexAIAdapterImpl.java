package com.jguru.vertexai.infrastructure.impl;

import com.jguru.vertexai.infrastructure.GoogleVertexAIAdapter;

public class GoogleVertexAIAdapterImpl implements GoogleVertexAIAdapter {

  @Override
  public String callVertexAI(String modelName, String prompt, String projectId, String location, String apiKey) {
    // Validate inputs
    if (modelName == null || modelName.trim().isEmpty()) {
      throw new IllegalArgumentException("Model name cannot be null or empty");
    }
    if (prompt == null || prompt.trim().isEmpty()) {
      throw new IllegalArgumentException("Prompt cannot be null or empty");
    }
    if (projectId == null || projectId.trim().isEmpty()) {
      throw new IllegalArgumentException("Project ID cannot be null or empty");
    }
    if (location == null || location.trim().isEmpty()) {
      throw new IllegalArgumentException("Location cannot be null or empty");
    }

    // In a real implementation, this would call the actual Google Vertex AI API
    // For now, return a mock response that shows the parameters were received
    return "Mock response for model: " + modelName + ", prompt: " + prompt + ", project: " + projectId + ", location: " + location
        + ", with API key length: " + (apiKey != null ? apiKey.length() : 0);
  }
}
