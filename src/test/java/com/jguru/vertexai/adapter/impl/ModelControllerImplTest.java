package com.jguru.vertexai.adapter.impl;

import com.jguru.vertexai.adapter.GenerateContentPresenter;
import com.jguru.vertexai.adapter.ModelController;
import com.jguru.vertexai.application.dto.GenerateContentRequest;
import com.jguru.vertexai.application.usecase.GenerateContentUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ModelControllerImplTest {

  @Test
  @DisplayName("Should generate content successfully and format response")
  public void shouldGenerateContentSuccessfullyAndFormatResponse() throws Exception {
    // Arrange
    GenerateContentUseCase useCase = mock(GenerateContentUseCase.class);
    GenerateContentPresenter presenter = mock(GenerateContentPresenter.class);

    when(useCase.execute(any(GenerateContentRequest.class))).thenReturn("Generated content");
    when(presenter.presentSuccess("Generated content")).thenReturn("SUCCESS: Generated content");

    ModelController controller = new ModelControllerImpl(useCase, presenter, "test-api-key");

    // Act
    String result = controller.generateContent("gemini.pro", "Hello, world!");

    // Assert
    assertEquals("SUCCESS: Generated content", result);
    verify(useCase).execute(any(GenerateContentRequest.class));
    verify(presenter).presentSuccess("Generated content");
  }

  @Test
  @DisplayName("Should handle exception from use case and present error")
  public void shouldHandleExceptionFromUseCaseAndPresentError() throws Exception {
    // Arrange
    GenerateContentUseCase useCase = mock(GenerateContentUseCase.class);
    GenerateContentPresenter presenter = mock(GenerateContentPresenter.class);

    when(useCase.execute(any(GenerateContentRequest.class))).thenThrow(new IllegalArgumentException("Invalid model alias"));
    when(presenter.presentError("Invalid model alias")).thenReturn("ERROR: Invalid model alias");

    ModelController controller = new ModelControllerImpl(useCase, presenter, "test-api-key");

    // Act
    String result = controller.generateContent("gemini.pro", "Hello, world!");

    // Assert
    assertEquals("ERROR: Invalid model alias", result);
    verify(useCase).execute(any(GenerateContentRequest.class));
    verify(presenter).presentError("Invalid model alias");
  }

  @Test
  @DisplayName("Should handle null model alias gracefully")
  public void shouldHandleNullModelAliasGracefully() throws Exception {
    // Arrange
    GenerateContentUseCase useCase = mock(GenerateContentUseCase.class);
    GenerateContentPresenter presenter = mock(GenerateContentPresenter.class);

    when(presenter.presentError("Model alias cannot be null or empty")).thenReturn("ERROR: Model alias cannot be null or empty");

    ModelController controller = new ModelControllerImpl(useCase, presenter, "test-api-key");

    // Act
    String result = controller.generateContent(null, "Hello, world!");

    // Assert
    assertEquals("ERROR: Model alias cannot be null or empty", result);
    verify(useCase, never()).execute(any(GenerateContentRequest.class));
    verify(presenter).presentError("Model alias cannot be null or empty");
  }

  @Test
  @DisplayName("Should handle null prompt gracefully")
  public void shouldHandleNullPromptGracefully() throws Exception {
    // Arrange
    GenerateContentUseCase useCase = mock(GenerateContentUseCase.class);
    GenerateContentPresenter presenter = mock(GenerateContentPresenter.class);

    when(presenter.presentError("Prompt cannot be null or empty")).thenReturn("ERROR: Prompt cannot be null or empty");

    ModelController controller = new ModelControllerImpl(useCase, presenter, "test-api-key");

    // Act
    String result = controller.generateContent("gemini.pro", null);

    // Assert
    assertEquals("ERROR: Prompt cannot be null or empty", result);
    verify(useCase, never()).execute(any(GenerateContentRequest.class));
    verify(presenter).presentError("Prompt cannot be null or empty");
  }

  @Test
  @DisplayName("Should handle empty model alias gracefully")
  public void shouldHandleEmptyModelAliasGracefully() throws Exception {
    // Arrange
    GenerateContentUseCase useCase = mock(GenerateContentUseCase.class);
    GenerateContentPresenter presenter = mock(GenerateContentPresenter.class);

    when(presenter.presentError("Model alias cannot be null or empty")).thenReturn("ERROR: Model alias cannot be null or empty");

    ModelController controller = new ModelControllerImpl(useCase, presenter, "test-api-key");

    // Act
    String result = controller.generateContent("", "Hello, world!");

    // Assert
    assertEquals("ERROR: Model alias cannot be null or empty", result);
    verify(useCase, never()).execute(any(GenerateContentRequest.class));
    verify(presenter).presentError("Model alias cannot be null or empty");
  }

  @Test
  @DisplayName("Should handle empty prompt gracefully")
  public void shouldHandleEmptyPromptGracefully() throws Exception {
    // Arrange
    GenerateContentUseCase useCase = mock(GenerateContentUseCase.class);
    GenerateContentPresenter presenter = mock(GenerateContentPresenter.class);

    when(presenter.presentError("Prompt cannot be null or empty")).thenReturn("ERROR: Prompt cannot be null or empty");

    ModelController controller = new ModelControllerImpl(useCase, presenter, "test-api-key");

    // Act
    String result = controller.generateContent("gemini.pro", "");

    // Assert
    assertEquals("ERROR: Prompt cannot be null or empty", result);
    verify(useCase, never()).execute(any(GenerateContentRequest.class));
    verify(presenter).presentError("Prompt cannot be null or empty");
  }
}
