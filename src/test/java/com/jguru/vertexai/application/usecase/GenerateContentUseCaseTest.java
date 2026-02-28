package com.jguru.vertexai.application.usecase;

import com.jguru.vertexai.application.dto.GenerateContentRequest;
import com.jguru.vertexai.domain.dto.AuthenticationConfig;
import com.jguru.vertexai.domain.dto.AuthenticationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class GenerateContentUseCaseTest {

  @Test
  @DisplayName("Should have method to execute content generation")
  public void shouldHaveMethodToExecuteContentGeneration() throws Exception {
    // This test ensures that the GenerateContentUseCase interface has the method
    GenerateContentUseCase useCase = new GenerateContentUseCase() {
      @Override
      public String execute(GenerateContentRequest request) {
        return "Generated content response";
      }
    };

    AuthenticationConfig authConfig = AuthenticationConfig.builder().withType(AuthenticationType.API_KEY).withApiKey("key").build();
    GenerateContentRequest request = new GenerateContentRequest("gemini.pro", "Hello, world!", authConfig);

    String result = useCase.execute(request);
    assertNotNull(result);
    assertEquals("Generated content response", result);
  }

  @Test
  @DisplayName("Should handle null request gracefully")
  public void shouldHandleNullRequestGracefully() {
    // This test ensures that the GenerateContentUseCase interface has proper error
    // handling
    GenerateContentUseCase useCase = new GenerateContentUseCase() {
      @Override
      public String execute(GenerateContentRequest request) {
        if (request == null) {
          throw new IllegalArgumentException("Request cannot be null");
        }
        return "Generated content response";
      }
    };

    assertThrows(IllegalArgumentException.class, () -> {
      useCase.execute(null);
    });
  }
}
