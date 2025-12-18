package com.jguru.vertexai.service.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationConfigTest {

  @Test
  void shouldRequireAuthenticationType() {
    assertThatThrownBy(() -> AuthenticationConfig.builder().build()).isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Authentication type must be provided");
  }

  @Test
  void shouldRequireApiKeyForApiKeyType() {
    assertThatThrownBy(() -> AuthenticationConfig.builder().withType(AuthenticationType.API_KEY).build())
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("apiKey");
  }

  @Test
  void shouldRequireProjectAndLocationForAdc() {
    assertThatThrownBy(() -> AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_ADC).build())
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("projectId");
    assertThatThrownBy(
        () -> AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_ADC).withProjectId("project").build())
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("location");
  }

  @Test
  void shouldRequireAllFieldsForExplicitKey() {
    assertThatThrownBy(() -> AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_EXPLICIT_KEY).build())
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("saKeyFile");
  }

  @Test
  void shouldBuildValidConfigurations() {
    AuthenticationConfig apiKeyConfig = AuthenticationConfig.builder().withType(AuthenticationType.API_KEY).withApiKey("key").build();
    assertThat(apiKeyConfig.getApiKey()).isEqualTo("key");

    AuthenticationConfig adcConfig = AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_ADC)
        .withProjectId("project").withLocation("region").build();
    assertThat(adcConfig.getProjectId()).isEqualTo("project");

    AuthenticationConfig explicitConfig = AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_EXPLICIT_KEY)
        .withProjectId("project").withLocation("region").withSaKeyFile("path/to/key.json").build();
    assertThat(explicitConfig.getSaKeyFile()).isEqualTo("path/to/key.json");
  }
}
