package com.jguru.vertexai.adapter.impl;

import com.jguru.vertexai.adapter.GenerateContentPresenter;
import com.jguru.vertexai.adapter.ModelController;
import com.jguru.vertexai.application.dto.GenerateContentRequest;
import com.jguru.vertexai.application.usecase.GenerateContentUseCase;
import com.jguru.vertexai.domain.dto.AuthenticationConfig;
import com.jguru.vertexai.domain.dto.AuthenticationType;

public class ModelControllerImpl implements ModelController {
  private final GenerateContentUseCase generateContentUseCase;
  private final GenerateContentPresenter presenter;
  private final String apiKey;

  public ModelControllerImpl(GenerateContentUseCase generateContentUseCase, GenerateContentPresenter presenter) {
    this(generateContentUseCase, presenter, System.getenv("GEMINI_API_KEY"));
  }

  public ModelControllerImpl(GenerateContentUseCase generateContentUseCase, GenerateContentPresenter presenter, String apiKey) {
    this.generateContentUseCase = generateContentUseCase;
    this.presenter = presenter;
    this.apiKey = apiKey;
  }

  @Override
  public String generateContent(String modelAlias, String prompt) {
    try {
      // Validate inputs
      if (modelAlias == null || modelAlias.trim().isEmpty()) {
        return presenter.presentError("Model alias cannot be null or empty");
      }
      if (prompt == null || prompt.trim().isEmpty()) {
        return presenter.presentError("Prompt cannot be null or empty");
      }

      // Execute the use case
      // Create a request with API_KEY auth (adapter layer doesn't handle auth
      // details)
      AuthenticationConfig authConfig = AuthenticationConfig.builder().withType(AuthenticationType.API_KEY).withApiKey(this.apiKey).build();
      GenerateContentRequest request = new GenerateContentRequest(modelAlias, prompt, authConfig);
      String result = generateContentUseCase.execute(request);

      // Format and return the success response
      return presenter.presentSuccess(result);
    } catch (Exception e) {
      // Format and return the error response
      return presenter.presentError(e.getMessage());
    }
  }
}
