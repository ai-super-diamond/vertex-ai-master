package com.jguru.vertexai.domain.exception;

/**
 * Exception thrown when an API call to an AI model fails.
 *
 * <p>
 * This exception wraps infrastructure-level failures (network errors, authentication issues, API errors) into a domain-level exception,
 * preventing infrastructure details from leaking into the domain and application layers.
 * </p>
 *
 * @since 0.0.1
 */
public class ApiCallException extends DomainException {

  private final String modelName;
  private final ErrorType errorType;

  /**
   * Creates a new ApiCallException.
   *
   * @param message
   *          Error message
   * @param modelName
   *          The model that was being called
   * @param errorType
   *          The type of error that occurred
   */
  public ApiCallException(String message, String modelName, ErrorType errorType) {
    super(message);
    this.modelName = modelName;
    this.errorType = errorType;
  }

  /**
   * Creates a new ApiCallException with a cause.
   *
   * @param message
   *          Error message
   * @param cause
   *          The underlying cause
   * @param modelName
   *          The model that was being called
   * @param errorType
   *          The type of error that occurred
   */
  public ApiCallException(String message, Throwable cause, String modelName, ErrorType errorType) {
    super(message, cause);
    this.modelName = modelName;
    this.errorType = errorType;
  }

  public String getModelName() {
    return modelName;
  }

  public ErrorType getErrorType() {
    return errorType;
  }

  /**
   * Categorizes API call errors for specific handling.
   */
  public enum ErrorType {
    /** Model or endpoint not found (404) */
    NOT_FOUND,

    /** Permission denied or authentication failed (403) */
    PERMISSION_DENIED,

    /** Network connectivity or timeout issues */
    NETWORK_ERROR,

    /** Invalid request format or parameters (400) */
    INVALID_REQUEST,

    /** Rate limiting or quota exceeded (429) */
    RATE_LIMITED,

    /** Unknown or uncategorized error */
    UNKNOWN
  }

  /**
   * Determines the ErrorType from an error message.
   *
   * @param errorMessage
   *          The error message to analyze
   * @return The appropriate ErrorType
   */
  public static ErrorType categorizeError(String errorMessage) {
    if (errorMessage == null) {
      return ErrorType.UNKNOWN;
    }

    String lowerMessage = errorMessage.toLowerCase();

    if (lowerMessage.contains("404") || lowerMessage.contains("not found")) {
      return ErrorType.NOT_FOUND;
    } else if (lowerMessage.contains("403") || lowerMessage.contains("permission denied") || lowerMessage.contains("unauthorized")) {
      return ErrorType.PERMISSION_DENIED;
    } else if (lowerMessage.contains("400") || lowerMessage.contains("invalid") || lowerMessage.contains("bad request")) {
      return ErrorType.INVALID_REQUEST;
    } else if (lowerMessage.contains("429") || lowerMessage.contains("rate limit") || lowerMessage.contains("quota")) {
      return ErrorType.RATE_LIMITED;
    } else if (lowerMessage.contains("timeout") || lowerMessage.contains("connection") || lowerMessage.contains("network")) {
      return ErrorType.NETWORK_ERROR;
    }

    return ErrorType.UNKNOWN;
  }
}
