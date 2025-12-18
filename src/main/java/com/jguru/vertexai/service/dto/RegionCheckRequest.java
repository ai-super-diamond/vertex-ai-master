package com.jguru.vertexai.service.dto;

import java.util.List;

/**
 * Request object for region availability checking.
 */
public class RegionCheckRequest {
  private final AuthenticationConfig authenticationConfig;
  private final String modelName;
  private final String cluster;
  private final String testPrompt;
  private final List<String> regions;
  private final boolean debug;

  public RegionCheckRequest(AuthenticationConfig authenticationConfig, String modelName, String cluster, String testPrompt,
      List<String> regions, boolean debug) {
    this.authenticationConfig = authenticationConfig;
    this.modelName = modelName;
    this.cluster = cluster;
    this.testPrompt = testPrompt;
    this.regions = regions;
    this.debug = debug;
  }

  public AuthenticationConfig getAuthenticationConfig() {
    return authenticationConfig;
  }

  public String getModelName() {
    return modelName;
  }

  public String getCluster() {
    return cluster;
  }

  public String getTestPrompt() {
    return testPrompt;
  }

  public List<String> getRegions() {
    return regions;
  }

  public boolean isDebug() {
    return debug;
  }

  public static class Builder {
    private AuthenticationConfig authenticationConfig;
    private String modelName;
    private String cluster;
    private String testPrompt;
    private List<String> regions;
    private boolean debug;

    public Builder withAuthenticationConfig(AuthenticationConfig authenticationConfig) {
      this.authenticationConfig = authenticationConfig;
      return this;
    }

    public Builder withModelName(String modelName) {
      this.modelName = modelName;
      return this;
    }

    public Builder withCluster(String cluster) {
      this.cluster = cluster;
      return this;
    }

    public Builder withTestPrompt(String testPrompt) {
      this.testPrompt = testPrompt;
      return this;
    }

    public Builder withRegions(List<String> regions) {
      this.regions = regions;
      return this;
    }

    public Builder withDebug(boolean debug) {
      this.debug = debug;
      return this;
    }

    public RegionCheckRequest build() {
      return new RegionCheckRequest(authenticationConfig, modelName, cluster, testPrompt, regions, debug);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
