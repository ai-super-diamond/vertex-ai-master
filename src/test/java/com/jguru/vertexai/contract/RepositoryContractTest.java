package com.jguru.vertexai.contract;

import com.jguru.vertexai.domain.entity.Model;
import com.jguru.vertexai.domain.repository.ModelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class RepositoryContractTest {

  // This test class defines the contract that all ModelRepository implementations must follow
  // We'll use a concrete implementation for testing the contract
  private final ModelRepository repository = new com.jguru.vertexai.infrastructure.impl.ModelRepositoryImpl();

  @Test
  @DisplayName("Contract: findByAlias should return null for non-existent alias")
  public void contractFindByAliasShouldReturnNullForNonExistentAlias() {
    // Arrange & Act
    Model result = repository.findByAlias("nonexistent.model");

    // Assert
    assertNull(result, "Repository contract: findByAlias should return null for non-existent alias");
  }

  @Test
  @DisplayName("Contract: findByAlias should return Model for existing alias")
  public void contractFindByAliasShouldReturnModelForExistingAlias() {
    // Arrange & Act
    Model result = repository.findByAlias("gemini.pro");

    // Assert
    assertNotNull(result, "Repository contract: findByAlias should return Model for existing alias");
    assertEquals("gemini.pro", result.getAlias(), "Repository contract: returned model should have correct alias");
    assertNotNull(result.getFullName(), "Repository contract: returned model should have full name");
  }

  @Test
  @DisplayName("Contract: existsByAlias should return false for non-existent alias")
  public void contractExistsByAliasShouldReturnFalseForNonExistentAlias() {
    // Arrange & Act
    boolean result = repository.existsByAlias("nonexistent.model");

    // Assert
    assertFalse(result, "Repository contract: existsByAlias should return false for non-existent alias");
  }

  @Test
  @DisplayName("Contract: existsByAlias should return true for existing alias")
  public void contractExistsByAliasShouldReturnTrueForExistingAlias() {
    // Arrange & Act
    boolean result = repository.existsByAlias("gemini.pro");

    // Assert
    assertTrue(result, "Repository contract: existsByAlias should return true for existing alias");
  }

  @Test
  @DisplayName("Contract: findByAlias should handle null alias gracefully")
  public void contractFindByAliasShouldHandleNullAliasGracefully() {
    // Arrange & Act
    Model result = repository.findByAlias(null);

    // Assert
    assertNull(result, "Repository contract: findByAlias should handle null alias gracefully by returning null");
  }

  @Test
  @DisplayName("Contract: existsByAlias should handle null alias gracefully")
  public void contractExistsByAliasShouldHandleNullAliasGracefully() {
    // Arrange & Act
    boolean result = repository.existsByAlias(null);

    // Assert
    assertFalse(result, "Repository contract: existsByAlias should handle null alias gracefully by returning false");
  }

  @Test
  @DisplayName("Contract: findByAlias should handle empty alias gracefully")
  public void contractFindByAliasShouldHandleEmptyAliasGracefully() {
    // Arrange & Act
    Model result = repository.findByAlias("");

    // Assert
    assertNull(result, "Repository contract: findByAlias should handle empty alias gracefully by returning null");
  }

  @Test
  @DisplayName("Contract: existsByAlias should handle empty alias gracefully")
  public void contractExistsByAliasShouldHandleEmptyAliasGracefully() {
    // Arrange & Act
    boolean result = repository.existsByAlias("");

    // Assert
    assertFalse(result, "Repository contract: existsByAlias should handle empty alias gracefully by returning false");
  }

  @Test
  @DisplayName("Contract: Implementation should be consistent across multiple calls")
  public void contractImplementationShouldBeConsistentAcrossMultipleCalls() {
    // Arrange - Multiple calls to the same method with same parameters

    // Act
    Model result1 = repository.findByAlias("gemini.pro");
    Model result2 = repository.findByAlias("gemini.pro");
    boolean exists1 = repository.existsByAlias("gemini.pro");
    boolean exists2 = repository.existsByAlias("gemini.pro");

    // Assert
    assertEquals(result1.getAlias(), result2.getAlias(), "Repository contract: Same query should return consistent alias");
    assertEquals(result1.getFullName(), result2.getFullName(), "Repository contract: Same query should return consistent full name");
    assertEquals(exists1, exists2, "Repository contract: Same existence check should return consistent result");
  }

  @Test
  @DisplayName("Contract: Repository should not return objects with null essential properties")
  public void contractRepositoryShouldNotReturnObjectsWithNullEssentialProperties() {
    // Arrange & Act
    Model result = repository.findByAlias("gemini.pro");

    // Assert
    assertNotNull(result.getAlias(), "Repository contract: Returned model should not have null alias");
    assertNotNull(result.getFullName(), "Repository contract: Returned model should not have null full name");
  }
}
