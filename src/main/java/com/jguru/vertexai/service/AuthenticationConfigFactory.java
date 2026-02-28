package com.jguru.vertexai.service;

import com.jguru.vertexai.domain.dto.AuthenticationConfig;
import com.jguru.vertexai.domain.dto.AuthenticationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AuthenticationConfigFactory {

  private static final Logger logger = LoggerFactory.getLogger(AuthenticationConfigFactory.class);

  public AuthenticationConfig createApiKeyConfig(String apiKey) {
    return AuthenticationConfig.builder().withType(AuthenticationType.API_KEY).withApiKey(apiKey).build();
  }

  public AuthenticationConfig createServiceAccountConfig(String projectId, String location, String saKeyFile, boolean checkAllRegions,
      boolean worldwide, String cluster, RegionProvider regionProvider) {
    boolean hasKeyFile = saKeyFile != null && !saKeyFile.isBlank();

    String baseLocation = resolveLocation(location, checkAllRegions, worldwide, cluster, regionProvider);

    if (baseLocation == null && !checkAllRegions && !worldwide) {
      throw new IllegalArgumentException("Service account location is required in normal mode.");
    }

    AuthenticationConfig.Builder builder = AuthenticationConfig.builder().withProjectId(projectId).withLocation(baseLocation);

    if (hasKeyFile) {
      builder.withType(AuthenticationType.SERVICE_ACCOUNT_EXPLICIT_KEY).withSaKeyFile(saKeyFile);
    } else {
      builder.withType(AuthenticationType.SERVICE_ACCOUNT_ADC);
    }

    return builder.build();
  }

  private String resolveLocation(String location, boolean checkAllRegions, boolean worldwide, String cluster,
      RegionProvider regionProvider) {
    if (location != null && !location.isBlank()) {
      return location;
    }

    if (checkAllRegions) {
      List<String> regionsForCluster = regionProvider.getRegionsForCluster(cluster);
      if (regionsForCluster != null && !regionsForCluster.isEmpty()) {
        logger.debug("Defaulting location to '{}' for region check mode", regionsForCluster.get(0));
        return regionsForCluster.get(0);
      }
      logger.debug("Defaulting location to 'us-central1' for region check mode");
      return "us-central1";
    }

    if (worldwide) {
      logger.debug("Defaulting location to 'us-central1' for worldwide mode");
      return "us-central1";
    }

    return null;
  }
}
