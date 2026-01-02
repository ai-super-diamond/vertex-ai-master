package com.jguru.vertexai.infrastructure.impl;

import com.jguru.vertexai.infrastructure.GoogleVertexAIAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class GoogleVertexAIAdapterImplTest {

  @Test
  @DisplayName("Should call Vertex AI API successfully")
  public void shouldCallVertexAIAPISuccessfully() {
    // Arrange
    GoogleVertexAIAdapter adapter = new GoogleVertexAIAdapterImpl();

    // Act
    String result = adapter.callVertexAI("gemini-1.5-pro-001", "Hello, world!", "test-project", "us-central1", "test-key");

    // Assert
    assertNotNull(result);
    // For the mock implementation, we expect a mock response
    assertTrue(result.contains("gemini-1.5-pro-001"));
    assertTrue(result.contains("Hello, world!"));
  }

  @Test
  @DisplayName("Should handle null model name gracefully")
  public void shouldHandleNullModelNameGracefully() {
    // Arrange
    GoogleVertexAIAdapter adapter = new GoogleVertexAIAdapterImpl();

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> {
      adapter.callVertexAI(null, "Hello, world!", "test-project", "us-central1", "test-key");
    });
  }

  @Test
  @DisplayName("Should handle empty model name gracefully")
  public void shouldHandleEmptyModelNameGracefully() {
    // Arrange
    GoogleVertexAIAdapter adapter = new GoogleVertexAIAdapterImpl();

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> {
      adapter.callVertexAI("", "Hello, world!", "test-project", "us-central1", "test-key");
    });
  }

  @Test
  @DisplayName("Should handle blank model name gracefully")
  public void shouldHandleBlankModelNameGracefully() {
    // Arrange
    GoogleVertexAIAdapter adapter = new GoogleVertexAIAdapterImpl();

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> {
      adapter.callVertexAI("   ", "Hello, world!", "test-project", "us-central1", "test-key");
    });
  }

  @Test
  @DisplayName("Should handle null prompt gracefully")
  public void shouldHandleNullPromptGracefully() {
    // Arrange
    GoogleVertexAIAdapter adapter = new GoogleVertexAIAdapterImpl();

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> {
      adapter.callVertexAI("gemini-1.5-pro-001", null, "test-project", "us-central1", "test-key");
    });
  }

  @Test
  @DisplayName("Should handle empty prompt gracefully")
  public void shouldHandleEmptyPromptGracefully() {
    // Arrange
    GoogleVertexAIAdapter adapter = new GoogleVertexAIAdapterImpl();

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> {
      adapter.callVertexAI("gemini-1.5-pro-001", "", "test-project", "us-central1", "test-key");
    });
  }

  @Test
  @DisplayName("Should handle blank prompt gracefully")
  public void shouldHandleBlankPromptGracefully() {
    // Arrange
    GoogleVertexAIAdapter adapter = new GoogleVertexAIAdapterImpl();

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> {
      adapter.callVertexAI("gemini-1.5-pro-001", "   ", "test-project", "us-central1", "test-key");
    });
  }

  @Test
  @DisplayName("Should handle null project ID gracefully")
  public void shouldHandleNullProjectIdGracefully() {
    // Arrange
    GoogleVertexAIAdapter adapter = new GoogleVertexAIAdapterImpl();

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> {
      adapter.callVertexAI("gemini-1.5-pro-001", "Hello, world!", null, "us-central1", "test-key");
    });
  }

  @Test
  @DisplayName("Should handle null location gracefully")
  public void shouldHandleNullLocationGracefully() {
    // Arrange
    GoogleVertexAIAdapter adapter = new GoogleVertexAIAdapterImpl();

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> {
      adapter.callVertexAI("gemini-1.5-pro-001", "Hello, world!", "test-project", null, "test-key");
    });
  }
}
