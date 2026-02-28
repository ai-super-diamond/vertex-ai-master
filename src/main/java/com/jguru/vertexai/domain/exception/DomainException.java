package com.jguru.vertexai.domain.exception;

/**
 * Base exception for all domain-level errors in the Vertex AI application.
 *
 * <p>
 * This exception serves as the root of the domain exception hierarchy, ensuring that all business logic errors are properly typed and can
 * be handled specifically by outer layers.
 * </p>
 *
 * <h2>Clean Architecture Principle:</h2>
 * <p>
 * Domain exceptions represent business rule violations or domain-level failures. They should not expose infrastructure details (e.g.,
 * database connection errors, HTTP status codes). Infrastructure layers should transform technical exceptions into domain exceptions when
 * crossing layer boundaries.
 * </p>
 *
 * @since 0.0.1
 */
public class DomainException extends Exception {

  public DomainException(String message) {
    super(message);
  }

  public DomainException(String message, Throwable cause) {
    super(message, cause);
  }
}
