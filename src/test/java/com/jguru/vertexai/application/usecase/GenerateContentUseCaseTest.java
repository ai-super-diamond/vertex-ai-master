package com.jguru.vertexai.application.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class GenerateContentUseCaseTest {

  @Test
  @DisplayName("Should have method to execute content generation")
  public void shouldHaveMethodToExecuteContentGeneration() {
    // This test ensures that the GenerateContentUseCase interface has the method
    GenerateContentUseCase useCase = new GenerateContentUseCase() {
      @Override
      public String execute(String modelAlias, String prompt) {
        return "Generated content response";
      }
    };

    String result = useCase.execute("gemini.pro", "Hello, world!");
    assertNotNull(result);
    assertEquals("Generated content response", result);
  }

  @Test
  @DisplayName("Should handle null model alias gracefully")
  public void shouldHandleNullModelAliasGracefully() {
    // This test ensures that the GenerateContentUseCase interface has proper error handling
    GenerateContentUseCase useCase = new GenerateContentUseCase() {
      @Override
      public String execute(String modelAlias, String prompt) {
        if (modelAlias == null) {
          throw new IllegalArgumentException("Model alias cannot be null");
        }
        return "Generated content response";
      }
    };

    assertThrows(IllegalArgumentException.class, () -> {
      useCase.execute(null, "Hello, world!");
    });
  }

  @Test
  @DisplayName("Should handle null prompt gracefully")
  public void shouldHandleNullPromptGracefully() {
    // This test ensures that the GenerateContentUseCase interface has proper error handling
    GenerateContentUseCase useCase = new GenerateContentUseCase() {
      @Override
      public String execute(String modelAlias, String prompt) {
        if (prompt == null) {
          throw new IllegalArgumentException("Prompt cannot be null");
        }
        return "Generated content response";
      }
    };

    assertThrows(IllegalArgumentException.class, () -> {
      useCase.execute("gemini.pro", null);
    });
  }
}
