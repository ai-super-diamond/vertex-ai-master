package com.jguru.vertexai.service;

import com.jguru.vertexai.domain.exception.ApiCallException;
import com.jguru.vertexai.domain.dto.GenerationResult;

/**
 * Abstraction for AI model clients that interact with various AI platforms.
 *
 * <p>
 * This interface provides a unified abstraction over different AI model providers (e.g., Vertex AI, Chat Completions API) allowing for
 * flexible implementation swapping, testing with mocks, and consistent error handling.
 * </p>
 *
 * <h2>Design Benefits:</h2>
 * <ul>
 * <li><b>Testability:</b> Enables easy mocking in unit tests</li>
 * <li><b>Flexibility:</b> Swap implementations without changing business logic</li>
 * <li><b>Dependency Inversion:</b> Service layer depends on abstraction, not concrete implementation</li>
 * <li><b>Single Responsibility:</b> Each method focuses on one type of API call</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 *
 * <pre>{@code
 * // Production usage
 * ModelClientFactory factory = new VertexAiClientFactory();
 * ModelClient client = factory.createClient(authConfig);
 * String response = client.callVertexAi("gemini-pro", "Hello world");
 *
 * // Test usage with mock
 * ModelClient mockClient = mock(ModelClient.class);
 * when(mockClient.callVertexAi(any(), any())).thenReturn("Mocked response");
 * }</pre>
 *
 * @see VertexAiClient
 * @see ModelClientFactory
 * @since 0.0.1
 */
public interface ModelClient {
  /**
   * Calls the AI model with automatic routing to the appropriate API endpoint.
   *
   * <p>
   * This is the main entry point for model invocation. It automatically routes the request to either:
   * </p>
   * <ul>
   * <li>Standard Vertex AI API for Gemini/Llama models</li>
   * <li>Chat Completions API for MaaS (Model-as-a-Service) models</li>
   * </ul>
   *
   * <p>
   * The routing decision is based on model properties configuration that identifies the model provider (e.g., deepseek-ai, openai,
   * google-openai).
   *
   * /** Calls the AI model API to generate a text response.
   *
   * @param modelName
   *          The model to use
   * @param text
   *          The prompt text
   * @return Generated response text
   * @throws ApiCallException
   *           If the API call fails
   */
  String callVertexAi(String modelName, String text) throws ApiCallException;

  /**
   * Calls the standard Vertex AI API for supported models.
   *
   * @param modelName
   *          The model to use
   * @param textPrompt
   *          The prompt text
   * @return GenerationResult containing the response and metadata
   * @throws ApiCallException
   *           If the API call fails
   */
  GenerationResult callStandardVertexAi(String modelName, String textPrompt) throws ApiCallException;

  /**
   * Calls the Chat Completions API for MaaS and third-party models.
   *
   * @param provider
   *          The provider name (e.g., \"deepseek-ai\", \"openai\")
   * @param modelName
   *          The model to use
   * @param textPrompt
   *          The prompt text
   * @return GenerationResult containing the response and metadata
   * @throws ApiCallException
   *           If the API call fails
   */
  GenerationResult callChatCompletionsApi(String provider, String modelName, String textPrompt) throws ApiCallException;
}
