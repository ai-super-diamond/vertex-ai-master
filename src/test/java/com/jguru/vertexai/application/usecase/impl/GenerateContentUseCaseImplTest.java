package com.jguru.vertexai.application.usecase.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jguru.vertexai.domain.ModelResolutionService;
import com.jguru.vertexai.domain.entity.Model;
import com.jguru.vertexai.domain.repository.ModelRepository;
import com.jguru.vertexai.infrastructure.GoogleVertexAIAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GenerateContentUseCaseImplTest {

  @Test
  @DisplayName("Should execute content generation successfully")
  public void shouldExecuteContentGenerationSuccessfully() {
    // Arrange

    ModelRepository modelRepository = mock(ModelRepository.class);

    ModelResolutionService modelResolutionService = mock(ModelResolutionService.class);

    GoogleVertexAIAdapter googleVertexAIAdapter = mock(GoogleVertexAIAdapter.class);

    Model model = new Model("gemini.pro", "gemini-1.5-pro-001");

    when(modelResolutionService.resolveModel("gemini.pro")).thenReturn(model);

    when(googleVertexAIAdapter.callVertexAI("gemini-1.5-pro-001", "Hello, world!", "test-project", "us-central1", "test-api-key"))
        .thenReturn(
            "Mock response for model: gemini-1.5-pro-001, prompt: Hello, world!, project: test-project, location: us-central1, with API key length: 11");

    GenerateContentUseCaseImpl useCase = new GenerateContentUseCaseImpl(modelRepository, modelResolutionService, googleVertexAIAdapter);

    // Act

    String result = useCase.execute("gemini.pro", "Hello, world!");

    // Assert

    assertNotNull(result);

    assertTrue(result.contains("gemini-1.5-pro-001"));

    assertTrue(result.contains("Hello, world!"));
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when model alias is null")
  public void shouldThrowIllegalArgumentExceptionWhenModelAliasIsNull() {
    // Arrange

    ModelRepository modelRepository = mock(ModelRepository.class);

    ModelResolutionService modelResolutionService = mock(ModelResolutionService.class);

    GoogleVertexAIAdapter googleVertexAIAdapter = mock(GoogleVertexAIAdapter.class);
    GenerateContentUseCaseImpl useCase = new GenerateContentUseCaseImpl(modelRepository, modelResolutionService, googleVertexAIAdapter);

    // Act & Assert

    assertThrows(IllegalArgumentException.class, () -> {
      useCase.execute(null, "Hello, world!");
    });
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when model alias is empty")
  public void shouldThrowIllegalArgumentExceptionWhenModelAliasIsEmpty() {
    // Arrange

    ModelRepository modelRepository = mock(ModelRepository.class);

    ModelResolutionService modelResolutionService = mock(ModelResolutionService.class);

    GoogleVertexAIAdapter googleVertexAIAdapter = mock(GoogleVertexAIAdapter.class);
    GenerateContentUseCaseImpl useCase = new GenerateContentUseCaseImpl(modelRepository, modelResolutionService, googleVertexAIAdapter);

    // Act & Assert

    assertThrows(IllegalArgumentException.class, () -> {
      useCase.execute("", "Hello, world!");
    });
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when prompt is null")
  public void shouldThrowIllegalArgumentExceptionWhenPromptIsNull() {
    // Arrange

    ModelRepository modelRepository = mock(ModelRepository.class);

    ModelResolutionService modelResolutionService = mock(ModelResolutionService.class);

    GoogleVertexAIAdapter googleVertexAIAdapter = mock(GoogleVertexAIAdapter.class);
    GenerateContentUseCaseImpl useCase = new GenerateContentUseCaseImpl(modelRepository, modelResolutionService, googleVertexAIAdapter);

    // Act & Assert

    assertThrows(IllegalArgumentException.class, () -> {
      useCase.execute("gemini.pro", null);
    });
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when prompt is empty")
  public void shouldThrowIllegalArgumentExceptionWhenPromptIsEmpty() {
    // Arrange

    ModelRepository modelRepository = mock(ModelRepository.class);

    ModelResolutionService modelResolutionService = mock(ModelResolutionService.class);

    GoogleVertexAIAdapter googleVertexAIAdapter = mock(GoogleVertexAIAdapter.class);
    GenerateContentUseCaseImpl useCase = new GenerateContentUseCaseImpl(modelRepository, modelResolutionService, googleVertexAIAdapter);

    // Act & Assert

    assertThrows(IllegalArgumentException.class, () -> {
      useCase.execute("gemini.pro", "");
    });
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when model is not found")
  public void shouldThrowIllegalArgumentExceptionWhenModelIsNotFound() {
    // Arrange

    ModelRepository modelRepository = mock(ModelRepository.class);

    ModelResolutionService modelResolutionService = mock(ModelResolutionService.class);

    GoogleVertexAIAdapter googleVertexAIAdapter = mock(GoogleVertexAIAdapter.class);

    when(modelResolutionService.resolveModel("invalid.model")).thenReturn(null);

    GenerateContentUseCaseImpl useCase = new GenerateContentUseCaseImpl(modelRepository, modelResolutionService, googleVertexAIAdapter);

    // Act & Assert

    assertThrows(IllegalArgumentException.class, () -> {
      useCase.execute("invalid.model", "Hello, world!");
    });
  }
}
