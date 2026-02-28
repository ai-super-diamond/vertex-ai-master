package com.jguru.vertexai.service.dto;

import com.jguru.vertexai.domain.dto.AuthenticationConfig;

/**
 * Request object for content generation.
 */
public class GenerationRequest {
  private final AuthenticationConfig authenticationConfig;
  private final String modelName;
  private final String text;

  public GenerationRequest(AuthenticationConfig authenticationConfig, String modelName, String text) {
    this.authenticationConfig = authenticationConfig;
    this.modelName = modelName;
    this.text = text;
  }

  public AuthenticationConfig getAuthenticationConfig() {
    return authenticationConfig;
  }

  public String getModelName() {
    return modelName;
  }

  public String getText() {
    return text;
  }

  public static class Builder {
    private AuthenticationConfig authenticationConfig;
    private String modelName;
    private String text;

    public Builder withAuthenticationConfig(AuthenticationConfig authenticationConfig) {
      this.authenticationConfig = authenticationConfig;
      return this;
    }

    public Builder withModelName(String modelName) {
      this.modelName = modelName;
      return this;
    }

    public Builder withText(String text) {
      this.text = text;
      return this;
    }

    public GenerationRequest build() {
      return new GenerationRequest(authenticationConfig, modelName, text);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
