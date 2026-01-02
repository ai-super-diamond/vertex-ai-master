package com.jguru.vertexai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jguru.vertexai.service.dto.AuthenticationConfig;
import com.jguru.vertexai.service.dto.AuthenticationType;
import com.jguru.vertexai.service.dto.RegionCheckResult;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
