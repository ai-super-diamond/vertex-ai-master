package com.jguru.vertexai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jguru.vertexai.service.dto.AuthenticationConfig;
import com.jguru.vertexai.service.dto.AuthenticationType;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthenticationConfigFactoryTest {

  private AuthenticationConfigFactory factory;
  private RegionProvider mockRegionProvider;

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
    AuthenticationConfig config = factory.createServiceAccountConfig("project-123", "us-central1", null, false, false, null,
        mockRegionProvider);

    assertThat(config).isNotNull();
    assertThat(config.getType()).isEqualTo(AuthenticationType.SERVICE_ACCOUNT_ADC);
    assertThat(config.getProjectId()).isEqualTo("project-123");
    assertThat(config.getLocation()).isEqualTo("us-central1");
  }

  @Test
  void shouldCreateServiceAccountConfigWithKeyFile() {
    AuthenticationConfig config = factory.createServiceAccountConfig("project-123", "us-central1", "/path/to/key.json", false, false, null,
        mockRegionProvider);

    assertThat(config).isNotNull();
    assertThat(config.getType()).isEqualTo(AuthenticationType.SERVICE_ACCOUNT_EXPLICIT_KEY);
    assertThat(config.getProjectId()).isEqualTo("project-123");
    assertThat(config.getLocation()).isEqualTo("us-central1");
    assertThat(config.getSaKeyFile()).isEqualTo("/path/to/key.json");
  }

  @Test
  void shouldDefaultLocationForRegionCheckMode() {
    List<String> regions = Arrays.asList("us-east1", "us-west1");
    when(mockRegionProvider.getRegionsForCluster("US")).thenReturn(regions);

    AuthenticationConfig config = factory.createServiceAccountConfig("project-123", null, null, true, false, "US", mockRegionProvider);

    assertThat(config).isNotNull();
    assertThat(config.getLocation()).isEqualTo("us-east1");
  }

  @Test
  void shouldDefaultLocationForWorldwideMode() {
    AuthenticationConfig config = factory.createServiceAccountConfig("project-123", null, null, false, true, null, mockRegionProvider);

    assertThat(config).isNotNull();
    assertThat(config.getLocation()).isEqualTo("us-central1");
  }

  @Test
  void shouldThrowExceptionWhenLocationRequiredInNormalMode() {
    assertThatThrownBy(() -> factory.createServiceAccountConfig("project-123", null, null, false, false, null, mockRegionProvider))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Service account location is required in normal mode");
  }

  @Test
  void shouldHandleEmptyRegionsListInCheckAllRegionsMode() {
    when(mockRegionProvider.getRegionsForCluster("UNKNOWN")).thenReturn(null);

    AuthenticationConfig config = factory.createServiceAccountConfig("project-123", null, null, true, false, "UNKNOWN", mockRegionProvider);

    assertThat(config).isNotNull();
    assertThat(config.getLocation()).isEqualTo("us-central1");
  }

  @Test
  void shouldPreferExplicitLocationOverDefaults() {
    when(mockRegionProvider.getRegionsForCluster("US")).thenReturn(Arrays.asList("us-east1", "us-west1"));

    AuthenticationConfig config = factory.createServiceAccountConfig("project-123", "europe-west1", null, true, false, "US",
        mockRegionProvider);

    assertThat(config).isNotNull();
    assertThat(config.getLocation()).isEqualTo("europe-west1");
  }
}
