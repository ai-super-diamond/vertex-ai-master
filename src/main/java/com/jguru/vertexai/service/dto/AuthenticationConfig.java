package com.jguru.vertexai.service.dto;

/**
 * Configuration for authentication with Vertex AI.
 */
public class AuthenticationConfig {
  private final AuthenticationType type;
  private final String apiKey;
  private final String projectId;
  private final String location;
  private final String saKeyFile;

  public AuthenticationConfig(AuthenticationType type, String apiKey, String projectId,
      String location, String saKeyFile) {
    this.type = type;
    this.apiKey = apiKey;
    this.projectId = projectId;
    this.location = location;
    this.saKeyFile = saKeyFile;
  }

  public AuthenticationType getType() {
    return type;
  }

  public String getApiKey() {
    return apiKey;
  }

  public String getProjectId() {
    return projectId;
  }

  public String getLocation() {
    return location;
  }

  public String getSaKeyFile() {
    return saKeyFile;
  }

  public static class Builder {
    private AuthenticationType type;
    private String apiKey;
    private String projectId;
    private String location;
    private String saKeyFile;

    public Builder withType(AuthenticationType type) {
      this.type = type;
      return this;
    }

    public Builder withApiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    public Builder withProjectId(String projectId) {
      this.projectId = projectId;
      return this;
    }

    public Builder withLocation(String location) {
      this.location = location;
      return this;
    }

    public Builder withSaKeyFile(String saKeyFile) {
      this.saKeyFile = saKeyFile;
      return this;
    }

    public AuthenticationConfig build() {
      return new AuthenticationConfig(type, apiKey, projectId, location, saKeyFile);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
