package com.jguru.vertexai.service;

import com.jguru.vertexai.domain.dto.AuthenticationConfig;

/**
 * Factory interface for creating ModelClient instances.
 *
 * <p>
 * This factory pattern provides several key benefits:
 * </p>
 * <ul>
 * <li><b>Encapsulation:</b> Hides client creation complexity</li>
 * <li><b>Flexibility:</b> Easy to swap implementations (production vs test)</li>
 * <li><b>Dependency Injection:</b> Service layer receives factory, not concrete clients</li>
 * <li><b>Testability:</b> Can inject mock factories in tests</li>
 * </ul>
 *
 * <h2>Implementation Strategy:</h2>
 * <p>
 * Implementations of this factory should:
 * </p>
 * <ol>
 * <li>Validate the authentication configuration</li>
 * <li>Load necessary resources (model properties, configurations)</li>
 * <li>Instantiate the appropriate ModelClient implementation</li>
 * <li>Handle any initialization errors gracefully</li>
 * </ol>
 *
 * <h2>Usage Examples:</h2>
 *
 * <h3>Production Usage:</h3>
 *
 * <pre>{@code
 * // Service layer receives factory via constructor injection
 * public class VertexAiServiceImpl {
 *   private final ModelClientFactory clientFactory;
 *
 *   public VertexAiServiceImpl(ModelClientFactory clientFactory) {
 *     this.clientFactory = clientFactory;
 *   }
 *
 *   public String generateContent(AuthenticationConfig auth, String prompt) {
 *     ModelClient client = clientFactory.createClient(auth);
 *     return client.callVertexAi("gemini-pro", prompt);
 *   }
 * }
 * }</pre>
 *
 * <h3>Test Usage:</h3>
 *
 * <pre>{@code
 * // Create mock factory for testing
 * ModelClientFactory mockFactory = mock(ModelClientFactory.class);
 * ModelClient mockClient = mock(ModelClient.class);
 * when(mockFactory.createClient(any())).thenReturn(mockClient);
 * when(mockClient.callVertexAi(any(), any())).thenReturn("Test response");
 *
 * // Inject mock factory into service
 * VertexAiService service = new VertexAiServiceImpl(regionProvider, mockFactory, properties);
 * }</pre>
 *
 * @see ModelClient
 * @see VertexAiClientFactory
 * @see AuthenticationConfig
 * @since 0.0.1
 */
public interface ModelClientFactory {
  /**
   * Creates a new ModelClient instance configured with the provided authentication.
   *
   * <p>
   * The factory is responsible for:
   * </p>
   * <ul>
   * <li>Validating the authentication configuration</li>
   * <li>Loading model properties and configurations</li>
   * <li>Instantiating the appropriate client implementation</li>
   * <li>Configuring the client with necessary credentials</li>
   * </ul>
   *
   * <p>
   * <b>Thread Safety:</b> Implementations should be thread-safe as factories are typically shared across multiple threads.
   * </p>
   *
   * <p>
   * <b>Error Handling:</b> If client creation fails, implementations may either:
   * </p>
   * <ul>
   * <li>Throw a runtime exception (fail-fast approach)</li>
   * <li>Return a client that throws exceptions on usage (lazy failure)</li>
   * </ul>
   *
   * @param authConfig
   *          The authentication configuration (API key or service account)
   * @return A fully configured ModelClient ready for use
   * @throws IllegalArgumentException
   *           If authConfig is null or invalid
   * @throws IllegalStateException
   *           If required resources cannot be loaded
   */
  ModelClient createClient(AuthenticationConfig authConfig);
}
