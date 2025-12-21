package com.jguru.vertexai.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Properties;

/**
 * Utility class for generating Markdown reports for model testing results.
 */
public class MarkdownReportGenerator {

  private static final DateTimeFormatter FILENAME_FORMATTER = DateTimeFormatter.ofPattern("dd·MM·yyyy_HH꞉mm꞉ss");

  /**
   * Generate a comprehensive Markdown report for model testing results.
   *
   * @param resultsDir
   *          Directory where the report will be saved
   * @param testMode
   *          Test mode (e.g., "US", "EU", "WORLDWIDE")
   * @param modelResults
   *          Map of model alias to their test results
   * @param modelProps
   *          Model properties configuration
   * @param testPrompt
   *          The prompt used for testing
   * @param regionsCount
   *          Number of regions tested
   * @param projectId
   *          Google Cloud project ID for URL construction
   * @return Path to the generated report file
   * @throws IOException
   *           if report generation fails
   */
  public static String generateReport(String resultsDir, String testMode, Map<String, ModelTestResult> modelResults, Properties modelProps,
      String testPrompt, int regionsCount, String projectId) throws IOException {

    String timestamp = LocalDateTime.now().format(FILENAME_FORMATTER);
    String reportFileName = String.format("%s/report-%s.md", resultsDir, timestamp);

    try (PrintWriter writer = new PrintWriter(new FileWriter(reportFileName))) {
      writeHeader(writer, testMode, testPrompt, regionsCount, modelResults.size());
      writeSummaryTable(writer, modelResults);
      writeDetailedResults(writer, modelResults, modelProps, projectId);
      writeFooter(writer);
    }

    return reportFileName;
  }

  private static void writeHeader(PrintWriter writer, String testMode, String testPrompt, int regionsCount, int modelCount) {
    writer.println("# 🚀 Vertex AI Model Testing Report");
    writer.println();
    writer.println("---");
    writer.println();
    writer.println("## 📋 Test Configuration");
    writer.println();
    writer.println("| Field | Value |");
    writer.println("|-------|-------|");
    writer.println("| **Test Mode** | " + testMode + " |");
    writer.println("| **Test Prompt** | `" + testPrompt + "` |");
    writer.println("| **Regions Tested** | " + regionsCount + " |");
    writer.println("| **Models Tested** | " + modelCount + " |");
    writer.println("| **Test Date** | " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " |");
    writer.println();
    writer.println("---");
    writer.println();
  }

  private static void writeSummaryTable(PrintWriter writer, Map<String, ModelTestResult> modelResults) {
    writer.println("## 📊 Summary Overview");
    writer.println();

    int totalSuccess = 0;
    int totalFailed = 0;
    int modelsWithSuccess = 0;

    for (ModelTestResult result : modelResults.values()) {
      totalSuccess += result.successCount;
      totalFailed += result.failCount;
      if (result.successCount > 0) {
        modelsWithSuccess++;
      }
    }

    writer.println("| Metric | Count | Percentage |");
    writer.println("|--------|-------|------------|");
    writer.println(String.format("| **Total Tests** | %d | 100%% |", totalSuccess + totalFailed));
    writer.println(
        String.format("| **✅ Successful Tests** | %d | %.1f%% |", totalSuccess, (totalSuccess * 100.0) / (totalSuccess + totalFailed)));
    writer
        .println(String.format("| **❌ Failed Tests** | %d | %.1f%% |", totalFailed, (totalFailed * 100.0) / (totalSuccess + totalFailed)));
    writer.println(String.format("| **Models with ≥1 Success** | %d | %.1f%% |", modelsWithSuccess,
        (modelsWithSuccess * 100.0) / modelResults.size()));
    writer.println();
    writer.println("---");
    writer.println();
  }

  private static void writeDetailedResults(PrintWriter writer, Map<String, ModelTestResult> modelResults, Properties modelProps,
      String projectId) {
    writer.println("## 📝 Detailed Model Results");
    writer.println();

    writer.println(
        "| # | Model Alias | Full Model Name | Provider | Region | API Type | Success | Failed | Status | Final Successful URL(s) |");
    writer.println(
        "|---|-------------|-----------------|----------|--------|----------|---------|--------|--------|--------------------------|");

    int index = 1;
    for (Map.Entry<String, ModelTestResult> entry : modelResults.entrySet()) {
      String alias = entry.getKey();
      ModelTestResult result = entry.getValue();

      String fullName = modelProps.getProperty(alias, "N/A");
      String provider = modelProps.getProperty(alias + ".provider", "native");
      String region = modelProps.getProperty(alias + ".region", "us-central1");
      String apiType = determineApiType(modelProps, alias);
      String status = result.successCount > 0 ? "✅" : "❌";
      String successfulUrls = buildSuccessfulUrls(apiType, result.regionResults, projectId);

      writer.println(String.format("| %d | `%s` | `%s` | %s | %s | %s | %d | %d | %s | %s |", index++, alias, fullName, provider, region,
          apiType, result.successCount, result.failCount, status, successfulUrls));
    }

    writer.println();
    writer.println("---");
    writer.println();
  }

  private static String determineApiType(Properties props, String alias) {
    String api = props.getProperty(alias + ".api");
    if ("rawPredict".equalsIgnoreCase(api)) {
      return "RawPredict";
    }

    String provider = props.getProperty(alias + ".provider");
    if (provider != null && !provider.isEmpty()) {
      return "Chat Completions";
    }

    return "Vertex AI SDK";
  }

  private static String buildSuccessfulUrls(String apiType, Map<String, String> regionResults, String projectId) {
    if (!"Chat Completions".equals(apiType)) {
      return "N/A";
    }

    StringBuilder urls = new StringBuilder();
    for (Map.Entry<String, String> entry : regionResults.entrySet()) {
      if ("SUCCESS".equals(entry.getValue())) {
        String region = entry.getKey();
        String host = "global".equals(region) ? "aiplatform.googleapis.com" : region + "-aiplatform.googleapis.com";
        String url = String.format("https://%s/v1/projects/%s/locations/%s/endpoints/openapi/chat/completions", host, projectId, region);
        if (urls.length() > 0) {
          urls.append(", ");
        }
        urls.append(url);
      }
    }

    return urls.length() > 0 ? urls.toString() : "None";
  }

  private static void writeFooter(PrintWriter writer) {
    writer.println("## 🔍 Legend");
    writer.println();
    writer.println("- **✅ Status**: Model has at least one successful region test");
    writer.println("- **❌ Status**: Model failed in all tested regions");
    writer.println("- **API Types**:");
    writer.println("  - **Vertex AI SDK**: Native Google Vertex AI SDK");
    writer.println("  - **Chat Completions**: OpenAI-compatible Chat Completions API (MaaS models)");
    writer.println("  - **RawPredict**: Raw prediction API (Mistral models)");
    writer.println();
    writer.println("---");
    writer.println();
    writer.println("*Generated by Vertex AI Master CLI*");
    writer.println();
  }

  /**
   * Data class to hold model test results.
   */
  public static class ModelTestResult {
    public final String alias;
    public final int successCount;
    public final int failCount;
    public final Map<String, String> regionResults;

    public ModelTestResult(String alias, int successCount, int failCount, Map<String, String> regionResults) {
      this.alias = alias;
      this.successCount = successCount;
      this.failCount = failCount;
      this.regionResults = regionResults;
    }
  }
}
