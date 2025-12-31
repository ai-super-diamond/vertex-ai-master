package com.jguru.vertexai;

import com.jguru.vertexai.service.VertexAiService;
import com.jguru.vertexai.service.VertexAiServiceImpl;
import com.jguru.vertexai.service.dto.AuthenticationConfig;
import com.jguru.vertexai.service.dto.AuthenticationType;
import com.jguru.vertexai.service.dto.GenerationRequest;
import com.jguru.vertexai.service.dto.GenerationResult;
import com.jguru.vertexai.service.dto.RegionCheckRequest;
import com.jguru.vertexai.service.dto.RegionCheckResult;
import com.jguru.vertexai.client.WorldwideAvailabilityClient;
import com.jguru.vertexai.utils.MarkdownReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Command(name = "vertex-ai", mixinStandardHelpOptions = true, version = "0.0.1", description = "A CLI for interacting with the Vertex AI API.")
public class VertexAiMasterMain implements Callable<Integer> {
  private static final Logger logger = LoggerFactory.getLogger(VertexAiMasterMain.class);

  static class ApiKeyAuth {
    @Option(names = "--api-key", description = "Your Vertex AI API key.", required = true)
    String apiKey;
  }

  static class ServiceAccountAuth {
    @Option(names = "--project-id", description = "Your Google Cloud project ID.")
    String projectId;

    @Option(names = "--location", description = "The Google Cloud location (e.g., us-central1). Optional for --check-all-regions and --worldwide modes.")
    String location;

    @Option(names = "--sa-key-file", description = "Path to Service Account JSON key file.")
    String saKeyFile;
  }

  @ArgGroup(multiplicity = "1")
  Auth auth;

  static class Auth {
    @ArgGroup(exclusive = false)
    ApiKeyAuth apiKeyAuth;

    @ArgGroup(exclusive = false)
    ServiceAccountAuth saAuth;
  }

  static class ModelSource {
    @Option(names = {"--model-name", "-m"}, description = "The name of the model to use.")
    String modelName;

    @Option(names = {"-model-file"}, description = "Test all models from properties file (.properties).")
    String modelFile;
  }

  @ArgGroup()
  ModelSource modelSource;

  @Option(names = {"--check-all-regions", "-car"}, description = "Check model availability across all regions in a cluster.")
  private boolean checkAllRegions;

  @Option(names = {"--cluster", "-c"}, description = "Region cluster to test (US, EU, ASIA, etc.). Used with --check-all-regions.")
  private String cluster;

  @Option(names = {"--worldwide", "-w"}, description = "Check model availability across all worldwide regions.")
  private boolean worldwide;

  @Option(names = {"--text", "-t"}, description = "The test prompt text (for region check mode).")
  private String textOption;

  @Option(names = {"--debug", "-d"}, description = "Enable debug mode for detailed error information in results.")
  private boolean debug;

  @Option(names = {"--output-file", "-o"}, description = "Write results to specified file instead of console.")
  private String outputFile;

  @Parameters(index = "0", arity = "0..1", description = "The text prompt to send to the model.")
  private String text;

  private final VertexAiService vertexAiService = new VertexAiServiceImpl();
  private PrintStream originalOut;
  private PrintStream originalErr;
  private PrintStream fileOut;

  private String getEffectiveModelName() {
    String name = (modelSource != null && modelSource.modelName != null && !modelSource.modelName.isBlank())
        ? modelSource.modelName
        : "gemini.pro";
    if (modelSource != null && modelSource.modelFile != null && !modelSource.modelFile.isBlank()) {
      System.setProperty("models.config", modelSource.modelFile);
    }
    return name;
  }

  private boolean isTestingAllModels() {
    return modelSource != null && modelSource.modelFile != null && !modelSource.modelFile.isBlank();
  }

  public boolean isDebugEnabled() {
    return debug;
  }

  private String getModelFile() {
    return (modelSource != null && modelSource.modelFile != null) ? modelSource.modelFile : null;
  }

