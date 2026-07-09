package com.jguru.vertexai.domain.dto;

/**
 * Configuration for authentication with Vertex AI.
 */
public class AuthenticationConfig {
  private final AuthenticationType type;
  private final String apiKey;
  private final String projectId;
  private final String location;
  private final String saKeyFile;
  private final boolean skipModelRegionOverride;

  public AuthenticationConfig(AuthenticationType type, String apiKey, String projectId, String location, String saKeyFile) {
    this(type, apiKey, projectId, location, saKeyFile, false);
  }

  public AuthenticationConfig(AuthenticationType type, String apiKey, String projectId, String location, String saKeyFile,
      boolean skipModelRegionOverride) {
    this.type = type;
    this.apiKey = apiKey;
    this.projectId = projectId;
    this.location = location;
    this.saKeyFile = saKeyFile;
    this.skipModelRegionOverride = skipModelRegionOverride;
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

  /**
   * Whether a model's configured {@code .region} override should be ignored in favor of the explicit {@link #getLocation()}.
   *
   * <p>
   * Used by per-region availability scans, which set {@link #getLocation()} to the specific region under test and must not have that
   * silently redirected to a model-pinned region.
   * </p>
   */
  public boolean isSkipModelRegionOverride() {
    return skipModelRegionOverride;
  }

  public static class Builder {
    private AuthenticationType type;
    private String apiKey;
    private String projectId;
    private String location;
    private String saKeyFile;
    private boolean skipModelRegionOverride;

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

    public Builder withSkipModelRegionOverride(boolean skipModelRegionOverride) {
      this.skipModelRegionOverride = skipModelRegionOverride;
      return this;
    }

    public AuthenticationConfig build() {
      if (type == null) {
        throw new IllegalStateException("Authentication type must be provided");
      }

      switch (type) {
        case API_KEY :
          requireNonBlank(apiKey, "apiKey");
          break;
        case SERVICE_ACCOUNT_ADC :
          requireNonBlank(location, "location");
          break;
        case SERVICE_ACCOUNT_EXPLICIT_KEY :
          requireNonBlank(saKeyFile, "saKeyFile");
          // projectId is optional if it can be extracted from saKeyFile
          requireNonBlank(location, "location");
          break;
        default :
          throw new IllegalStateException("Unsupported authentication type: " + type);
      }

      return new AuthenticationConfig(type, apiKey, projectId, location, saKeyFile, skipModelRegionOverride);
    }

    private static void requireNonBlank(String value, String fieldName) {
      if (value == null || value.isBlank()) {
        throw new IllegalArgumentException(fieldName + " must be provided");
      }
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
