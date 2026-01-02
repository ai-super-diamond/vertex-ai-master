package com.jguru.vertexai.adapter.impl;

import com.jguru.vertexai.adapter.GenerateContentPresenter;
import com.jguru.vertexai.adapter.ModelController;
import com.jguru.vertexai.application.usecase.GenerateContentUseCase;

public class ModelControllerImpl implements ModelController {
  private final GenerateContentUseCase generateContentUseCase;
  private final GenerateContentPresenter presenter;

  public ModelControllerImpl(GenerateContentUseCase generateContentUseCase, GenerateContentPresenter presenter) {
    this.generateContentUseCase = generateContentUseCase;
    this.presenter = presenter;
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
      String result = generateContentUseCase.execute(modelAlias, prompt);

      // Format and return the success response
      return presenter.presentSuccess(result);
    } catch (Exception e) {
      // Format and return the error response
      return presenter.presentError(e.getMessage());
    }
  }
}
