package com.jguru.vertexai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jguru.vertexai.domain.dto.AuthenticationConfig;
import com.jguru.vertexai.domain.dto.AuthenticationType;
import com.jguru.vertexai.domain.dto.GenerationResult;
import com.jguru.vertexai.domain.exception.ApiCallException;
import com.jguru.vertexai.service.dto.GenerationRequest;
import com.jguru.vertexai.service.dto.RegionCheckRequest;
import com.jguru.vertexai.service.dto.RegionCheckResult;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class VertexAiServiceImplTest {

  @Test
  void shouldTestEachRegionIndependentlyEvenWhenModelHasRegionOverride() throws Exception {
    // Given: a model pinned to "us-east5" via its .region property, and a scan
    // requesting two different regions.
    Properties modelProperties = new Properties();
    modelProperties.setProperty("pinned-model", "pinned-model-name");
    modelProperties.setProperty("pinned-model.region", "us-east5");

    ModelClientFactory clientFactory = mock(ModelClientFactory.class);
    ModelClient client = mock(ModelClient.class);
    when(clientFactory.createClient(any())).thenReturn(client);
    when(client.callVertexAi(any(), any())).thenReturn("ok");

    VertexAiServiceImpl service = new VertexAiServiceImpl(mock(RegionProvider.class), clientFactory, modelProperties);

    AuthenticationConfig authConfig = AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_ADC)
        .withProjectId("test-project").withLocation("us-central1").build();

    RegionCheckRequest request = RegionCheckRequest.builder().withAuthenticationConfig(authConfig).withModelName("pinned-model")
        .withRegions(List.of("europe-west1", "asia-east1")).withTestPrompt("hi").build();

    // When
    RegionCheckResult result = service.checkRegionAvailability(request);

    // Then: each region reports its own result, unaffected by the model's pinned region ...
    assertThat(result.getRegionResults()).containsOnlyKeys("europe-west1", "asia-east1");
    assertThat(result.getSuccessCount()).isEqualTo(2);

    // ... and the client factory received a distinct, non-overridden location per
    // region, with the override-skip flag set so VertexAiClient won't redirect it.
    ArgumentCaptor<AuthenticationConfig> configCaptor = ArgumentCaptor.forClass(AuthenticationConfig.class);
    verify(clientFactory, org.mockito.Mockito.times(2)).createClient(configCaptor.capture());

    List<AuthenticationConfig> capturedConfigs = configCaptor.getAllValues();
    assertThat(capturedConfigs).extracting(AuthenticationConfig::getLocation).containsExactlyInAnyOrder("europe-west1", "asia-east1");
    assertThat(capturedConfigs).allMatch(AuthenticationConfig::isSkipModelRegionOverride);
  }

  @Test
  void shouldContinueScanningOtherRegionsWhenAnExceptionHasNoMessage() throws Exception {
    // Given: the client throws with a null message (e.g. a bare
    // NullPointerException from client construction) for the first region, then
    // succeeds for the second.
    ModelClientFactory clientFactory = mock(ModelClientFactory.class);
    ModelClient client = mock(ModelClient.class);
    when(clientFactory.createClient(any())).thenReturn(client);
    when(client.callVertexAi(any(), any())).thenThrow(new RuntimeException((String) null)).thenReturn("ok");

    VertexAiServiceImpl service = new VertexAiServiceImpl(mock(RegionProvider.class), clientFactory, new Properties());

    AuthenticationConfig authConfig = AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_ADC)
        .withProjectId("test-project").withLocation("us-central1").build();

    RegionCheckRequest request = RegionCheckRequest.builder().withAuthenticationConfig(authConfig).withModelName("some-model")
        .withRegions(List.of("europe-west1", "asia-east1")).withTestPrompt("hi").build();

    // When: the scan runs
    RegionCheckResult result = service.checkRegionAvailability(request);

    // Then: it does not blow up with an NPE from the error formatter, and both
    // regions get a recorded result -- one failure, one success.
    assertThat(result.getRegionResults()).hasSize(2);
    assertThat(result.getSuccessCount()).isEqualTo(1);
    assertThat(result.getFailCount()).isEqualTo(1);
  }

  @Test
  void shouldReportFailureWhenModelReturnsEmptyResponse() throws Exception {
    // Given: the client returns an empty string instead of throwing.
    ModelClientFactory clientFactory = mock(ModelClientFactory.class);
    ModelClient client = mock(ModelClient.class);
    when(clientFactory.createClient(any())).thenReturn(client);
    when(client.callVertexAi(any(), any())).thenReturn("");

    VertexAiServiceImpl service = new VertexAiServiceImpl(mock(RegionProvider.class), clientFactory, new Properties());

    AuthenticationConfig authConfig = AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_ADC)
        .withProjectId("test-project").withLocation("us-central1").build();
    GenerationRequest request = GenerationRequest.builder().withAuthenticationConfig(authConfig).withModelName("some-model").withText("hi")
        .build();

    // When
    GenerationResult result = service.generateContent(request);

    // Then: it must not be reported as a success with a null/empty body
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getContent()).isNull();
    assertThat(result.getErrorMessage()).isNotBlank();
  }

  @Test
  void shouldUseApiCallExceptionsCategorizedErrorTypeInsteadOfGuessingFromMessage() throws Exception {
    // Given: a rate-limit failure whose message happens to contain "500" -- a
    // naive substring scan over the message would misclassify this as a 500
    // Internal Error instead of the rate limit the client already detected.
    ModelClientFactory clientFactory = mock(ModelClientFactory.class);
    ModelClient client = mock(ModelClient.class);
    when(clientFactory.createClient(any())).thenReturn(client);
    when(client.callVertexAi(any(), any()))
        .thenThrow(new ApiCallException("Quota exceeded: max 500 requests/min", "some-model", ApiCallException.ErrorType.RATE_LIMITED));

    VertexAiServiceImpl service = new VertexAiServiceImpl(mock(RegionProvider.class), clientFactory, new Properties());

    AuthenticationConfig authConfig = AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_ADC)
        .withProjectId("test-project").withLocation("us-central1").build();
    RegionCheckRequest request = RegionCheckRequest.builder().withAuthenticationConfig(authConfig).withModelName("some-model")
        .withRegions(List.of("us-east1")).withTestPrompt("hi").build();

    // When
    RegionCheckResult result = service.checkRegionAvailability(request);

    // Then: the already-categorized rate-limit type wins over the misleading
    // "500" substring in the message.
    assertThat(result.getRegionResults().get("us-east1")).isEqualTo("429 Rate Limited");
  }
}
