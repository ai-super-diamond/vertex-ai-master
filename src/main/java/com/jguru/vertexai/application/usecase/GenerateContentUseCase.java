package com.jguru.vertexai.application.usecase;

import com.jguru.vertexai.application.dto.GenerateContentRequest;
import com.jguru.vertexai.domain.exception.ApiCallException;
import com.jguru.vertexai.domain.exception.ModelNotFoundException;

/**
 * Use case for generating content using AI models.
 *
 * <p>
 * This interface defines the application's content generation operation. It accepts a request containing all necessary parameters and
 * returns the generated content.
 * </p>
 */
public interface GenerateContentUseCase {

  /**
   * Executes the content generation use case.
   *
   * @param request
   *          The generation request containing model alias, prompt, and authentication
   * @return The generated content
   * @throws ModelNotFoundException
   *           If the specified model cannot be found
   * @throws ApiCallException
   *           If the API call fails
   */
  String execute(GenerateContentRequest request) throws ModelNotFoundException, ApiCallException;
}
