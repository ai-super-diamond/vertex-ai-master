package com.jguru.vertexai.infrastructure.impl;

import com.jguru.vertexai.domain.entity.Model;
import com.jguru.vertexai.domain.repository.ModelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class ModelRepositoryImplTest {

  @Test
  @DisplayName("Should find model by alias successfully")
  public void shouldFindModelByAliasSuccessfully() {
    // Arrange
    ModelRepository repository = new ModelRepositoryImpl();

    // Act
    Model model = repository.findByAlias("gemini.pro");

    // Assert
    assertNotNull(model);
    assertEquals("gemini.pro", model.getAlias());
    assertEquals("gemini-1.5-pro-001", model.getFullName());
  }

  @Test
  @DisplayName("Should return null when model alias does not exist")
  public void shouldReturnNullWhenModelAliasDoesNotExist() {
    // Arrange
    ModelRepository repository = new ModelRepositoryImpl();

    // Act
    Model model = repository.findByAlias("non.existent");

    // Assert
    assertNull(model);
  }

  @Test
  @DisplayName("Should return true when model alias exists")
  public void shouldReturnTrueWhenModelAliasExists() {
    // Arrange
    ModelRepository repository = new ModelRepositoryImpl();

    // Act
    boolean exists = repository.existsByAlias("gemini.pro");

    // Assert
    assertTrue(exists);
  }

  @Test
  @DisplayName("Should return false when model alias does not exist")
  public void shouldReturnFalseWhenModelAliasDoesNotExist() {
    // Arrange
    ModelRepository repository = new ModelRepositoryImpl();

    // Act
    boolean exists = repository.existsByAlias("non.existent");

    // Assert
    assertFalse(exists);
  }

  @Test
  @DisplayName("Should handle null alias gracefully in findByAlias")
  public void shouldHandleNullAliasGracefullyInFindByAlias() {
    // Arrange
    ModelRepository repository = new ModelRepositoryImpl();

    // Act
    Model model = repository.findByAlias(null);

    // Assert
    assertNull(model);
  }

  @Test
  @DisplayName("Should handle null alias gracefully in existsByAlias")
  public void shouldHandleNullAliasGracefullyInExistsByAlias() {
    // Arrange
    ModelRepository repository = new ModelRepositoryImpl();

    // Act
    boolean exists = repository.existsByAlias(null);

    // Assert
    assertFalse(exists);
  }

  @Test
  @DisplayName("Should handle empty alias gracefully in findByAlias")
  public void shouldHandleEmptyAliasGracefullyInFindByAlias() {
    // Arrange
    ModelRepository repository = new ModelRepositoryImpl();

    // Act
    Model model = repository.findByAlias("");

    // Assert
    assertNull(model);
  }

  @Test
  @DisplayName("Should handle empty alias gracefully in existsByAlias")
  public void shouldHandleEmptyAliasGracefullyInExistsByAlias() {
    // Arrange
    ModelRepository repository = new ModelRepositoryImpl();

    // Act
    boolean exists = repository.existsByAlias("");

    // Assert
    assertFalse(exists);
  }
}
