package com.jguru.vertexai.unit;

import com.jguru.vertexai.domain.ModelResolutionService;
import com.jguru.vertexai.domain.entity.Model;
import com.jguru.vertexai.service.impl.ModelResolutionServiceImpl;
import com.jguru.vertexai.domain.repository.ModelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DomainServiceTest {

  @Test
  @DisplayName("Should resolve model when alias exists")
  public void shouldResolveModelWhenAliasExists() {
    // Arrange
    ModelRepository modelRepository = mock(ModelRepository.class);
    Model expectedModel = new Model("gemini.pro", "gemini-1.5-pro-001");
    when(modelRepository.findByAlias("gemini.pro")).thenReturn(expectedModel);

    ModelResolutionService service = new ModelResolutionServiceImpl(modelRepository);

    // Act
    Model result = service.resolveModel("gemini.pro");

    // Assert
    assertNotNull(result);
    assertEquals("gemini.pro", result.getAlias());
    assertEquals("gemini-1.5-pro-001", result.getFullName());
    verify(modelRepository).findByAlias("gemini.pro");
  }

  @Test
  @DisplayName("Should return null when model alias does not exist")
  public void shouldReturnNullWhenModelAliasDoesNotExist() {
    // Arrange
    ModelRepository modelRepository = mock(ModelRepository.class);
    when(modelRepository.findByAlias("nonexistent.model")).thenReturn(null);

    ModelResolutionService service = new ModelResolutionServiceImpl(modelRepository);

    // Act
    Model result = service.resolveModel("nonexistent.model");

    // Assert
    assertNull(result);
    verify(modelRepository).findByAlias("nonexistent.model");
  }

  @Test
  @DisplayName("Should return false when model alias is not valid")
  public void shouldReturnFalseWhenModelAliasIsNotValid() {
    // Arrange
    ModelRepository modelRepository = mock(ModelRepository.class);
    when(modelRepository.existsByAlias("invalid.model")).thenReturn(false);

    ModelResolutionService service = new ModelResolutionServiceImpl(modelRepository);

    // Act
    boolean result = service.isValidModelAlias("invalid.model");

    // Assert
    assertFalse(result);
    verify(modelRepository).existsByAlias("invalid.model");
  }

  @Test
  @DisplayName("Should return true when model alias is valid")
  public void shouldReturnTrueWhenModelAliasIsValid() {
    // Arrange
    ModelRepository modelRepository = mock(ModelRepository.class);
    when(modelRepository.existsByAlias("gemini.pro")).thenReturn(true);

    ModelResolutionService service = new ModelResolutionServiceImpl(modelRepository);

    // Act
    boolean result = service.isValidModelAlias("gemini.pro");

    // Assert
    assertTrue(result);
    verify(modelRepository).existsByAlias("gemini.pro");
  }

  @Test
  @DisplayName("Should handle null alias gracefully in resolveModel")
  public void shouldHandleNullAliasGracefullyInResolveModel() {
    // Arrange
    ModelRepository modelRepository = mock(ModelRepository.class);
    ModelResolutionService service = new ModelResolutionServiceImpl(modelRepository);

    // Act
    Model result = service.resolveModel(null);

    // Assert
    assertNull(result);
    verify(modelRepository, never()).findByAlias(anyString());
  }

  @Test
  @DisplayName("Should handle null alias gracefully in isValidModelAlias")
  public void shouldHandleNullAliasGracefullyInIsValidModelAlias() {
    // Arrange
    ModelRepository modelRepository = mock(ModelRepository.class);
    ModelResolutionService service = new ModelResolutionServiceImpl(modelRepository);

    // Act
    boolean result = service.isValidModelAlias(null);

    // Assert
    assertFalse(result);
    verify(modelRepository, never()).existsByAlias(anyString());
  }

  @Test
  @DisplayName("Should handle empty alias gracefully in resolveModel")
  public void shouldHandleEmptyAliasGracefullyInResolveModel() {
    // Arrange
    ModelRepository modelRepository = mock(ModelRepository.class);
    ModelResolutionService service = new ModelResolutionServiceImpl(modelRepository);

    // Act
    Model result = service.resolveModel("");

    // Assert
    assertNull(result);
    verify(modelRepository, never()).findByAlias(anyString());
  }

  @Test
  @DisplayName("Should handle empty alias gracefully in isValidModelAlias")
  public void shouldHandleEmptyAliasGracefullyInIsValidModelAlias() {
    // Arrange
    ModelRepository modelRepository = mock(ModelRepository.class);
    ModelResolutionService service = new ModelResolutionServiceImpl(modelRepository);

    // Act
    boolean result = service.isValidModelAlias("");

    // Assert
    assertFalse(result);
    verify(modelRepository, never()).existsByAlias(anyString());
  }
}
