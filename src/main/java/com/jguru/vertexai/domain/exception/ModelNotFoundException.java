package com.jguru.vertexai.domain.exception;

/**
 * Exception thrown when a requested model cannot be found in the model repository.
 *
 * <p>
 * This exception represents a specific business rule violation: attempting to use a model that doesn't exist or hasn't been configured in
 * the system.
 * </p>
 *
 * @since 0.0.1
 */
public class ModelNotFoundException extends DomainException {

  private final String modelAlias;

  /**
   * Creates a new ModelNotFoundException.
   *
   * @param modelAlias
   *          The alias of the model that was not found
   */
  public ModelNotFoundException(String modelAlias) {
    super("Model with alias '" + modelAlias + "' not found");
    this.modelAlias = modelAlias;
  }

  /**
   * Gets the model alias that was not found.
   *
   * @return The model alias
   */
  public String getModelAlias() {
    return modelAlias;
  }
}
