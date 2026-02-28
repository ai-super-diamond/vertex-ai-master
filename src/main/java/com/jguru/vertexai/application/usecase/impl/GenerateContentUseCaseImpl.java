package com.jguru.vertexai.application.usecase.impl;

import com.jguru.vertexai.application.dto.GenerateContentRequest;
import com.jguru.vertexai.application.usecase.GenerateContentUseCase;
import com.jguru.vertexai.domain.ModelResolutionService;
import com.jguru.vertexai.domain.entity.Model;
import com.jguru.vertexai.domain.exception.ApiCallException;
import com.jguru.vertexai.domain.exception.ModelNotFoundException;
import com.jguru.vertexai.service.ModelClient;
import com.jguru.vertexai.service.ModelClientFactory;

/**
 * Implementation of GenerateContentUseCase following Clean Architecture principles.
 *
 * <p>
 * This use case orchestrates the content generation process by: 1. Resolving model aliases to actual model configurations 2. Creating
 * appropriate API clients via factory 3. Delegating API calls to infrastructure layer
 * </p>
 *
 * <p>
 * Note: This class depends only on domain interfaces (ports), never on infrastructure implementations. This maintains the Dependency Rule
 * of Clean Architecture.
 * </p>
 */
public class GenerateContentUseCaseImpl implements GenerateContentUseCase {

  private final ModelResolutionService modelResolutionService;
  private final ModelClientFactory modelClientFactory;

  public GenerateContentUseCaseImpl(ModelResolutionService modelResolutionService, ModelClientFactory modelClientFactory) {
    this.modelResolutionService = modelResolutionService;
    this.modelClientFactory = modelClientFactory;
  }

  @Override
  public String execute(GenerateContentRequest request) throws ModelNotFoundException, ApiCallException {
    // Validate request (already done in GenerateContentRequest constructor, but
    // defensive)
    if (request == null) {
      throw new IllegalArgumentException("Request cannot be null");
    }

    // Resolve model alias to actual model
    Model model = modelResolutionService.resolveModel(request.getModelAlias());
    if (model == null) {
      throw new ModelNotFoundException(request.getModelAlias());
    }

    // Create client with authentication config from request (NO hardcoded values!)
    ModelClient client = modelClientFactory.createClient(request.getAuthenticationConfig());

    // Execute API call and return result
    return client.callVertexAi(model.getFullName(), request.getPrompt());
  }
}