  @Override
  public Integer call() throws Exception {
    // Set up output redirection if needed
    setupOutputRedirection();

    try {
      // Check if we're in region check mode
      if (checkAllRegions) {
        return performRegionCheck();
      }

      // Check if we're in worldwide check mode
      if (worldwide) {
        return performWorldwideCheck();
      }

      // Normal mode: generate content
      String prompt = textOption != null ? textOption : text;
      if (prompt == null || prompt.isEmpty()) {
        logger.error("No prompt text provided.");
        return 1;
      }

      // Create authentication config
      AuthenticationConfig authConfig = createAuthenticationConfig();
      if (authConfig == null) {
        return 1;
      }

      // Create generation request
      GenerationRequest request = GenerationRequest.builder().withAuthenticationConfig(authConfig).withModelName(getEffectiveModelName())
          .withText(prompt).build();

      // Generate content using service
      GenerationResult result = vertexAiService.generateContent(request);

      if (result.isSuccess()) {
        logger.info(result.getContent());
        return 0;
      } else {
        logger.error("Error generating content: {}", result.getErrorMessage());
        return 1;
      }
    } finally {
      closeOutputRedirection();
    }
  }

  private Integer performRegionCheck() throws Exception {
    if (auth.saAuth == null) {
      logger.error("Region check requires Service Account authentication.");
      return 1;
    }

    if (cluster == null || cluster.isEmpty()) {
      logger.error("--cluster (-c) is required with --check-all-regions.");
      return 1;
    }

    // Get regions for the specified cluster
    List<String> regions = getRegionsForCluster(cluster);
    if (regions == null || regions.isEmpty()) {
      logger.error("Unknown cluster '{}'. Valid options: US, EU, ASIA, MIDDLE_EAST, AFRICA, CANADA, SOUTH_AMERICA", cluster);
      return 1;
    }

    String testPrompt = (textOption != null && !textOption.isEmpty()) ? textOption : (text != null ? text : "200+200*99=?");

    // Check if we should test all models from a file
    if (isTestingAllModels()) {
      return testAllModelsFromFile(regions, testPrompt);
    }

    // Single model test
    logger.info("\n=== Region Availability Check ===");
    logger.info("Model: {}", getEffectiveModelName());
    logger.info("Cluster: {}", cluster);
    logger.info("Regions to test: {}", regions.size());
    logger.info("Test prompt: {}", testPrompt);
    logger.info("\nTesting...");

    // Create authentication config
    AuthenticationConfig authConfig = resolveServiceAccountAuthentication();
    if (authConfig == null) {
      throw new IllegalStateException("Service account configuration is required for region availability checks.");
    }

    // Create region check request
    RegionCheckRequest request = RegionCheckRequest.builder().withAuthenticationConfig(authConfig).withModelName(getEffectiveModelName())
        .withCluster(cluster).withTestPrompt(testPrompt).withRegions(regions).withDebug(debug).build();

    // Check region availability using service
    RegionCheckResult result = vertexAiService.checkRegionAvailability(request);

    // Display results
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

    return result.hasSuccess() ? 0 : 1;
  }

  private List<String> getRegionsForCluster(String clusterName) {
    return ((VertexAiServiceImpl) vertexAiService).getRegionsForCluster(clusterName);

  }

