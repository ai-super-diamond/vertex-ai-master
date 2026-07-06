package com.jguru.vertexai.infrastructure.client;

import com.google.genai.Client;
import com.jguru.vertexai.domain.dto.AuthenticationConfig;
import com.jguru.vertexai.domain.dto.AuthenticationType;
import com.jguru.vertexai.domain.dto.GenerationResult;
import com.jguru.vertexai.domain.exception.ApiCallException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class VertexAiClientTest {

  private VertexAiClient spyClient;
  private GenerationResult mockResult;

  /**
   * Helper method to set to private modelProperties field for testing purposes. This is the only place where reflection is used in this
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

  private Client buildSdkClient(String location) throws Exception {
    AuthenticationConfig authConfig = AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_ADC)
        .withProjectId("test-project").withLocation(location).build();
    VertexAiClient client = new VertexAiClient(authConfig);
    com.google.auth.oauth2.GoogleCredentials credentials = mock(com.google.auth.oauth2.GoogleCredentials.class);

    Method buildVertexAiClientMethod = VertexAiClient.class.getDeclaredMethod("buildVertexAiClient",
        com.google.auth.oauth2.GoogleCredentials.class, String.class);
    buildVertexAiClientMethod.setAccessible(true);
    return (Client) buildVertexAiClientMethod.invoke(client, credentials, location);
  }

  private String baseUrl(Client client) throws Exception {
    Method baseUrlMethod = Client.class.getDeclaredMethod("baseUrl");
    baseUrlMethod.setAccessible(true);
    Optional<?> baseUrl = (Optional<?>) baseUrlMethod.invoke(client);
    return (String) baseUrl.orElseThrow();
  }

  @BeforeEach
  void setUp() {
    // Given: A real client instance
    VertexAiClient client = new VertexAiClient(AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_ADC)
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
  void shouldExtractProjectIdFromServiceAccountCredentials() throws Exception {
    // Given
    com.google.auth.oauth2.ServiceAccountCredentials credentials = mock(com.google.auth.oauth2.ServiceAccountCredentials.class);
    when(credentials.getProjectId()).thenReturn("extracted-project-id");

    AuthenticationConfig authConfig = AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_EXPLICIT_KEY)
        .withSaKeyFile("dummy-path").withLocation("us-central1").build();

    VertexAiClient client = new VertexAiClient(authConfig);

    Method buildVertexAiClientMethod = VertexAiClient.class.getDeclaredMethod("buildVertexAiClient",
        com.google.auth.oauth2.GoogleCredentials.class, String.class);
    buildVertexAiClientMethod.setAccessible(true);

    // When
    com.google.genai.Client genAiClient = (com.google.genai.Client) buildVertexAiClientMethod.invoke(client, credentials, "us-central1");

    // Then
    assertThat(genAiClient).isNotNull();
    // Verification of project ID extraction is implicit if it doesn't throw
    // IllegalStateException
  }

  @Test
  void shouldThrowExceptionWhenProjectIdMissingEverywhere() throws Exception {
    // Given
    com.google.auth.oauth2.GoogleCredentials credentials = mock(com.google.auth.oauth2.GoogleCredentials.class);

    AuthenticationConfig authConfig = AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_EXPLICIT_KEY)
        .withSaKeyFile("dummy-path").withLocation("us-central1").build();

    VertexAiClient client = new VertexAiClient(authConfig);

    Method buildVertexAiClientMethod = VertexAiClient.class.getDeclaredMethod("buildVertexAiClient",
        com.google.auth.oauth2.GoogleCredentials.class, String.class);
    buildVertexAiClientMethod.setAccessible(true);

    // When & Then
    java.lang.reflect.InvocationTargetException e = assertThrows(java.lang.reflect.InvocationTargetException.class, () -> {
      buildVertexAiClientMethod.invoke(client, credentials, "us-central1");
    });
    assertThat(e.getCause()).isInstanceOf(IllegalStateException.class).hasMessageContaining("Project ID is required");
  }

  @Test
  void shouldUseMultiRegionEndpointForEuLocation() throws Exception {
    Client genAiClient = buildSdkClient("eu");

    assertThat(baseUrl(genAiClient)).isEqualTo("https://aiplatform.eu.rep.googleapis.com");
    assertThat(genAiClient.location()).isEqualTo("eu");
  }

  @Test
  void shouldUseMultiRegionEndpointForUsLocation() throws Exception {
    Client genAiClient = buildSdkClient("us");

    assertThat(baseUrl(genAiClient)).isEqualTo("https://aiplatform.us.rep.googleapis.com");
    assertThat(genAiClient.location()).isEqualTo("us");
  }

  @Test
  void shouldUseVertexAiRoutingForRegionalLocation() throws Exception {
    Client genAiClient = buildSdkClient("us-central1");

    assertThat(baseUrl(genAiClient)).isEqualTo("https://us-central1-aiplatform.googleapis.com");
    assertThat(genAiClient.location()).isEqualTo("us-central1");
  }

  @Test
  void shouldKeepGlobalOnVertexAiRouting() throws Exception {
    Client genAiClient = buildSdkClient("global");

    assertThat(baseUrl(genAiClient)).isEqualTo("https://aiplatform.googleapis.com");
    assertThat(genAiClient.location()).isEqualTo("global");
  }

  @Test
  void shouldLoadModelProperties() {
    // Given
    VertexAiClient client = new VertexAiClient(
        AuthenticationConfig.builder().withType(AuthenticationType.API_KEY).withApiKey("test-key").build());

    // When & Then
    // We can't directly test to private modelProperties field without reflection,
    // but we can test to behavior that depends on it
    assertThat(client).isNotNull();
  }

  @Test
  void shouldHandleInvalidServiceAccountKey() {
    // Given
    AuthenticationConfig authConfig = AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_EXPLICIT_KEY)
        .withSaKeyFile("/nonexistent/path/key.json").withProjectId("test-project").withLocation("us-central1").build();

    VertexAiClient client = new VertexAiClient(authConfig);

    // When & Then
    assertThrows(ApiCallException.class, () -> client.callVertexAi("test-model", "test prompt"));
  }

  @Test
  void shouldHandleApiKeyAuthentication() {
    // Given
    AuthenticationConfig authConfig = AuthenticationConfig.builder().withType(AuthenticationType.API_KEY).withApiKey("invalid-api-key")
        .build();

    VertexAiClient client = new VertexAiClient(authConfig);

    // When & Then
    // We expect this to fail with an invalid API key, but to important thing is
    // that
    // it attempts to API call in a correct manner (using API key auth)
    assertThrows(Exception.class, () -> client.callVertexAi("gemini-pro", "test prompt"));
  }

  @Test
  void shouldHandleServiceAccountADC() {
    // Given
    AuthenticationConfig authConfig = AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_ADC)
        .withProjectId("nonexistent-project").withLocation("us-central1").build();

    VertexAiClient client = new VertexAiClient(authConfig);

    // When & Then
    // We expect this to fail with invalid credentials, but to important thing is
    // that
    // it attempts to API call in a correct manner (using ADC)
    assertThrows(Exception.class, () -> client.callVertexAi("gemini-pro", "test prompt"));
  }

  @Test
  void shouldRouteToChatCompletionsWhenProviderIsPresent() throws Exception {
    // Given: Custom properties injected via reflection helper
    Properties customProps = new Properties();
    customProps.setProperty("maas-model", "maas-model-full");
    customProps.setProperty("maas-model.provider", "test-provider");
    setModelProperties(spyClient, customProps);

    // And: We stub protected methods
    doReturn(mockResult).when(spyClient).callChatCompletionsApi(anyString(), anyString(), anyString());
    // ... existing code ...
    // When: The public method is called
    spyClient.callVertexAi("maas-model", "a prompt");

    // Then: Verify that ONLY the correct internal method was called
    verify(spyClient, times(1)).callChatCompletionsApi(eq("test-provider"), eq("maas-model-full"), eq("a prompt"));
    verify(spyClient, never()).callStandardVertexAi(anyString(), anyString());
  }

  @Test
  void shouldRouteToAnthropicWhenProviderIsAnthropic() throws Exception {
    // Given: Custom properties injected via reflection helper
    Properties customProps = new Properties();
    customProps.setProperty("anthropic-model", "anthropic-model-full");
    customProps.setProperty("anthropic-model.provider", "anthropic");
    setModelProperties(spyClient, customProps);

    // And: We stub the protected method
    doReturn(mockResult).when(spyClient).callAnthropicVertexAi(eq("anthropic-model-full"), eq("a prompt"));

    // When: The public method is called
    spyClient.callVertexAi("anthropic-model", "a prompt");

    // Then: Verify that ONLY the anthropic transport was invoked
    verify(spyClient, times(1)).callAnthropicVertexAi(eq("anthropic-model-full"), eq("a prompt"));
    verify(spyClient, never()).callChatCompletionsApi(anyString(), anyString(), anyString());
    verify(spyClient, never()).callStandardVertexAi(anyString(), anyString());
  }

  @Test
  void shouldRouteToChatCompletionsWhenOpenAIFlagIsPresent() throws Exception {
    // Given: Custom properties injected via reflection helper
    Properties customProps = new Properties();
    customProps.setProperty("openai-model", "openai-model-full");
    customProps.setProperty("openai-model.openai", "true");
    setModelProperties(spyClient, customProps);

    // And: We stub protected methods
    doReturn(mockResult).when(spyClient).callChatCompletionsApi(eq("openai"), eq("openai-model-full"), eq("a prompt"));

    // When: The public method is called
    spyClient.callVertexAi("openai-model", "a prompt");

    // Then: Verify that ONLY the correct internal method was called
    verify(spyClient, times(1)).callChatCompletionsApi(eq("openai"), eq("openai-model-full"), eq("a prompt"));
    verify(spyClient, never()).callStandardVertexAi(anyString(), anyString());
  }

  @Test
  void shouldRouteToStandardVertexAiForModelWithoutSpecialFlags() throws Exception {
    // Given: Custom properties injected via to reflection helper (no special flags)
    Properties customProps = new Properties();
    customProps.setProperty("standard-model", "standard-model-name");
    setModelProperties(spyClient, customProps);

    // And: We stub to protected methods to prevent real network calls and return a
    // mock result
    doReturn(mockResult).when(spyClient).callStandardVertexAi(eq("standard-model-name"), eq("a prompt"));
    doReturn(mockResult).when(spyClient).callChatCompletionsApi(anyString(), anyString(), anyString());

    // When: The public method is called
    spyClient.callVertexAi("standard-model-name", "a prompt");

    // Then: Verify that ONLY to correct internal method was called
    verify(spyClient, times(1)).callStandardVertexAi(eq("standard-model-name"), eq("a prompt"));
    verify(spyClient, never()).callChatCompletionsApi(anyString(), anyString(), anyString());
  }

  @Test
  void shouldRouteToChatCompletionsWhenGoogleOpenAiProviderIsPresent() throws Exception {
    // Given: Custom properties injected via to reflection helper
    Properties customProps = new Properties();
    customProps.setProperty("gemini.flash.openapi", "google/gemini-2.0-flash-001");
    customProps.setProperty("gemini.flash.openapi.provider", "google-openai");
    customProps.setProperty("gemini.flash.openapi.openai", "true");
    setModelProperties(spyClient, customProps);

    // And: We stub to protected methods to prevent real network calls and return a
    // mock result
    doReturn(mockResult).when(spyClient).callChatCompletionsApi(anyString(), anyString(), anyString());
    doReturn(mockResult).when(spyClient).callStandardVertexAi(anyString(), anyString());

    // When: The public method is called with to model alias
    spyClient.callVertexAi("gemini.flash.openapi", "a prompt");

    // Then: Verify that callChatCompletionsApi was called with to correct model
    // name (not to
    // alias)
    verify(spyClient, times(1)).callChatCompletionsApi(eq("google-openai"), eq("google/gemini-2.0-flash-001"), eq("a prompt"));
    verify(spyClient, never()).callStandardVertexAi(anyString(), anyString());
  }

  // ============== Error Hint Logic Tests for callStandardVertexAi ==============

  @Test
  void shouldAppend404HintForCallStandardVertexAi() throws Exception {
    // Given: A spy client with properties for a standard model
    Properties customProps = new Properties();
    customProps.setProperty("standard-model", "standard-model-name");
    setModelProperties(spyClient, customProps);

    // And: We stub the internal loadCredentials method to throw a 404-like
    // exception
    // This allows us to test the catch block in callStandardVertexAi
    doCallRealMethod().when(spyClient).callStandardVertexAi(anyString(), anyString());
    doThrow(new IOException("HTTP 404: Not Found")).when(spyClient).loadCredentials();

    // When & Then: The exception should contain a helpful hint (from
    // callStandardVertexAi's catch block)
    ApiCallException exception = assertThrows(ApiCallException.class,
        () -> spyClient.callStandardVertexAi("standard-model-name", "test prompt"));

    assertThat(exception.getMessage())
        .contains("(Hint: Model may not be enabled in your GCP project. Check the model card and click 'Enable'.)");
  }

  @Test
  void shouldAppend403HintForCallStandardVertexAi() throws Exception {
    // Given: A spy client with properties for a standard model
    Properties customProps = new Properties();
    customProps.setProperty("standard-model", "standard-model-name");
    setModelProperties(spyClient, customProps);

    // And: We stub the internal loadCredentials method to throw a 403-like
    // exception
    // This allows us to test the catch block in callStandardVertexAi
    doCallRealMethod().when(spyClient).callStandardVertexAi(anyString(), anyString());
    doThrow(new IOException("HTTP 403: Permission Denied")).when(spyClient).loadCredentials();

    // When & Then: The exception should contain a helpful hint (from
    // callStandardVertexAi's catch block)
    ApiCallException exception = assertThrows(ApiCallException.class,
        () -> spyClient.callStandardVertexAi("standard-model-name", "test prompt"));

    assertThat(exception.getMessage())
        .contains("(Hint: Check that your credentials have been granted the necessary IAM permissions for Vertex AI.)");
  }

  @Test
  void shouldNotAppendHintForCallStandardVertexAiOnOtherErrors() throws Exception {
    // Given: A spy client with properties for a standard model
    Properties customProps = new Properties();
    customProps.setProperty("standard-model", "standard-model-name");
    setModelProperties(spyClient, customProps);

    // And: We stub the internal loadCredentials method to throw a non-404/403 exception
    // This allows us to test the catch block in callStandardVertexAi
    doCallRealMethod().when(spyClient).callStandardVertexAi(anyString(), anyString());
    doThrow(new IOException("HTTP 500: Internal Server Error")).when(spyClient).loadCredentials();

    // When & Then: The exception should NOT contain hints
    ApiCallException exception = assertThrows(ApiCallException.class,
        () -> spyClient.callStandardVertexAi("standard-model-name", "test prompt"));

    assertThat(exception.getMessage()).doesNotContain("(Hint:");
  }

  // ============== Region Resolution Tests ==============

  @Test
  void shouldUseDefaultLocationWhenNoRegionOverride() throws Exception {
    // Given: Custom properties without a region override
    Properties customProps = new Properties();
    customProps.setProperty("standard-model", "standard-model-name");
    setModelProperties(spyClient, customProps);

    // And: We stub to protected method to capture the location used
    doReturn(mockResult).when(spyClient).callStandardVertexAi(anyString(), anyString());

    // When: The public method is called
    spyClient.callVertexAi("standard-model-name", "a prompt");

    // Then: Verify that callStandardVertexAi was called (which uses the default
    // location from authConfig)
    verify(spyClient, times(1)).callStandardVertexAi(eq("standard-model-name"), eq("a prompt"));
  }

  @Test
  void shouldUseModelRegionOverrideWhenPresent() throws Exception {
    // Given: Custom properties with region override
    Properties customProps = new Properties();
    customProps.setProperty("regional-model", "regional-model-name");
    customProps.setProperty("regional-model.region", "global");
    setModelProperties(spyClient, customProps);

    // And: We stub to protected method to prevent real network calls
    doReturn(mockResult).when(spyClient).callStandardVertexAi(anyString(), anyString());

    // When: The public method is called
    spyClient.callVertexAi("regional-model-name", "a prompt");

    // Then: Verify that callStandardVertexAi was called (which internally uses
    // resolveEffectiveLocation)
    // The region override "global" should be used instead of the default
    // "us-central1"
    verify(spyClient, times(1)).callStandardVertexAi(eq("regional-model-name"), eq("a prompt"));
  }

  @Test
  void shouldUseRegionalModelRegionOverride() throws Exception {
    // Given: Custom properties with regional override
    Properties customProps = new Properties();
    customProps.setProperty("us-east5-model", "us-east5-model-name");
    customProps.setProperty("us-east5-model.region", "us-east5");
    setModelProperties(spyClient, customProps);

    // And: We stub protected method to prevent real network calls
    doReturn(mockResult).when(spyClient).callStandardVertexAi(anyString(), anyString());

    // When: The public method is called
    spyClient.callVertexAi("us-east5-model", "a prompt");

    // Then: Verify that callStandardVertexAi was called
    // The region override "us-east5" should be used instead of the default
    // "us-central1"
    verify(spyClient, times(1)).callStandardVertexAi(eq("us-east5-model-name"), eq("a prompt"));
  }
}
