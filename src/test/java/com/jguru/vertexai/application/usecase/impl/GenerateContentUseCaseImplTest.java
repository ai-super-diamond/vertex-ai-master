package com.jguru.vertexai.application.usecase.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jguru.vertexai.application.dto.GenerateContentRequest;
import com.jguru.vertexai.domain.ModelResolutionService;
import com.jguru.vertexai.domain.dto.AuthenticationConfig;
import com.jguru.vertexai.domain.dto.AuthenticationType;
import com.jguru.vertexai.domain.entity.Model;
import com.jguru.vertexai.domain.exception.ApiCallException;
import com.jguru.vertexai.domain.exception.ModelNotFoundException;
import com.jguru.vertexai.service.ModelClient;
import com.jguru.vertexai.service.ModelClientFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GenerateContentUseCaseImplTest {

  @Test
  @DisplayName("Should execute content generation successfully")
  public void shouldExecuteContentGenerationSuccessfully() throws ApiCallException, ModelNotFoundException {
    // Arrange
    ModelResolutionService modelResolutionService = mock(ModelResolutionService.class);
    ModelClientFactory modelClientFactory = mock(ModelClientFactory.class);
    ModelClient modelClient = mock(ModelClient.class);

    Model model = new Model("gemini.pro", "gemini-1.5-pro-001");
    AuthenticationConfig authConfig = AuthenticationConfig.builder().withType(AuthenticationType.API_KEY).withApiKey("key").build();
    GenerateContentRequest request = new GenerateContentRequest("gemini.pro", "Hello, world!", authConfig);

    when(modelResolutionService.resolveModel("gemini.pro")).thenReturn(model);
    when(modelClientFactory.createClient(authConfig)).thenReturn(modelClient);
    when(modelClient.callVertexAi("gemini-1.5-pro-001", "Hello, world!")).thenReturn("Generated content");

    GenerateContentUseCaseImpl useCase = new GenerateContentUseCaseImpl(modelResolutionService, modelClientFactory);

    // Act
    String result = useCase.execute(request);

    // Assert
    assertNotNull(result);
    assertEquals("Generated content", result);
    verify(modelResolutionService).resolveModel("gemini.pro");
    verify(modelClientFactory).createClient(authConfig);
    verify(modelClient).callVertexAi("gemini-1.5-pro-001", "Hello, world!");
  }

  @Test
  @DisplayName("Should throw ModelNotFoundException when model alias is not found")
  public void shouldThrowModelNotFoundExceptionWhenModelAliasIsNotFound() {
    // Arrange
    ModelResolutionService modelResolutionService = mock(ModelResolutionService.class);
    ModelClientFactory modelClientFactory = mock(ModelClientFactory.class);

    AuthenticationConfig authConfig = AuthenticationConfig.builder().withType(AuthenticationType.API_KEY).withApiKey("key").build();
    GenerateContentRequest request = new GenerateContentRequest("unknown.model", "Hello", authConfig);

    when(modelResolutionService.resolveModel("unknown.model")).thenReturn(null);

    GenerateContentUseCaseImpl useCase = new GenerateContentUseCaseImpl(modelResolutionService, modelClientFactory);

    // Act & Assert
    assertThrows(ModelNotFoundException.class, () -> useCase.execute(request));
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when request is null")
  public void shouldThrowIllegalArgumentExceptionWhenRequestIsNull() {
    // Arrange
    ModelResolutionService modelResolutionService = mock(ModelResolutionService.class);
    ModelClientFactory modelClientFactory = mock(ModelClientFactory.class);
    GenerateContentUseCaseImpl useCase = new GenerateContentUseCaseImpl(modelResolutionService, modelClientFactory);

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
  }
}
