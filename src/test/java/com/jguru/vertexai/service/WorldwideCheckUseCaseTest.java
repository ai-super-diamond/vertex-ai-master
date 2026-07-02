package com.jguru.vertexai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jguru.vertexai.domain.dto.AuthenticationConfig;
import com.jguru.vertexai.domain.dto.AuthenticationType;
import com.jguru.vertexai.service.dto.RegionCheckRequest;
import com.jguru.vertexai.service.dto.RegionCheckResult;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class WorldwideCheckUseCaseTest {

  private WorldwideCheckUseCase useCase;
  private VertexAiService mockService;
  private AuthenticationConfig authConfig;

  @BeforeEach
  void setUp() {
    mockService = mock(VertexAiService.class);
    useCase = new WorldwideCheckUseCase(mockService);
    authConfig = AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_ADC).withProjectId("test-project")
        .withLocation("us-central1").build();
  }

  @Test
  void shouldExecuteWorldwideCheck() throws Exception {
    when(mockService.getAllRegions()).thenReturn(java.util.Arrays.asList("us-central1", "europe-west1", "asia-east1"));
    when(mockService.checkRegionAvailability(any())).thenReturn(new RegionCheckResult(Map.of("us-central1", "SUCCESS")),
        new RegionCheckResult(Map.of("europe-west1", "SUCCESS")), new RegionCheckResult(Map.of("asia-east1", "404 Not Found")));

    RegionCheckResult result = useCase.execute(authConfig, "gemini-pro", "test prompt", false);

    assertThat(result).isNotNull();
  }

  @Test
  void shouldGenerateReport() {
    Map<String, String> results = new HashMap<>();
    results.put("us-central1", "SUCCESS");
    results.put("europe-west1", "SUCCESS");
    RegionCheckResult result = new RegionCheckResult(results);

    Properties mockProps = new Properties();
    mockProps.setProperty("gemini-pro", "gemini-1.5-pro");

    useCase.generateReport(result, "gemini-pro", "test prompt", "test-project", null, mockProps);

    assertThat(result.getSuccessCount()).isEqualTo(2);
    assertThat(result.getFailCount()).isEqualTo(0);
  }

  @Test
  void shouldHandleReportGenerationWithCustomModelFile() {
    Map<String, String> results = new HashMap<>();
    results.put("us-central1", "SUCCESS");
    RegionCheckResult result = new RegionCheckResult(results);

    Properties mockProps = new Properties();
    mockProps.setProperty("custom-model", "custom-model-name");

    useCase.generateReport(result, "custom-model", "test prompt", "test-project", "custom-models.properties", mockProps);

    assertThat(result.getTotalCount()).isEqualTo(1);
  }

  @Test
  void shouldExecuteWorldwideCheckForAllModels() throws Exception {
    Properties modelProps = new Properties();
    modelProps.setProperty("gemini-pro", "gemini-1.5-pro");
    modelProps.setProperty("gemini-flash", "gemini-1.5-flash");

    when(mockService.getAllRegions()).thenReturn(List.of("us-central1", "europe-west1"));
    when(mockService.checkRegionAvailability(any(RegionCheckRequest.class))).thenAnswer(invocation -> {
      RegionCheckRequest request = invocation.getArgument(0);
      return new RegionCheckResult(Map.of(request.getRegions().get(0), "SUCCESS"));
    });

    int exitCode = useCase.executeAllModels(authConfig, "test prompt", true, "models.properties", modelProps);

    assertThat(exitCode).isEqualTo(0);
    verify(mockService).getAllRegions();
    verify(mockService, times(4)).checkRegionAvailability(any(RegionCheckRequest.class));

    ArgumentCaptor<RegionCheckRequest> requestCaptor = ArgumentCaptor.forClass(RegionCheckRequest.class);
    verify(mockService, times(4)).checkRegionAvailability(requestCaptor.capture());
    assertThat(requestCaptor.getAllValues()).extracting(RegionCheckRequest::getModelName).contains("gemini-pro", "gemini-flash");
    assertThat(requestCaptor.getAllValues()).allMatch(RegionCheckRequest::isDebug);
  }

  @Test
  void shouldReturnFailureCodeWhenAllWorldwideModelChecksFail() throws Exception {
    Properties modelProps = new Properties();
    modelProps.setProperty("gemini-pro", "gemini-1.5-pro");
    modelProps.setProperty("gemini-flash", "gemini-1.5-flash");

    when(mockService.getAllRegions()).thenReturn(List.of("us-central1"));
    when(mockService.checkRegionAvailability(any(RegionCheckRequest.class)))
        .thenReturn(new RegionCheckResult(Map.of("us-central1", "404 Not Found")));

    int exitCode = useCase.executeAllModels(authConfig, "test prompt", false, "models.properties", modelProps);

    assertThat(exitCode).isEqualTo(1);
    verify(mockService, times(2)).checkRegionAvailability(any(RegionCheckRequest.class));
  }

  @Test
  void shouldCheckOnlyGlobalEndpointForGlobalWorldwideModel() throws Exception {
    Properties modelProps = new Properties();
    modelProps.setProperty("global-model", "publisher/global-model");
    modelProps.setProperty("global-model.region", "global");

    List<String> checkedRegions = new ArrayList<>();
    when(mockService.getAllRegions()).thenReturn(List.of("us-central1", "europe-west1"));
    when(mockService.checkRegionAvailability(any(RegionCheckRequest.class))).thenAnswer(invocation -> {
      RegionCheckRequest request = invocation.getArgument(0);
      checkedRegions.addAll(request.getRegions());
      return new RegionCheckResult(Map.of(request.getRegions().get(0), "SUCCESS"));
    });

    int exitCode = useCase.executeAllModels(authConfig, "test prompt", false, "models.properties", modelProps);

    assertThat(exitCode).isEqualTo(0);
    assertThat(checkedRegions).containsExactly("global");
  }

  @Test
  void shouldDisplayResultsWithFailures() throws Exception {
    Map<String, String> results = new HashMap<>();
    results.put("us-central1", "SUCCESS");
    results.put("europe-west1", "403 Permission Denied");
    RegionCheckResult mockResult = new RegionCheckResult(results);

    when(mockService.getAllRegions()).thenReturn(java.util.Arrays.asList("us-central1", "europe-west1"));
    when(mockService.checkRegionAvailability(any())).thenReturn(new RegionCheckResult(Map.of("us-central1", "SUCCESS")),
        new RegionCheckResult(Map.of("europe-west1", "403 Permission Denied")));

    RegionCheckResult result = useCase.execute(authConfig, "gemini-pro", "test prompt", false);

    assertThat(result.getRegionResults()).isEqualTo(mockResult.getRegionResults());
    assertThat(result.getSuccessCount()).isEqualTo(mockResult.getSuccessCount());
    assertThat(result.getFailCount()).isEqualTo(mockResult.getFailCount());
  }
}