  private Integer testAllModelsFromFile(List<String> regions, String testPrompt) throws Exception {
    String modelFile = getModelFile();
    System.setProperty("models.config", modelFile);

    // Load properties file to get all model aliases
    Properties props = new Properties();
    try (java.io.FileInputStream fis = new java.io.FileInputStream(modelFile)) {
      props.load(fis);
    } catch (java.io.IOException e) {
      logger.error("Failed to load model file: {}", modelFile, e);
      return 1;
    }

    // Extract model aliases (skip .region, .provider, .openai, .api properties)
    java.util.Set<String> modelAliases = new java.util.HashSet<>();
    for (Object key : props.keySet()) {
      String keyStr = key.toString();
      // Skip sub-properties like .region, .provider, .openai, .api, .test.worldwide
      if (!keyStr.endsWith(".region") && !keyStr.endsWith(".provider") && !keyStr.endsWith(".openai") && !keyStr.endsWith(".api")
          && !keyStr.contains(".test.")) {
        modelAliases.add(keyStr);
      }
    }

    logger.info("\n========================================");
    logger.info("Testing All Models from File");
    logger.info("========================================");
    logger.info("Model file: {}", modelFile);
    logger.info("Models found: {}", modelAliases.size());
    logger.info("Cluster: {}", cluster);
    logger.info("Regions to test: {}", regions.size());
    logger.info("Test prompt: {}", testPrompt);
    logger.info("");

    // Create authentication config
    AuthenticationConfig authConfig = resolveServiceAccountAuthentication();
    if (authConfig == null) {
      throw new IllegalStateException("Service account configuration is required for region availability checks.");
    }

    int totalModels = 0;
    int successfulModels = 0;

    // Collect results for Markdown report
    java.util.Map<String, MarkdownReportGenerator.ModelTestResult> modelTestResults = new java.util.LinkedHashMap<>();

    // Test each model
    for (String modelAlias : modelAliases) {
      totalModels++;
      logger.info("\n========================================");
      logger.info("Testing Model: {}", modelAlias);
      logger.info("========================================");

      // Check if model has global region
      String modelRegion = props.getProperty(modelAlias + ".region");
      boolean isGlobalModel = "global".equalsIgnoreCase(modelRegion);

      // For global models, test only once with global endpoint
      List<String> regionsToTest = isGlobalModel ? java.util.List.of("global") : regions;

      if (isGlobalModel) {
        logger.info("(Global model - testing with global endpoint only)");
      }

      // Create region check request for this model
      RegionCheckRequest request = RegionCheckRequest.builder().withAuthenticationConfig(authConfig).withModelName(modelAlias)
          .withCluster(cluster).withTestPrompt(testPrompt).withRegions(regionsToTest).withDebug(debug).build();

      // Check region availability
      RegionCheckResult result = vertexAiService.checkRegionAvailability(request);

      // Store results for report
      modelTestResults.put(modelAlias, new MarkdownReportGenerator.ModelTestResult(modelAlias, result.getSuccessCount(),
          result.getFailCount(), result.getRegionResults()));

      // Display results for this model
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

      if (result.hasSuccess()) {
        successfulModels++;
      }
    }

    logger.info("\n========================================");
    logger.info("Overall Summary");
    logger.info("========================================");
    logger.info("Total models tested: {}", totalModels);
    logger.info("Models with at least one success: {}", successfulModels);
    logger.info("Models with all failures: {}", totalModels - successfulModels);

    // Generate Markdown report
    try {
      String reportPath = MarkdownReportGenerator.generateReport("results", cluster, modelTestResults, props, testPrompt, regions.size(),
          authConfig.getProjectId());
      logger.info("\n📄 Markdown report generated: {}", reportPath);
    } catch (java.io.IOException e) {
      logger.warn("Failed to generate Markdown report: {}", e.getMessage());
    }

    return successfulModels > 0 ? 0 : 1;
  }

  private AuthenticationConfig resolveServiceAccountAuthentication() {
    if (auth.saAuth == null) {
      logger.error("Service account credentials are required for this operation.");
      return null;
    }

    boolean hasKeyFile = auth.saAuth.saKeyFile != null && !auth.saAuth.saKeyFile.isBlank();

    // Determine base location:
    // - In region/worldwide modes: default automatically (first cluster region or us-central1)
    // - In normal mode: location must be provided
    String baseLocation = auth.saAuth.location;
    if (baseLocation == null || baseLocation.isBlank()) {
      if (checkAllRegions) {
        List<String> regionsForCluster = getRegionsForCluster(cluster);
        if (regionsForCluster != null && !regionsForCluster.isEmpty()) {
          baseLocation = regionsForCluster.get(0);
          logger.debug("Defaulting location to '{}' for region check mode", baseLocation);
        } else {
          baseLocation = "us-central1";
          logger.debug("Defaulting location to 'us-central1' for region check mode");
        }
      } else if (worldwide) {
        baseLocation = "us-central1";
        logger.debug("Defaulting location to 'us-central1' for worldwide mode");
      } else {
        logger.error("Service account location is required in normal mode.");
        return null;
      }
    }

    AuthenticationConfig.Builder builder = AuthenticationConfig.builder().withProjectId(auth.saAuth.projectId).withLocation(baseLocation);

    if (hasKeyFile) {
      builder.withType(AuthenticationType.SERVICE_ACCOUNT_EXPLICIT_KEY).withSaKeyFile(auth.saAuth.saKeyFile);
    } else {
      builder.withType(AuthenticationType.SERVICE_ACCOUNT_ADC);
    }

    try {
      return builder.build();
    } catch (IllegalArgumentException | IllegalStateException e) {
      logger.error("Invalid service account configuration: {}", e.getMessage());
      return null;
    }
  }

  private AuthenticationConfig createAuthenticationConfig() {
    try {
      if (auth.apiKeyAuth != null) {
        return AuthenticationConfig.builder().withType(AuthenticationType.API_KEY).withApiKey(auth.apiKeyAuth.apiKey).build();
      } else if (auth.saAuth != null) {
        return resolveServiceAccountAuthentication();
      } else {
        logger.error("Please provide either API key or Service Account credentials.");
        return null;
      }
    } catch (IllegalArgumentException | IllegalStateException e) {
      logger.error("Invalid authentication configuration: {}", e.getMessage());
      return null;
    }
  }

