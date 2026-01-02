package com.jguru.vertexai.infrastructure;

public interface GoogleVertexAIAdapter {
  String callVertexAI(String modelName, String prompt, String projectId, String location, String apiKey);
}
