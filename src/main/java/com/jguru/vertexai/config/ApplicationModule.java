package com.jguru.vertexai.config;

import com.jguru.vertexai.adapter.GenerateContentPresenter;
import com.jguru.vertexai.adapter.ModelController;
import com.jguru.vertexai.adapter.impl.ModelControllerImpl;
import com.jguru.vertexai.application.usecase.GenerateContentUseCase;
import com.jguru.vertexai.application.usecase.impl.GenerateContentUseCaseImpl;
import com.jguru.vertexai.domain.ModelResolutionService;
import com.jguru.vertexai.domain.impl.ModelResolutionServiceImpl;
import com.jguru.vertexai.domain.repository.ModelRepository;
import com.jguru.vertexai.infrastructure.GoogleVertexAIAdapter;
import com.jguru.vertexai.infrastructure.impl.GoogleVertexAIAdapterImpl;
import com.jguru.vertexai.infrastructure.impl.ModelRepositoryImpl;

public class ApplicationModule {

  private ModelRepository modelRepository;

  private GoogleVertexAIAdapter googleVertexAIAdapter;

  public ModelRepository provideModelRepository() {
    if (modelRepository == null) {
      modelRepository = new ModelRepositoryImpl();
    }

    return modelRepository;
  }

  public GoogleVertexAIAdapter provideGoogleVertexAIAdapter() {
    if (googleVertexAIAdapter == null) {
      googleVertexAIAdapter = new GoogleVertexAIAdapterImpl();
    }
    return googleVertexAIAdapter;
  }

  public ModelResolutionService provideModelResolutionService(ModelRepository modelRepository) {
    return new ModelResolutionServiceImpl(modelRepository);
  }

  public GenerateContentUseCase provideGenerateContentUseCase(ModelRepository modelRepository,
      ModelResolutionService modelResolutionService) {
    return new GenerateContentUseCaseImpl(modelRepository, modelResolutionService, provideGoogleVertexAIAdapter());
  }

  public ModelController provideModelController(GenerateContentUseCase generateContentUseCase) {
    GenerateContentPresenter presenter = new GenerateContentPresenter();

    return new ModelControllerImpl(generateContentUseCase, presenter);
  }
}
