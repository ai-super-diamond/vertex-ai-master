package com.jguru.vertexai.service;

import com.jguru.vertexai.infrastructure.client.WorldwideAvailabilityClient;
import com.jguru.vertexai.domain.dto.AuthenticationConfig;
import com.jguru.vertexai.service.dto.RegionCheckRequest;
import com.jguru.vertexai.service.dto.RegionCheckResult;
import com.jguru.vertexai.utils.MarkdownReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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

  public int executeAllModels(AuthenticationConfig authConfig, String testPrompt, boolean debug, String modelFile,
      Properties modelProperties) throws Exception {
    logger.info("\n========================================");
    logger.info("Testing All Models Worldwide from File");
    logger.info("========================================");
    logger.info("Model file: {}", modelFile);

    List<String> allRegions = List.copyOf(vertexAiService.getAllRegions());
    java.util.Set<String> modelAliases = extractModelAliases(modelProperties);
    logger.info("Models found: {}", modelAliases.size());
    logger.info("Regions to test: {}", allRegions.size());
    logger.info("Test prompt: {}", testPrompt);
    logger.info("");

    int totalModels = 0;
    int successfulModels = 0;

    java.util.Map<String, MarkdownReportGenerator.ModelTestResult> modelTestResults = new java.util.LinkedHashMap<>();
    WorldwideAvailabilityClient client = new WorldwideAvailabilityClient(vertexAiService);

    for (String modelAlias : modelAliases) {
      totalModels++;
      logger.info("\n========================================");
      logger.info("Testing Model: {}", modelAlias);
      logger.info("========================================");

      String modelRegion = modelProperties.getProperty(modelAlias + ".region");
      boolean isGlobalModel = "global".equalsIgnoreCase(modelRegion);
      List<String> regionsToTest = isGlobalModel ? java.util.List.of("global") : allRegions;

      if (isGlobalModel) {
        logger.info("(Global model - testing with global endpoint only)");
      }

      RegionCheckRequest request = RegionCheckRequest.builder().withAuthenticationConfig(authConfig).withModelName(modelAlias)
          .withTestPrompt(testPrompt).withRegions(regionsToTest).withDebug(debug).build();

      RegionCheckResult result = client.checkWorldwideAvailability(request);

      modelTestResults.put(modelAlias, new MarkdownReportGenerator.ModelTestResult(modelAlias, result.getSuccessCount(),
          result.getFailCount(), result.getRegionResults()));

      displayModelResults(modelAlias, result);

      if (result.hasSuccess()) {
        successfulModels++;
      }
    }

    displayOverallSummary(totalModels, successfulModels);

    generateMarkdownReport("results", "WORLDWIDE", modelTestResults, modelProperties, testPrompt, allRegions.size(),
        authConfig.getProjectId());

    return successfulModels > 0 ? 0 : 1;
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

  private java.util.Set<String> extractModelAliases(Properties modelProperties) {
    java.util.Set<String> modelAliases = new java.util.TreeSet<>();
    for (Object key : modelProperties.keySet()) {
      String keyStr = key.toString();
      if (!keyStr.endsWith(".region") && !keyStr.endsWith(".provider") && !keyStr.endsWith(".openai") && !keyStr.endsWith(".api")
          && !keyStr.contains(".test.")) {
        modelAliases.add(keyStr);
      }
    }
    return modelAliases;
  }

  private void displayModelResults(String modelAlias, RegionCheckResult result) {
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

    logger.info("\nSummary for {}:", modelAlias);
    logger.info("  Success: {}", result.getSuccessCount());
    logger.info("  Failed: {}", result.getFailCount());
  }

  private void displayOverallSummary(int totalModels, int successfulModels) {
    logger.info("\n========================================");
    logger.info("Overall Summary");
    logger.info("========================================");
    logger.info("Total models tested: {}", totalModels);
    logger.info("Models with at least one success: {}", successfulModels);
    logger.info("Models with all failures: {}", totalModels - successfulModels);
  }

  private void generateMarkdownReport(String resultsDir, String testMode,
      Map<String, MarkdownReportGenerator.ModelTestResult> modelTestResults, Properties modelProperties, String testPrompt,
      int regionsCount, String projectId) {
    try {
      String reportPath = MarkdownReportGenerator.generateReport(resultsDir, testMode, modelTestResults, modelProperties, testPrompt,
          regionsCount, projectId);
      logger.info("\n📄 Markdown report generated: {}", reportPath);
    } catch (java.io.IOException e) {
      logger.warn("Failed to generate Markdown report: {}", e.getMessage());
    }
  }
}
