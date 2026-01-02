package com.jguru.vertexai.service;

import com.jguru.vertexai.service.dto.GenerationResult;
import java.io.IOException;

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
   * </p>
   *
   * @param modelName
   *          The model name or alias (e.g., "gemini-1.5-pro", "deepseek-chat")
   * @param text
   *          The prompt text to send to the model
   * @return The generated text response from the model
   * @throws IOException
   *           If the API call fails due to network, authentication, or API errors
   * @throws IllegalArgumentException
   *           If modelName or text is null or empty
   */
  String callVertexAi(String modelName, String text) throws IOException;

  /**
   * Calls the standard Vertex AI API for Google's native models (Gemini, Llama).
   *
   * <p>
   * This method directly invokes the Vertex AI endpoint without routing logic. It's used for models that are hosted natively on Vertex AI
   * platform.
   * </p>
   *
   * <p>
   * <b>Typical Models:</b> gemini-1.5-pro, gemini-1.5-flash, llama-3.1-405b
   * </p>
   *
   * @param modelName
   *          The native Vertex AI model name
   * @param textPrompt
   *          The prompt text
   * @return GenerationResult containing the response text and metadata
   * @throws IOException
   *           If the API call fails
   */
  GenerationResult callStandardVertexAi(String modelName, String textPrompt) throws IOException;

  /**
   * Calls the Chat Completions API for MaaS (Model-as-a-Service) models.
   *
   * <p>
   * This method is used for third-party models integrated into Vertex AI through the Chat Completions API endpoint. These models require a
   * provider prefix in the model identifier.
   * </p>
   *
   * <p>
   * <b>Supported Providers:</b> deepseek-ai, openai, google-openai, anthropic, etc.
   * </p>
   *
   * <p>
   * <b>Example Model Identifiers:</b>
   * </p>
   * <ul>
   * <li>deepseek-ai/deepseek-chat</li>
   * <li>openai/gpt-4o</li>
   * <li>google/gemini-2.0-flash-exp (google-openai provider)</li>
   * </ul>
   *
   * @param provider
   *          The model provider identifier (e.g., "deepseek-ai", "openai")
   * @param modelName
   *          The model name without provider prefix
   * @param textPrompt
   *          The prompt text
   * @return GenerationResult containing the response text and metadata
   * @throws IOException
   *           If the API call fails
   */
  GenerationResult callChatCompletionsApi(String provider, String modelName, String textPrompt) throws IOException;
}
