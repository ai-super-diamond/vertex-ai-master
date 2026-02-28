package com.jguru.vertexai.infrastructure.client;

import com.jguru.vertexai.service.ModelClient;
import com.jguru.vertexai.service.ModelClientFactory;
import com.jguru.vertexai.domain.dto.AuthenticationConfig;

/**
 * Production implementation of ModelClientFactory that creates VertexAiClient instances.
 *
 * <p>
 * This factory is the default production implementation used throughout the application. It creates fully configured VertexAiClient
 * instances that can communicate with:
 * </p>
 * <ul>
 * <li>Google Vertex AI (native Gemini/Llama models)</li>
 * <li>Chat Completions API (MaaS third-party models)</li>
 * </ul>
 *
 * <h2>Thread Safety:</h2>
 * <p>
 * This factory is stateless and thread-safe. It can be shared across multiple threads and used concurrently without synchronization.
 * </p>
 *
 * <h2>Usage:</h2>
 *
 * <pre>{@code
 * // In application initialization
 * ModelClientFactory factory = new VertexAiClientFactory();
 *
 * // Pass to service layer via dependency injection
 * VertexAiService service = new VertexAiServiceImpl(regionProvider, factory, // <-- Factory injected here
 *     modelProperties);
 *
 * // Service uses factory to create clients on demand
 * ModelClient client = factory.createClient(authConfig);
 * }</pre>
 *
 * <h2>Testing Alternative:</h2>
 * <p>
 * For testing, create a mock factory instead:
 * </p>
 *
 * <pre>{@code
 * ModelClientFactory mockFactory = authConfig -> {
 *   ModelClient mockClient = mock(ModelClient.class);
 *   when(mockClient.callVertexAi(any(), any())).thenReturn("Test response");
 *   return mockClient;
 * };
 * }</pre>
 *
 * @see ModelClient
 * @see ModelClientFactory
 * @see VertexAiClient
 * @since 0.0.1
 */
public class VertexAiClientFactory implements ModelClientFactory {

  /**
   * Creates a new VertexAiClient instance configured with the provided authentication.
   *
   * <p>
   * The created client will be fully configured and ready to make API calls. Model properties are loaded from the classpath or external
   * configuration.
   * </p>
   *
   * @param authConfig
   *          The authentication configuration containing either:
   *          <ul>
   *          <li>API key for Gemini API access</li>
   *          <li>Service account credentials for Vertex AI</li>
   *          </ul>
   * @return A new VertexAiClient instance ready for use
   * @throws IllegalArgumentException
   *           If authConfig is null or invalid (thrown by VertexAiClient constructor)
   */
  @Override
  public ModelClient createClient(AuthenticationConfig authConfig) {
    return new VertexAiClient(authConfig);
  }
}
