package com.jguru.vertexai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jguru.vertexai.domain.dto.AuthenticationConfig;
import com.jguru.vertexai.domain.dto.AuthenticationType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AuthenticationConfigFactoryTest {

  private AuthenticationConfigFactory factory;
  private RegionProvider mockRegionProvider;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    factory = new AuthenticationConfigFactory();
    mockRegionProvider = mock(RegionProvider.class);
  }

  @Test
  void shouldCreateApiKeyConfig() {
    AuthenticationConfig config = factory.createApiKeyConfig("test-api-key");

    assertThat(config).isNotNull();
    assertThat(config.getType()).isEqualTo(AuthenticationType.API_KEY);
    assertThat(config.getApiKey()).isEqualTo("test-api-key");
  }

  @Test
  void shouldCreateServiceAccountConfigWithExplicitLocation() {
    AuthenticationConfig config = factory.createServiceAccountConfig("us-central1", null, false, false, null, mockRegionProvider);

    assertThat(config).isNotNull();
    assertThat(config.getType()).isEqualTo(AuthenticationType.SERVICE_ACCOUNT_ADC);
    assertThat(config.getProjectId()).isNull();
    assertThat(config.getLocation()).isEqualTo("us-central1");
  }

  @Test
  void shouldCreateServiceAccountConfigWithKeyFile() throws IOException {
    Path keyFile = writeServiceAccountKey("project-123");

    AuthenticationConfig config = factory.createServiceAccountConfig("us-central1", keyFile.toString(), false, false, null,
        mockRegionProvider);

    assertThat(config).isNotNull();
    assertThat(config.getType()).isEqualTo(AuthenticationType.SERVICE_ACCOUNT_EXPLICIT_KEY);
    assertThat(config.getProjectId()).isEqualTo("project-123");
    assertThat(config.getLocation()).isEqualTo("us-central1");
    assertThat(config.getSaKeyFile()).isEqualTo(keyFile.toString());
  }

  @Test
  void shouldDefaultLocationForRegionCheckMode() {
    List<String> regions = Arrays.asList("us-east1", "us-west1");
    when(mockRegionProvider.getRegionsForCluster("US")).thenReturn(regions);

    AuthenticationConfig config = factory.createServiceAccountConfig(null, null, true, false, "US", mockRegionProvider);

    assertThat(config).isNotNull();
    assertThat(config.getLocation()).isEqualTo("us-east1");
  }

  @Test
  void shouldDefaultLocationForWorldwideMode() {
    AuthenticationConfig config = factory.createServiceAccountConfig(null, null, false, true, null, mockRegionProvider);

    assertThat(config).isNotNull();
    assertThat(config.getLocation()).isEqualTo("us-central1");
  }

  @Test
  void shouldThrowExceptionWhenLocationRequiredInNormalMode() {
    assertThatThrownBy(() -> factory.createServiceAccountConfig(null, null, false, false, null, mockRegionProvider))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Service account location is required in normal mode");
  }

  @Test
  void shouldHandleEmptyRegionsListInCheckAllRegionsMode() {
    when(mockRegionProvider.getRegionsForCluster("UNKNOWN")).thenReturn(null);

    AuthenticationConfig config = factory.createServiceAccountConfig(null, null, true, false, "UNKNOWN", mockRegionProvider);

    assertThat(config).isNotNull();
    assertThat(config.getLocation()).isEqualTo("us-central1");
  }

  @Test
  void shouldPreferExplicitLocationOverDefaults() {
    when(mockRegionProvider.getRegionsForCluster("US")).thenReturn(Arrays.asList("us-east1", "us-west1"));

    AuthenticationConfig config = factory.createServiceAccountConfig("europe-west1", null, true, false, "US", mockRegionProvider);

    assertThat(config).isNotNull();
    assertThat(config.getLocation()).isEqualTo("europe-west1");
  }

  @Test
  void shouldCreateAdcConfigWithExplicitLocation() {
    AuthenticationConfig config = factory.createAdcConfig("proj", "europe-west1", false, false, null, mockRegionProvider);

    assertThat(config).isNotNull();
    assertThat(config.getType()).isEqualTo(AuthenticationType.SERVICE_ACCOUNT_ADC);
    assertThat(config.getProjectId()).isEqualTo("proj");
    assertThat(config.getLocation()).isEqualTo("europe-west1");
  }

  @Test
  void shouldThrowExceptionWhenProjectMissingForAdc() {
    assertThatThrownBy(() -> factory.createAdcConfig(null, "europe-west1", false, false, null, mockRegionProvider))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("--project");
  }

  @Test
  void shouldThrowExceptionWhenAdcLocationMissingInNormalMode() {
    assertThatThrownBy(() -> factory.createAdcConfig("proj", null, false, false, null, mockRegionProvider))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("--adc-location");
  }

  @Test
  void shouldDefaultAdcLocationForRegionCheckMode() {
    when(mockRegionProvider.getRegionsForCluster("US")).thenReturn(Arrays.asList("us-east1", "us-west1"));

    AuthenticationConfig config = factory.createAdcConfig("proj", null, true, false, "US", mockRegionProvider);

    assertThat(config).isNotNull();
    assertThat(config.getLocation()).isEqualTo("us-east1");
  }

  @Test
  void shouldDefaultAdcLocationForWorldwideMode() {
    AuthenticationConfig config = factory.createAdcConfig("proj", null, false, true, null, mockRegionProvider);

    assertThat(config).isNotNull();
    assertThat(config.getLocation()).isEqualTo("us-central1");
  }

  @Test
  void shouldRejectKeyFileWithoutProjectId() throws IOException {
    Path keyFile = tempDir.resolve("missing-project.json");
    Files.writeString(keyFile, "{\"type\":\"service_account\"}");

    assertThatThrownBy(() -> factory.createServiceAccountConfig("us-central1", keyFile.toString(), false, false, null, mockRegionProvider))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("project_id");
  }

  private Path writeServiceAccountKey(String projectId) throws IOException {
    Path keyFile = tempDir.resolve("sa-key.json");
    Files.writeString(keyFile, "{\"type\":\"service_account\",\"project_id\":\"" + projectId + "\"}");
    return keyFile;
  }
}
