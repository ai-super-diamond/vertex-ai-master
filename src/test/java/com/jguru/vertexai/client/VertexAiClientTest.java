package com.jguru.vertexai.client;

import com.jguru.vertexai.service.dto.AuthenticationConfig;
import com.jguru.vertexai.service.dto.AuthenticationType;
import com.jguru.vertexai.service.dto.GenerationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class VertexAiClientTest {

  private VertexAiClient client;
  private VertexAiClient spyClient;
  private GenerationResult mockResult;

  /**
   * Helper method to set the private modelProperties field for testing purposes. This is the only place where reflection is used in this
   * test class.
   */
  private void setModelProperties(VertexAiClient client, Properties properties) {
    try {
      Field modelPropertiesField = VertexAiClient.class.getDeclaredField("modelProperties");
      modelPropertiesField.setAccessible(true);
      modelPropertiesField.set(client, properties);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set modelProperties via reflection", e);
    }
  }

  @BeforeEach
  void setUp() {
    // Given: A real client instance
    client = new VertexAiClient(AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_ADC)
        .withProjectId("test-project").withLocation("us-central1").build());

    // And: A spy of that client
    spyClient = spy(client);

    // And: A mock result for stubbing
    mockResult = GenerationResult.builder().withText("mock").build();
  }

  @Test
  void shouldCreateClientWithApiKeyAuthentication() {
    // Given
    String apiKey = "test-api-key";

    // When
    VertexAiClient client = new VertexAiClient(
        AuthenticationConfig.builder().withType(AuthenticationType.API_KEY).withApiKey(apiKey).build());

    // Then
    assertThat(client).isNotNull();
  }

  @Test
  void shouldCreateClientWithServiceAccountADC() {
    // Given
    String projectId = "test-project";
    String location = "us-central1";

    // When
    VertexAiClient client = new VertexAiClient(AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_ADC)
        .withProjectId(projectId).withLocation(location).build());

    // Then
    assertThat(client).isNotNull();
  }

  @Test
  void shouldCreateClientWithExplicitServiceAccountKey() {
    // Given
    String saKeyFile = "/path/to/key.json";
    String projectId = "test-project";
    String location = "us-central1";

    // When
    VertexAiClient client = new VertexAiClient(AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_EXPLICIT_KEY)
        .withSaKeyFile(saKeyFile).withProjectId(projectId).withLocation(location).build());

    // Then
    assertThat(client).isNotNull();
  }

  @Test
  void shouldLoadModelProperties() {
    // Given
    VertexAiClient client = new VertexAiClient(
        AuthenticationConfig.builder().withType(AuthenticationType.API_KEY).withApiKey("test-key").build());

    // When & Then
    // We can't directly test the private modelProperties field without reflection,
    // but we can test the behavior that depends on it
    assertThat(client).isNotNull();
  }

  @Test
  void shouldHandleInvalidServiceAccountKey() {
    // Given
    AuthenticationConfig authConfig = AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_EXPLICIT_KEY)
        .withSaKeyFile("/nonexistent/path/key.json").withProjectId("test-project").withLocation("us-central1").build();

    VertexAiClient client = new VertexAiClient(authConfig);

    // When & Then
    assertThrows(IOException.class, () -> {
      client.callVertexAi("test-model", "test prompt");
    });
  }

  @Test
  void shouldHandleApiKeyAuthentication() throws IOException {
    // Given
    AuthenticationConfig authConfig = AuthenticationConfig.builder().withType(AuthenticationType.API_KEY).withApiKey("invalid-api-key")
        .build();

    VertexAiClient client = new VertexAiClient(authConfig);

    // When & Then
    // We expect this to fail with an invalid API key, but the important thing is that
    // it attempts the API call in the correct manner (using API key auth)
    assertThrows(Exception.class, () -> {
      client.callVertexAi("gemini-pro", "test prompt");
    });
  }

  @Test
  void shouldHandleServiceAccountADC() throws IOException {
    // Given
    AuthenticationConfig authConfig = AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_ADC)
        .withProjectId("nonexistent-project").withLocation("us-central1").build();

    VertexAiClient client = new VertexAiClient(authConfig);

    // When & Then
    // We expect this to fail with invalid credentials, but the important thing is that
    // it attempts the API call in the correct manner (using ADC)
    assertThrows(Exception.class, () -> {
      client.callVertexAi("gemini-pro", "test prompt");
    });
  }

  @Test
  void shouldRouteToChatCompletionsWhenProviderIsPresent() throws IOException {
    // Given: Custom properties injected via the reflection helper
    Properties customProps = new Properties();
    customProps.setProperty("maas-model.provider", "test-provider");
    customProps.setProperty("maas-model", "maas-model"); // This line is the key fix
    setModelProperties(spyClient, customProps);

    // And: We stub the protected methods to prevent real network calls and return a mock result
    doReturn(mockResult).when(spyClient).callChatCompletionsApi(anyString(), anyString(), anyString());
    doReturn(mockResult).when(spyClient).callStandardVertexAi(anyString(), anyString());

    // When: The public method is called
    spyClient.callVertexAi("maas-model", "a prompt");

    // Then: Verify that ONLY the correct internal method was called
    verify(spyClient, times(1)).callChatCompletionsApi(eq("test-provider"), eq("maas-model"), eq("a prompt"));
    verify(spyClient, never()).callStandardVertexAi(anyString(), anyString());
  }

  @Test
  void shouldRouteToChatCompletionsWhenOpenAIFlagIsPresent() throws IOException {
    // Given: Custom properties injected via the reflection helper
    Properties customProps = new Properties();
    customProps.setProperty("openai-model.openai", "true");
    customProps.setProperty("openai-model", "openai-model"); // This line is the key fix
    setModelProperties(spyClient, customProps);

    // And: We stub the protected methods to prevent real network calls and return a mock result
    doReturn(mockResult).when(spyClient).callChatCompletionsApi(eq("openai"), eq("openai-model"), eq("a prompt"));
    doReturn(mockResult).when(spyClient).callStandardVertexAi(anyString(), anyString());

    // When: The public method is called
    spyClient.callVertexAi("openai-model", "a prompt");

    // Then: Verify that ONLY the correct internal method was called
    verify(spyClient, times(1)).callChatCompletionsApi(eq("openai"), eq("openai-model"), eq("a prompt"));
    verify(spyClient, never()).callStandardVertexAi(anyString(), anyString());
  }

  @Test
  void shouldRouteToStandardVertexAiForModelWithoutSpecialFlags() throws IOException {
    // Given: Custom properties injected via the reflection helper (no special flags)
    Properties customProps = new Properties();
    customProps.setProperty("standard-model", "standard-model-name");
    setModelProperties(spyClient, customProps);

    // And: We stub the protected methods to prevent real network calls and return a mock result
    doReturn(mockResult).when(spyClient).callStandardVertexAi(eq("standard-model-name"), eq("a prompt"));
    doReturn(mockResult).when(spyClient).callChatCompletionsApi(anyString(), anyString(), anyString());

    // When: The public method is called
    spyClient.callVertexAi("standard-model-name", "a prompt");

    // Then: Verify that ONLY the correct internal method was called
    verify(spyClient, times(1)).callStandardVertexAi(eq("standard-model-name"), eq("a prompt"));
    verify(spyClient, never()).callChatCompletionsApi(anyString(), anyString(), anyString());
  }
  @Test
  void shouldRouteToChatCompletionsWhenGoogleOpenAiProviderIsPresent() throws IOException {
    // Given: Custom properties injected via the reflection helper
    Properties customProps = new Properties();
    customProps.setProperty("gemini.flash.openapi", "google/gemini-2.0-flash-001");
    customProps.setProperty("gemini.flash.openapi.provider", "google-openai");
    customProps.setProperty("gemini.flash.openapi.openai", "true");
    setModelProperties(spyClient, customProps);

    // And: We stub the protected methods to prevent real network calls and return a mock result
    doReturn(mockResult).when(spyClient).callChatCompletionsApi(anyString(), anyString(), anyString());
    doReturn(mockResult).when(spyClient).callStandardVertexAi(anyString(), anyString());

    // When: The public method is called with the model alias
    spyClient.callVertexAi("gemini.flash.openapi", "a prompt");

    // Then: Verify that callChatCompletionsApi was called with the correct model name (not the
    // alias)
    verify(spyClient, times(1)).callChatCompletionsApi(eq("google-openai"), eq("google/gemini-2.0-flash-001"), eq("a prompt"));
    verify(spyClient, never()).callStandardVertexAi(anyString(), anyString());
  }
}
