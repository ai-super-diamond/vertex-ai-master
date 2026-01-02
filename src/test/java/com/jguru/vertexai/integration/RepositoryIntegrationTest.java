package com.jguru.vertexai.integration;

import com.jguru.vertexai.domain.entity.Model;
import com.jguru.vertexai.domain.repository.ModelRepository;
import com.jguru.vertexai.infrastructure.impl.ModelRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class RepositoryIntegrationTest {

  @Test
  @DisplayName("Should persist and retrieve Model through repository implementation")
  public void shouldPersistAndRetrieveModelThroughRepositoryImplementation() {
    // Arrange
    ModelRepository repository = new ModelRepositoryImpl();

    // Act & Assert - Verify initial state
    Model existingModel = repository.findByAlias("gemini.pro");
    assertNotNull(existingModel, "Initial model should exist");
    assertEquals("gemini.pro", existingModel.getAlias());
    assertEquals("gemini-1.5-pro-001", existingModel.getFullName());

    // Act & Assert - Verify existence check
    boolean exists = repository.existsByAlias("gemini.pro");
    assertTrue(exists, "Model should exist");

    // Act & Assert - Verify non-existent model
    Model nonExistentModel = repository.findByAlias("nonexistent.model");
    assertNull(nonExistentModel, "Non-existent model should return null");

    boolean nonExistentExists = repository.existsByAlias("nonexistent.model");
    assertFalse(nonExistentExists, "Non-existent model should return false for existence");

    // Act & Assert - Verify null handling
    Model nullModel = repository.findByAlias(null);
    assertNull(nullModel, "Null alias should return null");

    boolean nullExists = repository.existsByAlias(null);
    assertFalse(nullExists, "Null alias should return false for existence");

    // Act & Assert - Verify empty string handling
    Model emptyModel = repository.findByAlias("");
    assertNull(emptyModel, "Empty alias should return null");

    boolean emptyExists = repository.existsByAlias("");
    assertFalse(emptyExists, "Empty alias should return false for existence");
  }

  @Test
  @DisplayName("Should correctly identify valid and invalid model aliases")
  public void shouldCorrectlyIdentifyValidAndInvalidModelAliases() {
    // Arrange
    ModelRepository repository = new ModelRepositoryImpl();

    // Act & Assert - Valid aliases
    assertTrue(repository.existsByAlias("gemini.pro"), "Valid alias should exist");
    assertTrue(repository.existsByAlias("gpt4"), "Valid alias should exist");
    assertTrue(repository.existsByAlias("claude"), "Valid alias should exist");

    // Act & Assert - Invalid aliases
    assertFalse(repository.existsByAlias("invalid.model"), "Invalid alias should not exist");
    assertFalse(repository.existsByAlias("nonexistent"), "Invalid alias should not exist");
    assertFalse(repository.existsByAlias(""), "Empty alias should not exist");
    assertFalse(repository.existsByAlias(null), "Null alias should not exist");
  }

  @Test
  @DisplayName("Should return consistent results for same query")
  public void shouldReturnConsistentResultsForSameQuery() {
    // Arrange
    ModelRepository repository = new ModelRepositoryImpl();

    // Act - Multiple queries for the same model
    Model result1 = repository.findByAlias("gemini.pro");
    Model result2 = repository.findByAlias("gemini.pro");
    Model result3 = repository.findByAlias("gemini.pro");

    // Assert - All results should be equivalent
    assertNotNull(result1);
    assertNotNull(result2);
    assertNotNull(result3);

    assertEquals(result1.getAlias(), result2.getAlias(), "Aliases should match");
    assertEquals(result2.getAlias(), result3.getAlias(), "Aliases should match");
    assertEquals(result1.getFullName(), result2.getFullName(), "Full names should match");
    assertEquals(result2.getFullName(), result3.getFullName(), "Full names should match");
  }
}
