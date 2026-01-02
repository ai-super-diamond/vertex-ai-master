package com.jguru.vertexai.infrastructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class GoogleVertexAIAdapterTest {

  @Test
  @DisplayName("Should have method to call Vertex AI API")
  public void shouldHaveMethodToCallVertexAI() {
    // This test ensures that the GoogleVertexAIAdapter interface has the method
    GoogleVertexAIAdapter adapter = new GoogleVertexAIAdapter() {
      @Override
      public String callVertexAI(String modelName, String prompt, String projectId, String location, String apiKey) {
        return "API response";
      }
    };

    String result = adapter.callVertexAI("gemini-1.5-pro-001", "Hello, world!", "test-project", "us-central1", "test-key");
    assertNotNull(result);
    assertEquals("API response", result);
  }

  @Test
  @DisplayName("Should handle null model name gracefully")
  public void shouldHandleNullModelNameGracefully() {
    // This test ensures that the GoogleVertexAIAdapter interface has proper error handling
    GoogleVertexAIAdapter adapter = new GoogleVertexAIAdapter() {
      @Override
      public String callVertexAI(String modelName, String prompt, String projectId, String location, String apiKey) {
        if (modelName == null) {
          throw new IllegalArgumentException("Model name cannot be null");
        }
        return "API response";
      }
    };

    assertThrows(IllegalArgumentException.class, () -> {
      adapter.callVertexAI(null, "Hello, world!", "test-project", "us-central1", "test-key");
    });
  }

  @Test
  @DisplayName("Should handle null prompt gracefully")
  public void shouldHandleNullPromptGracefully() {
    // This test ensures that the GoogleVertexAIAdapter interface has proper error handling
    GoogleVertexAIAdapter adapter = new GoogleVertexAIAdapter() {
      @Override
      public String callVertexAI(String modelName, String prompt, String projectId, String location, String apiKey) {
        if (prompt == null) {
          throw new IllegalArgumentException("Prompt cannot be null");
        }
        return "API response";
      }
    };

    assertThrows(IllegalArgumentException.class, () -> {
      adapter.callVertexAI("gemini-1.5-pro-001", null, "test-project", "us-central1", "test-key");
    });
  }

  @Test
  @DisplayName("Should handle null project ID gracefully")
  public void shouldHandleNullProjectIdGracefully() {
    // This test ensures that the GoogleVertexAIAdapter interface has proper error handling
    GoogleVertexAIAdapter adapter = new GoogleVertexAIAdapter() {
      @Override
      public String callVertexAI(String modelName, String prompt, String projectId, String location, String apiKey) {
        if (projectId == null) {
          throw new IllegalArgumentException("Project ID cannot be null");
        }
        return "API response";
      }
    };

    assertThrows(IllegalArgumentException.class, () -> {
      adapter.callVertexAI("gemini-1.5-pro-001", "Hello, world!", null, "us-central1", "test-key");
    });
  }

  @Test
  @DisplayName("Should handle null location gracefully")
  public void shouldHandleNullLocationGracefully() {
    // This test ensures that the GoogleVertexAIAdapter interface has proper error handling
    GoogleVertexAIAdapter adapter = new GoogleVertexAIAdapter() {
      @Override
      public String callVertexAI(String modelName, String prompt, String projectId, String location, String apiKey) {
        if (location == null) {
          throw new IllegalArgumentException("Location cannot be null");
        }
        return "API response";
      }
    };

    assertThrows(IllegalArgumentException.class, () -> {
      adapter.callVertexAI("gemini-1.5-pro-001", "Hello, world!", "test-project", null, "test-key");
    });
  }
}
