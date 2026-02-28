package com.jguru.vertexai.application.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import com.jguru.vertexai.domain.dto.AuthenticationConfig;
import com.jguru.vertexai.domain.dto.AuthenticationType;
import org.junit.jupiter.api.BeforeEach;

public class GenerateContentRequestTest {

  private AuthenticationConfig authConfig;

  @BeforeEach
  void setUp() {
    authConfig = AuthenticationConfig.builder().withType(AuthenticationType.API_KEY).withApiKey("test-key").build();
  }

  @Test
  @DisplayName("Should create GenerateContentRequest with valid model alias and prompt")
  public void shouldCreateGenerateContentRequestWithValidModelAliasAndPrompt() {
    // Arrange & Act
    GenerateContentRequest request = new GenerateContentRequest("gemini.pro", "Hello, world!", authConfig);

    // Assert
    assertEquals("gemini.pro", request.getModelAlias());
    assertEquals("Hello, world!", request.getPrompt());
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when model alias is null")
  public void shouldThrowIllegalArgumentExceptionWhenModelAliasIsNull() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new GenerateContentRequest(null, "Hello, world!", authConfig);
    });
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when model alias is empty")
  public void shouldThrowIllegalArgumentExceptionWhenModelAliasIsEmpty() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new GenerateContentRequest("", "Hello, world!", authConfig);
    });
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when model alias is blank")
  public void shouldThrowIllegalArgumentExceptionWhenModelAliasIsBlank() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new GenerateContentRequest("   ", "Hello, world!", authConfig);
    });
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when prompt is null")
  public void shouldThrowIllegalArgumentExceptionWhenPromptIsNull() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new GenerateContentRequest("gemini.pro", null, authConfig);
    });
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when prompt is empty")
  public void shouldThrowIllegalArgumentExceptionWhenPromptIsEmpty() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new GenerateContentRequest("gemini.pro", "", authConfig);
    });
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when prompt is blank")
  public void shouldThrowIllegalArgumentExceptionWhenPromptIsBlank() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new GenerateContentRequest("gemini.pro", "   ", authConfig);
    });
  }

  @Test
  @DisplayName("Should have correct string representation")
  public void shouldHaveCorrectStringRepresentation() {
    // Arrange
    GenerateContentRequest request = new GenerateContentRequest("gemini.pro", "Hello, world!", authConfig);

    // Act
    String requestString = request.toString();

    // Assert
    assertTrue(requestString.contains("gemini.pro"));
    assertTrue(requestString.contains("Hello, world!"));
  }

  @Test
  @DisplayName("Should be equal to another request with same values")
  public void shouldBeEqualToAnotherRequestWithSameValues() {
    // Arrange
    GenerateContentRequest request1 = new GenerateContentRequest("gemini.pro", "Hello, world!", authConfig);
    GenerateContentRequest request2 = new GenerateContentRequest("gemini.pro", "Hello, world!", authConfig);

    // Assert
    assertEquals(request1, request2);
    assertEquals(request1.hashCode(), request2.hashCode());
  }

  @Test
  @DisplayName("Should not be equal to another request with different model alias")
  public void shouldNotBeEqualToAnotherRequestWithDifferentModelAlias() {
    // Arrange
    GenerateContentRequest request1 = new GenerateContentRequest("gemini.pro", "Hello, world!", authConfig);
    GenerateContentRequest request2 = new GenerateContentRequest("gpt4", "Hello, world!", authConfig);

    // Assert
    assertNotEquals(request1, request2);
  }

  @Test
  @DisplayName("Should not be equal to another request with different prompt")
  public void shouldNotBeEqualToAnotherRequestWithDifferentPrompt() {
    // Arrange
    GenerateContentRequest request1 = new GenerateContentRequest("gemini.pro", "Hello, world!", authConfig);
    GenerateContentRequest request2 = new GenerateContentRequest("gemini.pro", "Goodbye, world!", authConfig);

    // Assert
    assertNotEquals(request1, request2);
  }
}
