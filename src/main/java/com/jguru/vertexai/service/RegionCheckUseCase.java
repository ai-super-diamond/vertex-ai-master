package com.jguru.vertexai.service;

import com.jguru.vertexai.service.dto.AuthenticationConfig;
import com.jguru.vertexai.service.dto.RegionCheckRequest;
import com.jguru.vertexai.service.dto.RegionCheckResult;
import com.jguru.vertexai.utils.MarkdownReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class RegionCheckUseCase {

  private static final Logger logger = LoggerFactory.getLogger(RegionCheckUseCase.class);

  private final VertexAiService vertexAiService;

  public RegionCheckUseCase(VertexAiService vertexAiService) {
    this.vertexAiService = vertexAiService;
  }

  public RegionCheckResult execute(AuthenticationConfig authConfig, String modelName, String cluster, List<String> regions,
      String testPrompt, boolean debug) throws Exception {
    logger.info("\n=== Region Availability Check ===");
    logger.info("Model: {}", modelName);
    logger.info("Cluster: {}", cluster);
    logger.info("Regions to test: {}", regions.size());
    logger.info("Test prompt: {}", testPrompt);
    logger.info("\nTesting...");

    RegionCheckRequest request = RegionCheckRequest.builder().withAuthenticationConfig(authConfig).withModelName(modelName)
        .withCluster(cluster).withTestPrompt(testPrompt).withRegions(regions).withDebug(debug).build();

    RegionCheckResult result = vertexAiService.checkRegionAvailability(request);

    displayResults(result);

    return result;
  }

  public int executeAllModels(AuthenticationConfig authConfig, String cluster, List<String> regions, String testPrompt, boolean debug,
      String modelFile, Properties modelProperties) throws Exception {
    logger.info("\n========================================");
    logger.info("Testing All Models from File");
    logger.info("========================================");
    logger.info("Model file: {}", modelFile);

    java.util.Set<String> modelAliases = extractModelAliases(modelProperties);
    logger.info("Models found: {}", modelAliases.size());
    logger.info("Cluster: {}", cluster);
    logger.info("Regions to test: {}", regions.size());
    logger.info("Test prompt: {}", testPrompt);
    logger.info("");

    int totalModels = 0;
    int successfulModels = 0;

    java.util.Map<String, MarkdownReportGenerator.ModelTestResult> modelTestResults = new java.util.LinkedHashMap<>();

    for (String modelAlias : modelAliases) {
      totalModels++;
      logger.info("\n========================================");
      logger.info("Testing Model: {}", modelAlias);
      logger.info("========================================");

      String modelRegion = modelProperties.getProperty(modelAlias + ".region");
      boolean isGlobalModel = "global".equalsIgnoreCase(modelRegion);

      List<String> regionsToTest = isGlobalModel ? java.util.List.of("global") : regions;

      if (isGlobalModel) {
        logger.info("(Global model - testing with global endpoint only)");
      }

      RegionCheckRequest request = RegionCheckRequest.builder().withAuthenticationConfig(authConfig).withModelName(modelAlias)
          .withCluster(cluster).withTestPrompt(testPrompt).withRegions(regionsToTest).withDebug(debug).build();

      RegionCheckResult result = vertexAiService.checkRegionAvailability(request);

      modelTestResults.put(modelAlias, new MarkdownReportGenerator.ModelTestResult(modelAlias, result.getSuccessCount(),
          result.getFailCount(), result.getRegionResults()));

      displayModelResults(modelAlias, result);

      if (result.hasSuccess()) {
        successfulModels++;
      }
    }

    displayOverallSummary(totalModels, successfulModels);

    generateMarkdownReport("results", cluster, modelTestResults, modelProperties, testPrompt, regions.size(), authConfig.getProjectId());

    return successfulModels > 0 ? 0 : 1;
  }

  private java.util.Set<String> extractModelAliases(Properties modelProperties) {
    java.util.Set<String> modelAliases = new java.util.HashSet<>();
    for (Object key : modelProperties.keySet()) {
      String keyStr = key.toString();
      if (!keyStr.endsWith(".region") && !keyStr.endsWith(".provider") && !keyStr.endsWith(".openai") && !keyStr.endsWith(".api")
          && !keyStr.contains(".test.")) {
        modelAliases.add(keyStr);
      }
    }
    return modelAliases;
  }

  private void displayResults(RegionCheckResult result) {
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
    logger.info("Success: {}", result.getSuccessCount());
    logger.info("Failed: {}", result.getFailCount());
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

  private void generateMarkdownReport(String resultsDir, String cluster,
      Map<String, MarkdownReportGenerator.ModelTestResult> modelTestResults, Properties modelProperties, String testPrompt,
      int regionsCount, String projectId) {
    try {
      String reportPath = MarkdownReportGenerator.generateReport(resultsDir, cluster, modelTestResults, modelProperties, testPrompt,
          regionsCount, projectId);
      logger.info("\n📄 Markdown report generated: {}", reportPath);
    } catch (java.io.IOException e) {
      logger.warn("Failed to generate Markdown report: {}", e.getMessage());
    }
  }
}
