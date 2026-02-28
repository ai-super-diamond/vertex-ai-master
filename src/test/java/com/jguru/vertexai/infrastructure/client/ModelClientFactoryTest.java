package com.jguru.vertexai.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jguru.vertexai.service.ModelClient;
import com.jguru.vertexai.service.ModelClientFactory;
import com.jguru.vertexai.domain.dto.AuthenticationConfig;
import com.jguru.vertexai.domain.dto.AuthenticationType;
import com.jguru.vertexai.domain.dto.GenerationResult;
import com.jguru.vertexai.domain.exception.ApiCallException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModelClientFactoryTest {

  private ModelClientFactory factory;
  private AuthenticationConfig authConfig;

  @BeforeEach
  void setUp() {
    factory = new VertexAiClientFactory();
    authConfig = AuthenticationConfig.builder().withType(AuthenticationType.API_KEY).withApiKey("test-key").build();
  }

  @Test
  void shouldCreateClientWithValidAuthConfig() {
    ModelClient client = factory.createClient(authConfig);
    assertThat(client).isNotNull();
    assertThat(client).isInstanceOf(VertexAiClient.class);
  }

  @Test
  void shouldCreateMockClientForTesting() throws IOException, ApiCallException {
    ModelClient mockClient = mock(ModelClient.class);
    when(mockClient.callVertexAi(any(), any())).thenReturn("Mocked response");

    ModelClientFactory mockFactory = authConfig -> mockClient;

    ModelClient client = mockFactory.createClient(authConfig);
    String response = client.callVertexAi("gemini-pro", "Hello");

    assertThat(response).isEqualTo("Mocked response");
    verify(mockClient).callVertexAi("gemini-pro", "Hello");
  }

  @Test
  void shouldSupportDependencyInjectionPattern() {
    ModelClientFactory injectedFactory = new VertexAiClientFactory();

    ModelClient client = injectedFactory.createClient(authConfig);

    assertThat(client).isNotNull();
  }

  @Test
  void shouldCreateIndependentClientInstances() {
    ModelClient client1 = factory.createClient(authConfig);
    ModelClient client2 = factory.createClient(authConfig);

    assertThat(client1).isNotNull();
    assertThat(client2).isNotNull();
    assertThat(client1).isNotSameAs(client2);
  }

  @Test
  void mockFactoryShouldEnableServiceLayerTesting() throws IOException, ApiCallException {
    ModelClient mockClient = mock(ModelClient.class);
    GenerationResult mockResult = GenerationResult.success("Test response");
    when(mockClient.callStandardVertexAi(any(), any())).thenReturn(mockResult);

    ModelClientFactory testFactory = authConfig -> mockClient;

    ModelClient client = testFactory.createClient(authConfig);
    GenerationResult result = client.callStandardVertexAi("test-model", "test prompt");

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getContent()).isEqualTo("Test response");
  }
}
