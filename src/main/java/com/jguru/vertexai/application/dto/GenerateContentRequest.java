package com.jguru.vertexai.application.dto;

import com.jguru.vertexai.domain.dto.AuthenticationConfig;
import java.util.Objects;

public class GenerateContentRequest {
  private final String modelAlias;
  private final String prompt;
  private final AuthenticationConfig authenticationConfig;

  public GenerateContentRequest(String modelAlias, String prompt, AuthenticationConfig authenticationConfig) {
    if (modelAlias == null || modelAlias.trim().isEmpty()) {
      throw new IllegalArgumentException("Model alias cannot be null or empty");
    }
    if (prompt == null || prompt.trim().isEmpty()) {
      throw new IllegalArgumentException("Prompt cannot be null or empty");
    }
    if (authenticationConfig == null) {
      throw new IllegalArgumentException("Authentication configuration cannot be null");
    }
    this.modelAlias = modelAlias.trim();
    this.prompt = prompt.trim();
    this.authenticationConfig = authenticationConfig;
  }

  public String getModelAlias() {
    return modelAlias;
  }

  public String getPrompt() {
    return prompt;
  }

  public AuthenticationConfig getAuthenticationConfig() {
    return authenticationConfig;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    GenerateContentRequest that = (GenerateContentRequest) o;
    return Objects.equals(modelAlias, that.modelAlias) && Objects.equals(prompt, that.prompt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(modelAlias, prompt);
  }

  @Override
  public String toString() {
    return "GenerateContentRequest{" + "modelAlias='" + modelAlias + '\'' + ", prompt='" + prompt + '\'' + '}';
  }
}
