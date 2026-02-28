package com.jguru.vertexai.service;

import com.jguru.vertexai.infrastructure.client.WorldwideAvailabilityClient;
import com.jguru.vertexai.domain.dto.AuthenticationConfig;
import com.jguru.vertexai.service.dto.RegionCheckRequest;
import com.jguru.vertexai.service.dto.RegionCheckResult;
import com.jguru.vertexai.utils.MarkdownReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

public class WorldwideCheckUseCase {

  private static final Logger logger = LoggerFactory.getLogger(WorldwideCheckUseCase.class);

  private final VertexAiService vertexAiService;

  public WorldwideCheckUseCase(VertexAiService vertexAiService) {
    this.vertexAiService = vertexAiService;
  }

  public RegionCheckResult execute(AuthenticationConfig authConfig, String modelAlias, String testPrompt, boolean debug) throws Exception {
    logger.info("\n=== Worldwide Region Availability Check ===");
    logger.info("Model: {}", modelAlias);
    logger.info("Test prompt: {}", testPrompt);
    logger.info("\nTesting...");

    RegionCheckRequest request = RegionCheckRequest.builder().withAuthenticationConfig(authConfig).withModelName(modelAlias)
        .withTestPrompt(testPrompt).withDebug(debug).build();

    WorldwideAvailabilityClient client = new WorldwideAvailabilityClient(vertexAiService);
    RegionCheckResult result = client.checkWorldwideAvailability(request);

    displayResults(result);

    return result;
  }

  public void generateReport(RegionCheckResult result, String modelAlias, String testPrompt, String projectId, String modelFile,
      Properties modelProperties) {
    try {
      if (modelFile == null) {
        modelFile = "src/main/resources/models.properties";
      }

      java.util.Map<String, MarkdownReportGenerator.ModelTestResult> modelResults = new java.util.LinkedHashMap<>();
      modelResults.put(modelAlias, new MarkdownReportGenerator.ModelTestResult(modelAlias, result.getSuccessCount(), result.getFailCount(),
          result.getRegionResults()));

      String reportPath = MarkdownReportGenerator.generateReport("results", "WORLDWIDE", modelResults, modelProperties, testPrompt,
          result.getTotalCount(), projectId);
      logger.info("\n📄 Markdown report generated: {}", reportPath);
    } catch (java.io.IOException e) {
      logger.warn("Failed to generate Markdown report: {}", e.getMessage());
    }
  }

  private void displayResults(RegionCheckResult result) {
    int successCount = result.getSuccessCount();
    int failCount = result.getFailCount();

    logger.info("\n=== Results ===");
    for (Map.Entry<String, String> entry : result.getRegionResults().entrySet()) {
      String region = entry.getKey();
      String status = entry.getValue();
      boolean success = "SUCCESS".equals(status);

      if (success) {
        logger.info("✓ {}: {}", region, status);
      } else {
        logger.error("✗ {}: {}", region, status);
      }
    }

    logger.info("\n=== Summary ===");
    logger.info("Total: {}", result.getTotalCount());
    logger.info("Success: {}", successCount);
    logger.info("Failed: {}", failCount);
  }
}
