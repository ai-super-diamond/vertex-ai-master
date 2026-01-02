package com.jguru.vertexai.domain.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class ModelTest {

  @Test
  @DisplayName("Should create Model with valid alias and full name")
  public void shouldCreateModelWithValidAliasAndFullName() {
    // Arrange & Act
    Model model = new Model("gemini.pro", "gemini-1.5-pro-001");

    // Assert
    assertEquals("gemini.pro", model.getAlias());
    assertEquals("gemini-1.5-pro-001", model.getFullName());
    assertFalse(model.isGlobal());
  }

  @Test
  @DisplayName("Should create Model with global region setting")
  public void shouldCreateModelWithGlobalRegionSetting() {
    // Arrange & Act
    Model model = new Model("global-model", "global-full-name", true);

    // Assert
    assertEquals("global-model", model.getAlias());
    assertEquals("global-full-name", model.getFullName());
    assertTrue(model.isGlobal());
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when alias is null")
  public void shouldThrowIllegalArgumentExceptionWhenAliasIsNull() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new Model(null, "gemini-1.5-pro-001");
    });
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when alias is empty")
  public void shouldThrowIllegalArgumentExceptionWhenAliasIsEmpty() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new Model("", "gemini-1.5-pro-001");
    });
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when alias is blank")
  public void shouldThrowIllegalArgumentExceptionWhenAliasIsBlank() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new Model("   ", "gemini-1.5-pro-001");
    });
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when full name is null")
  public void shouldThrowIllegalArgumentExceptionWhenFullNameIsNull() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new Model("gemini.pro", null);
    });
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when full name is empty")
  public void shouldThrowIllegalArgumentExceptionWhenFullNameIsEmpty() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new Model("gemini.pro", "");
    });
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when full name is blank")
  public void shouldThrowIllegalArgumentExceptionWhenFullNameIsBlank() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new Model("gemini.pro", "   ");
    });
  }

  @Test
  @DisplayName("Should create Model with default non-global setting when using two-parameter constructor")
  public void shouldCreateModelWithDefaultNonGlobalSetting() {
    // Arrange & Act
    Model model = new Model("gemini.pro", "gemini-1.5-pro-001");

    // Assert
    assertFalse(model.isGlobal());
  }

  @Test
  @DisplayName("Should have correct string representation")
  public void shouldHaveCorrectStringRepresentation() {
    // Arrange
    Model model = new Model("gemini.pro", "gemini-1.5-pro-001");

    // Act
    String modelString = model.toString();

    // Assert
    assertTrue(modelString.contains("gemini.pro"));
    assertTrue(modelString.contains("gemini-1.5-pro-001"));
    assertFalse(modelString.contains("global=true"));
  }

  @Test
  @DisplayName("Should have correct string representation for global model")
  public void shouldHaveCorrectStringRepresentationForGlobalModel() {
    // Arrange
    Model model = new Model("global-model", "global-full-name", true);

    // Act
    String modelString = model.toString();

    // Assert
    assertTrue(modelString.contains("global-model"));
    assertTrue(modelString.contains("global-full-name"));
    assertTrue(modelString.contains("global=true"));
  }
}
