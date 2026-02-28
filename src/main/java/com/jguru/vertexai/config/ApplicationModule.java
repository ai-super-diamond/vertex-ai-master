package com.jguru.vertexai.config;

import com.jguru.vertexai.adapter.GenerateContentPresenter;
import com.jguru.vertexai.adapter.ModelController;
import com.jguru.vertexai.adapter.impl.ModelControllerImpl;
import com.jguru.vertexai.application.usecase.GenerateContentUseCase;
import com.jguru.vertexai.application.usecase.impl.GenerateContentUseCaseImpl;
import com.jguru.vertexai.domain.ModelResolutionService;
import com.jguru.vertexai.service.impl.ModelResolutionServiceImpl;
import com.jguru.vertexai.service.ModelClientFactory;
import com.jguru.vertexai.domain.repository.ModelRepository;
import com.jguru.vertexai.infrastructure.client.VertexAiClientFactory;
import com.jguru.vertexai.infrastructure.impl.ModelRepositoryImpl;

/**
 * Application module for manual dependency injection.
 *
 * <p>
 * This module wires together all layers following Clean Architecture principles. Dependencies flow inward: Infrastructure → Application →
 * Domain
 * </p>
 */
public class ApplicationModule {

  private ModelRepository modelRepository;
  private ModelClientFactory modelClientFactory;
  private String apiKey = System.getenv("GEMINI_API_KEY");

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public ModelRepository provideModelRepository() {
    if (modelRepository == null) {
      modelRepository = new ModelRepositoryImpl();
    }
    return modelRepository;
  }

  public ModelClientFactory provideModelClientFactory() {
    if (modelClientFactory == null) {
      // Infrastructure provides implementation of domain port (note: polymorphism)
      modelClientFactory = (ModelClientFactory) new VertexAiClientFactory();
    }
    return modelClientFactory;
  }

  public void setModelClientFactory(ModelClientFactory modelClientFactory) {
    this.modelClientFactory = modelClientFactory;
  }

  public ModelResolutionService provideModelResolutionService(ModelRepository modelRepository) {
    return new ModelResolutionServiceImpl(modelRepository);
  }

  public GenerateContentUseCase provideGenerateContentUseCase(ModelRepository modelRepository,
      ModelResolutionService modelResolutionService) {
    ModelClientFactory factory = provideModelClientFactory();
    return new GenerateContentUseCaseImpl(modelResolutionService, factory);
  }

  public ModelController provideModelController(GenerateContentUseCase generateContentUseCase) {
    GenerateContentPresenter presenter = new GenerateContentPresenter();
    return new ModelControllerImpl(generateContentUseCase, presenter, this.apiKey);
  }
}