  private Integer performWorldwideCheck() throws Exception {
    if (auth.saAuth == null) {
      logger.error("Worldwide check requires Service Account authentication.");
      return 1;
    }

    String testPrompt = (textOption != null && !textOption.isEmpty()) ? textOption : (text != null ? text : "200+200*99=?");
    String modelAlias = getEffectiveModelName();

    logger.info("\n=== Worldwide Region Availability Check ===");
    logger.info("Model: {}", modelAlias);
    logger.info("Test prompt: {}", testPrompt);
    logger.info("\nTesting...");

    // Create authentication config
    AuthenticationConfig authConfig = resolveServiceAccountAuthentication();
    if (authConfig == null) {
      throw new IllegalStateException("Service account configuration is required for worldwide availability checks.");
    }

    // Create region check request
    RegionCheckRequest request = RegionCheckRequest.builder().withAuthenticationConfig(authConfig).withModelName(modelAlias)
        .withTestPrompt(testPrompt).withDebug(debug).build();

    // Check worldwide availability using the client
    WorldwideAvailabilityClient client = new WorldwideAvailabilityClient(vertexAiService);
    RegionCheckResult result = client.checkWorldwideAvailability(request);

    // Display results
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

    // Generate Markdown report for worldwide check
    try {
      // Load model properties for report
      String modelFile = getModelFile();
      if (modelFile == null) {
        modelFile = "src/main/resources/models.properties";
      }
      Properties props = new Properties();
      try (java.io.FileInputStream fis = new java.io.FileInputStream(modelFile)) {
        props.load(fis);
      }

      // Create a single-model result map
      java.util.Map<String, MarkdownReportGenerator.ModelTestResult> modelResults = new java.util.LinkedHashMap<>();
      modelResults.put(modelAlias,
          new MarkdownReportGenerator.ModelTestResult(modelAlias, successCount, failCount, result.getRegionResults()));

      String reportPath = MarkdownReportGenerator.generateReport("results", "WORLDWIDE", modelResults, props, testPrompt,
          result.getTotalCount(), authConfig.getProjectId());
      logger.info("\n📄 Markdown report generated: {}", reportPath);
    } catch (java.io.IOException e) {
      logger.warn("Failed to generate Markdown report: {}", e.getMessage());
    }

    return result.hasSuccess() ? 0 : 1;
  }

  private void setupOutputRedirection() throws Exception {
    // Generate default output file if not specified and in debug/region-check mode
    if (outputFile == null && (debug || checkAllRegions || worldwide)) {
      // Create results directory if it doesn't exist
      Path resultsDirPath = Paths.get("results");
      try {
        Files.createDirectories(resultsDirPath);
      } catch (Exception e) {
        logger.error("Failed to create results directory: {}", resultsDirPath, e);
        throw new RuntimeException("Failed to create results directory: " + resultsDirPath, e);
      }

      // Generate timestamped filename with special characters · and ꞉
      LocalDateTime now = LocalDateTime.now();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd·MM·yyyy_HH꞉mm꞉ss");
      String timestamp = now.format(formatter);
      outputFile = String.format("results/runtime-results-%s.txt", timestamp);
    }

    // Redirect output to file if specified
    if (outputFile != null) {
      originalOut = System.out;
      originalErr = System.err;
      Path filePath = Paths.get(outputFile);
      Path parentDirPath = filePath.getParent();

      // Create parent directories if they don't exist
      if (parentDirPath != null) {
        try {
          Files.createDirectories(parentDirPath);
        } catch (Exception e) {
          logger.error("Failed to create parent directory for output file: {}", parentDirPath, e);
          throw new RuntimeException("Failed to create parent directory for output file: " + parentDirPath, e);
        }
      }

      fileOut = new PrintStream(new FileOutputStream(filePath.toFile()), true, "UTF-8");
      System.setOut(fileOut);
      System.setErr(fileOut);
      // Log to original console that output is being redirected
      originalOut.println("Writing output to: " + filePath.toAbsolutePath());
    }
  }

  private void closeOutputRedirection() {
    if (fileOut != null) {
      fileOut.flush();
      fileOut.close();
      System.setOut(originalOut);
      System.setErr(originalErr);
      if (originalOut != null) {
        originalOut.println("Output written to: " + outputFile);
      }
    }
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new VertexAiMasterMain()).execute(args);
    System.exit(exitCode);
  }
}
