package com.jguru.vertexai.application.usecase.impl;

import com.jguru.vertexai.application.usecase.GenerateContentUseCase;
import com.jguru.vertexai.domain.ModelResolutionService;
import com.jguru.vertexai.domain.entity.Model;
import com.jguru.vertexai.domain.repository.ModelRepository;
import com.jguru.vertexai.infrastructure.GoogleVertexAIAdapter;

public class GenerateContentUseCaseImpl implements GenerateContentUseCase {

  private final ModelRepository modelRepository;

  private final ModelResolutionService modelResolutionService;

  private final GoogleVertexAIAdapter googleVertexAIAdapter;

  public GenerateContentUseCaseImpl(ModelRepository modelRepository, ModelResolutionService modelResolutionService,
      GoogleVertexAIAdapter googleVertexAIAdapter) {
    this.modelRepository = modelRepository;

    this.modelResolutionService = modelResolutionService;

    this.googleVertexAIAdapter = googleVertexAIAdapter;
  }

  @Override
  public String execute(String modelAlias, String prompt) {
    if (modelAlias == null || modelAlias.trim().isEmpty()) {
      throw new IllegalArgumentException("Model alias cannot be null or empty");
    }

    if (prompt == null || prompt.trim().isEmpty()) {
      throw new IllegalArgumentException("Prompt cannot be null or empty");
    }

    // Resolve the model alias to get the full model details

    Model model = modelResolutionService.resolveModel(modelAlias);

    if (model == null) {
      throw new IllegalArgumentException("Model with alias '" + modelAlias + "' not found");
    }

    // Call the infrastructure layer to actually interact with the Vertex AI API
    return googleVertexAIAdapter.callVertexAI(model.getFullName(), prompt, "test-project", "us-central1", "test-api-key");
  }
}
