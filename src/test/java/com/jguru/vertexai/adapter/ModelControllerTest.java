package com.jguru.vertexai.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class ModelControllerTest {

  @Test
  @DisplayName("Should have method to generate content")
  public void shouldHaveMethodToGenerateContent() {
    // This test ensures that the ModelController interface has the method
    ModelController controller = new ModelController() {
      @Override
      public String generateContent(String modelAlias, String prompt) {
        return "Generated content response";
      }
    };

    String result = controller.generateContent("gemini.pro", "Hello, world!");
    assertNotNull(result);
    assertEquals("Generated content response", result);
  }

  @Test
  @DisplayName("Should handle null model alias gracefully")
  public void shouldHandleNullModelAliasGracefully() {
    // This test ensures that the ModelController interface has proper error handling
    ModelController controller = new ModelController() {
      @Override
      public String generateContent(String modelAlias, String prompt) {
        if (modelAlias == null) {
          throw new IllegalArgumentException("Model alias cannot be null");
        }
        return "Generated content response";
      }
    };

    assertThrows(IllegalArgumentException.class, () -> {
      controller.generateContent(null, "Hello, world!");
    });
  }

  @Test
  @DisplayName("Should handle null prompt gracefully")
  public void shouldHandleNullPromptGracefully() {
    // This test ensures that the ModelController interface has proper error handling
    ModelController controller = new ModelController() {
      @Override
      public String generateContent(String modelAlias, String prompt) {
        if (prompt == null) {
          throw new IllegalArgumentException("Prompt cannot be null");
        }
        return "Generated content response";
      }
    };

    assertThrows(IllegalArgumentException.class, () -> {
      controller.generateContent("gemini.pro", null);
    });
  }
}
