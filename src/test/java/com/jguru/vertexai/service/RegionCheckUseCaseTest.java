package com.jguru.vertexai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jguru.vertexai.domain.dto.AuthenticationConfig;
import com.jguru.vertexai.domain.dto.AuthenticationType;
import com.jguru.vertexai.service.dto.RegionCheckRequest;
import com.jguru.vertexai.service.dto.RegionCheckResult;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegionCheckUseCaseTest {

  private RegionCheckUseCase useCase;
  private VertexAiService mockService;
  private AuthenticationConfig authConfig;

  @BeforeEach
  void setUp() {
    mockService = mock(VertexAiService.class);
    useCase = new RegionCheckUseCase(mockService);
    authConfig = AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_ADC).withProjectId("test-project")
        .withLocation("us-central1").build();
  }

  @Test
  void shouldExecuteRegionCheck() throws Exception {
    List<String> regions = Arrays.asList("us-east1", "us-west1");
    Map<String, String> results = new HashMap<>();
    results.put("us-east1", "SUCCESS");
    results.put("us-west1", "SUCCESS");
    RegionCheckResult expectedResult = new RegionCheckResult(results);

    when(mockService.checkRegionAvailability(any(RegionCheckRequest.class))).thenReturn(expectedResult);

    RegionCheckResult result = useCase.execute(authConfig, "gemini-pro", "US", regions, "test prompt", false);

    assertThat(result).isNotNull();
    assertThat(result.getSuccessCount()).isEqualTo(2);
    assertThat(result.getFailCount()).isEqualTo(0);
    verify(mockService).checkRegionAvailability(any(RegionCheckRequest.class));
  }

  @Test
  void shouldHandleFailedRegions() throws Exception {
    List<String> regions = Arrays.asList("us-east1", "us-west1");
    Map<String, String> results = new HashMap<>();
    results.put("us-east1", "SUCCESS");
    results.put("us-west1", "404 Not Found");
    RegionCheckResult expectedResult = new RegionCheckResult(results);

    when(mockService.checkRegionAvailability(any(RegionCheckRequest.class))).thenReturn(expectedResult);

    RegionCheckResult result = useCase.execute(authConfig, "gemini-pro", "US", regions, "test prompt", false);

    assertThat(result).isNotNull();
    assertThat(result.getSuccessCount()).isEqualTo(1);
    assertThat(result.getFailCount()).isEqualTo(1);
  }

  @Test
  void shouldExecuteAllModelsCheck() throws Exception {
    List<String> regions = Arrays.asList("us-east1");
    Properties modelProps = new Properties();
    modelProps.setProperty("gemini-pro", "gemini-1.5-pro");
    modelProps.setProperty("gemini-flash", "gemini-1.5-flash");

    Map<String, String> results = new HashMap<>();
    results.put("us-east1", "SUCCESS");
    RegionCheckResult mockResult = new RegionCheckResult(results);

    when(mockService.checkRegionAvailability(any(RegionCheckRequest.class))).thenReturn(mockResult);

    int exitCode = useCase.executeAllModels(authConfig, "US", regions, "test prompt", false, "models.properties", modelProps);

    assertThat(exitCode).isEqualTo(0);
  }

  @Test
  void shouldHandleGlobalModels() throws Exception {
    List<String> regions = Arrays.asList("us-east1");
    Properties modelProps = new Properties();
    modelProps.setProperty("global-model", "test-global-model");
    modelProps.setProperty("global-model.region", "global");

    Map<String, String> results = new HashMap<>();
    results.put("global", "SUCCESS");
    RegionCheckResult mockResult = new RegionCheckResult(results);

    when(mockService.checkRegionAvailability(any(RegionCheckRequest.class))).thenReturn(mockResult);

    int exitCode = useCase.executeAllModels(authConfig, "US", regions, "test prompt", false, "models.properties", modelProps);

    assertThat(exitCode).isEqualTo(0);
  }

  @Test
  void shouldReturnFailureCodeWhenNoSuccesses() throws Exception {
    List<String> regions = Arrays.asList("us-east1");
    Properties modelProps = new Properties();
    modelProps.setProperty("test-model", "test-model-name");

    Map<String, String> results = new HashMap<>();
    results.put("us-east1", "404 Not Found");
    RegionCheckResult mockResult = new RegionCheckResult(results);

    when(mockService.checkRegionAvailability(any(RegionCheckRequest.class))).thenReturn(mockResult);

    int exitCode = useCase.executeAllModels(authConfig, "US", regions, "test prompt", false, "models.properties", modelProps);

    assertThat(exitCode).isEqualTo(1);
  }
}
