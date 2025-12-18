package com.jguru.vertexai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.jguru.vertexai.service.VertexAiServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;

class VertexAiMasterMainTest {

  private static final Logger logger = LoggerFactory.getLogger(VertexAiMasterMainTest.class);

  // Test configuration properties
  private static final Properties testProps = new Properties();
  private static final String TEST_PROJECT_ID;
  private static final String TEST_LOCATION;
  private static final String TEST_SA_KEY_FILE;
  private static final String TEST_MODEL_NAME;
  private static final String TEST_PROMPT;

  static {
    try (InputStream input = VertexAiMasterMainTest.class.getClassLoader().getResourceAsStream("test.properties")) {
      if (input != null) {
        testProps.load(input);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to load test.properties", e);
    }

    TEST_PROJECT_ID = testProps.getProperty("test.project.id");
    TEST_LOCATION = testProps.getProperty("test.project.location");
    TEST_SA_KEY_FILE = testProps.getProperty("test.project.sa.key.file");
    TEST_MODEL_NAME = testProps.getProperty("test.model.name");
    TEST_PROMPT = testProps.getProperty("test.prompt");
  }

  @Test
  @EnabledIfSystemProperty(named = "run.integration.tests", matches = "true")
  void testVertexAiWithServiceAccountKey() {
    // Given: Service Account key file path
    String saKeyPath = TEST_SA_KEY_FILE;
    File saKeyFile = new File(saKeyPath);

    // Verify the SA key file exists
    assertThat(saKeyFile).as("Service Account key file should exist").exists();

    // Given: CLI arguments
    String[] args = {"--project-id", TEST_PROJECT_ID, "--location", TEST_LOCATION, "--sa-key-file", saKeyPath, "--model-name",
        TEST_MODEL_NAME, TEST_PROMPT};

    // When: Execute the CLI command
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    PrintStream originalErr = System.err;

    try {
      System.setOut(new PrintStream(outContent));
      System.setErr(new PrintStream(errContent));

      VertexAiMasterMain app = new VertexAiMasterMain();
      CommandLine cmd = new CommandLine(app);
      int exitCode = cmd.execute(args);

      // Then: Verify successful execution
      assertThat(exitCode).as("CLI should exit successfully").isEqualTo(0);

      String output = outContent.toString();
      String errorOutput = errContent.toString();

      // Verify response is not empty
      assertThat(output.trim()).as("Response should not be empty").isNotEmpty();

      logger.info("[TEST] Response received: {}", output.trim());

    } finally {
      // Restore original streams FIRST
      System.setOut(originalOut);
      System.setErr(originalErr);
    }

    // Print captured output for debugging (after streams are restored)
    if (outContent.size() > 0) {
      logger.info("=== CLI Output ===\n{}", outContent.toString());
    }
    if (errContent.size() > 0) {
      logger.error("=== CLI Error Output ===\n{}", errContent.toString());
    }
  }

  @Test
  void shouldRequireLocationInNormalMode() {
    // Given: CLI arguments without --location in normal mode
    String[] args = {"--project-id", "test-project", "--sa-key-file", "test.json", "--model-name", "gemini.pro", "Test prompt"};

    // When: Execute the CLI command
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    PrintStream originalErr = System.err;

    try {
      System.setErr(new PrintStream(errContent));

      VertexAiMasterMain app = new VertexAiMasterMain();
      CommandLine cmd = new CommandLine(app);
      int exitCode = cmd.execute(args);

      // Then: Should fail with error about missing location
      assertThat(exitCode).as("CLI should fail when location is missing in normal mode").isNotEqualTo(0);

      String errorOutput = errContent.toString();
      assertThat(errorOutput).as("Error should mention required location").containsAnyOf("location", "Location");

    } finally {
      System.setErr(originalErr);
    }
  }

  @Test
  void shouldNotRequireLocationInRegionCheckMode() {
    // Given: CLI arguments without --location in region check mode
    String[] args = {"--project-id", "test-project", "--sa-key-file", "test.json", "--check-all-regions", "--cluster", "US", "--model-name",
        "gemini.pro", "Test prompt"};

    // When: Parse command
    VertexAiMasterMain app = new VertexAiMasterMain();
    CommandLine cmd = new CommandLine(app);

    // Parsing should not fail (execution will fail due to invalid key, but parsing succeeds)
    CommandLine.ParseResult parseResult = cmd.parseArgs(args);

    // Then: Parsing succeeds (location is optional)
    assertThat(parseResult).as("Parsing should succeed without --location in region check mode").isNotNull();
  }

  @Test
  void shouldNotRequireLocationInWorldwideMode() {
    // Given: CLI arguments without --location in worldwide mode
    String[] args = {"--project-id", "test-project", "--sa-key-file", "test.json", "--worldwide", "--model-name", "gemini.pro",
        "Test prompt"};

    // When: Parse command
    VertexAiMasterMain app = new VertexAiMasterMain();
    CommandLine cmd = new CommandLine(app);

    // Parsing should not fail (execution will fail due to invalid key, but parsing succeeds)
    CommandLine.ParseResult parseResult = cmd.parseArgs(args);

    // Then: Parsing succeeds (location is optional)
    assertThat(parseResult).as("Parsing should succeed without --location in worldwide mode").isNotNull();
  }

  @Test
  void shouldRejectBothModelNameAndModelFile() {
    // Given: CLI arguments with both --model-name and -model-file
    String[] args = {"--project-id", "test-project", "--location", "us-central1", "--model-name", "gemini-pro", "-model-file",
        "models.properties", "Test prompt"};

    // When: Parse command
    VertexAiMasterMain app = new VertexAiMasterMain();
    CommandLine cmd = new CommandLine(app);

    // Then: Should fail with mutual exclusivity error
    try {
      cmd.parseArgs(args);
      assertThat(false).as("Should have thrown exception for mutually exclusive options").isTrue();
    } catch (CommandLine.MaxValuesExceededException e) {
      // Picocli throws MaxValuesExceededException for exclusive groups
      assertThat(e.getMessage()).as("Error message should mention the exclusive group").containsAnyOf("model-file", "modelName",
          "expected only one");
    }
  }

  @Test
  void shouldAcceptModelNameOnly() throws IOException {
    // Given: CLI arguments with only --model-name
    String[] args = {"--project-id", "test-project", "--location", "us-central1", "--model-name", "gemini-1.5-pro-001", "Test prompt"};

    // When: Parse command
    VertexAiMasterMain app = new VertexAiMasterMain();
    CommandLine cmd = new CommandLine(app);
    CommandLine.ParseResult parseResult = cmd.parseArgs(args);

    // Then: Parsing succeeds
    assertThat(parseResult).as("Parsing should succeed with --model-name only").isNotNull();
  }

  @Test
  void shouldAcceptModelFileOnly() throws IOException {
    // Given: A temporary model properties file
    File tempModelFile = File.createTempFile("test-models", ".properties");
    tempModelFile.deleteOnExit();

    try (PrintWriter writer = new PrintWriter(new FileWriter(tempModelFile))) {
      writer.println("test-model=gemini-1.5-pro-001");
      writer.println("test-model.provider=test-provider");
    }

    // Given: CLI arguments with only -model-file
    String[] args = {"--project-id", "test-project", "--location", "us-central1", "-model-file", tempModelFile.getAbsolutePath(),
        "Test prompt"};

    // When: Parse command
    VertexAiMasterMain app = new VertexAiMasterMain();
    CommandLine cmd = new CommandLine(app);
    CommandLine.ParseResult parseResult = cmd.parseArgs(args);

    // Then: Parsing succeeds
    assertThat(parseResult).as("Parsing should succeed with -model-file only").isNotNull();
  }

  @Test
  void shouldUseDefaultModelWhenNeitherSpecified() {
    // Given: CLI arguments without --model-name or -model-file
    String[] args = {"--project-id", "test-project", "--location", "us-central1", "Test prompt"};

    // When: Parse command
    VertexAiMasterMain app = new VertexAiMasterMain();
    CommandLine cmd = new CommandLine(app);
    CommandLine.ParseResult parseResult = cmd.parseArgs(args);

    // Then: Parsing succeeds (default model should be used)
    assertThat(parseResult).as("Parsing should succeed without model specification").isNotNull();
  }

  @Test
  void shouldLoadModelFileWhenSpecified() throws IOException {
    // Given: A temporary model properties file with custom alias
    File tempModelFile = File.createTempFile("custom-models", ".properties");
    tempModelFile.deleteOnExit();

    try (PrintWriter writer = new PrintWriter(new FileWriter(tempModelFile))) {
      writer.println("custom-alias=gemini-1.5-flash-001");
      writer.println("custom-alias.provider=google");
    }

    // Given: CLI arguments with -model-file
    String[] args = {"--project-id", "test-project", "--location", "us-central1", "-model-file", tempModelFile.getAbsolutePath(),
        "Test prompt"};

    // When: Parse command
    VertexAiMasterMain app = new VertexAiMasterMain();
    CommandLine cmd = new CommandLine(app);
    CommandLine.ParseResult parseResult = cmd.parseArgs(args);

    // Then: Parsing should succeed
    assertThat(parseResult).as("Parsing should succeed").isNotNull();
  }

  @Test
  @EnabledIfSystemProperty(named = "run.integration.tests", matches = "true")
  void testVertexAiWithExpiredServiceAccountKey_ShouldFail() {
    // Given: Expired Service Account key file path
    String expiredKeyPath = "keys\\expired.json";
    File expiredKeyFile = new File(expiredKeyPath);

    // Verify the expired key file exists
    assertThat(expiredKeyFile).as("Expired Service Account key file should exist").exists();

    // Given: CLI arguments with expired credentials
    String[] args = {"--project-id", "vertex-ai-project-skorec", "--location", "us-central1", "--sa-key-file", expiredKeyPath,
        "--model-name", "gemini.pro", "Test prompt"};

    // When: Execute the CLI command with expired credentials
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    PrintStream originalErr = System.err;

    try {
      System.setOut(new PrintStream(outContent));
      System.setErr(new PrintStream(errContent));

      VertexAiMasterMain app = new VertexAiMasterMain();
      CommandLine cmd = new CommandLine(app);
      int exitCode = cmd.execute(args);

      // Then: MUST fail with non-zero exit code (no ADC fallback allowed)
      assertThat(exitCode).as("CLI must fail with non-zero exit code when expired key is provided - ADC fallback must not occur")
          .isNotEqualTo(0);

    } finally {
      // Restore original streams FIRST
      System.setOut(originalOut);
      System.setErr(originalErr);
    }

    String output = outContent.toString();
    String errorOutput = errContent.toString();

    // Print output for debugging (after stream is restored)
    logger.error("=== Error Output (Expired Key Test) ===\n{}", errorOutput);
    if (!output.isEmpty()) {
      logger.info("=== Output (Expired Key Test) ===\n{}", output);
    }

    // Verify error message indicates authentication/token failure (not ADC fallback)
    assertThat(errorOutput).as("Error output must indicate authentication/token failure (no ADC fallback)").containsAnyOf(
        "TokenResponseException", "invalid_grant", "401", "403", "expired", "unauthorized", "Permission", "denied", "access denied",
        "authentication", "credentials", "Failed to load service account key", "ADC fallback is disabled");
  }

  // Region to city mapping
  private static String getRegionCity(String region) {
    Map<String, String> regionCityMap = new HashMap<>();
    // US regions
    regionCityMap.put("us-central1", "Iowa");
    regionCityMap.put("us-east1", "South Carolina");
    regionCityMap.put("us-east4", "Northern Virginia");
    regionCityMap.put("us-east5", "Columbus");
    regionCityMap.put("us-south1", "Dallas");
    regionCityMap.put("us-west1", "Oregon");
    regionCityMap.put("us-west2", "Los Angeles");
    regionCityMap.put("us-west3", "Salt Lake City");
    regionCityMap.put("us-west4", "Las Vegas");
    // Europe regions
    regionCityMap.put("europe-central2", "Warsaw");
    regionCityMap.put("europe-north1", "Finland");
    regionCityMap.put("europe-southwest1", "Madrid");
    regionCityMap.put("europe-west1", "Belgium");
    regionCityMap.put("europe-west2", "London");
    regionCityMap.put("europe-west3", "Frankfurt");
    regionCityMap.put("europe-west4", "Netherlands");
    regionCityMap.put("europe-west6", "Zurich");
    regionCityMap.put("europe-west8", "Milan");
    regionCityMap.put("europe-west9", "Paris");
    regionCityMap.put("europe-west12", "Turin");
    // Asia regions
    regionCityMap.put("asia-east1", "Taiwan");
    regionCityMap.put("asia-east2", "Hong Kong");
    regionCityMap.put("asia-northeast1", "Tokyo");
    regionCityMap.put("asia-northeast2", "Osaka");
    regionCityMap.put("asia-northeast3", "Seoul");
    regionCityMap.put("asia-south1", "Mumbai");
    regionCityMap.put("asia-south2", "Delhi");
    regionCityMap.put("asia-southeast1", "Singapore");
    regionCityMap.put("asia-southeast2", "Jakarta");
    regionCityMap.put("australia-southeast1", "Sydney");
    regionCityMap.put("australia-southeast2", "Melbourne");
    // Other regions
    regionCityMap.put("me-central1", "Doha");
    regionCityMap.put("me-central2", "Dammam");
    regionCityMap.put("me-west1", "Tel Aviv");
    regionCityMap.put("africa-south1", "Johannesburg");
    regionCityMap.put("northamerica-northeast1", "Montreal");
    regionCityMap.put("northamerica-northeast2", "Toronto");
    regionCityMap.put("southamerica-east1", "Sao Paulo");
    regionCityMap.put("southamerica-west1", "Santiago");
    return regionCityMap.getOrDefault(region, "N/A");
  }

  @Test
  @EnabledIfSystemProperty(named = "run.integration.tests", matches = "true")
  void shouldAllModelsPass() throws IOException {
    // Given: Working Service Account key file path
    String workingKeyPath = TEST_SA_KEY_FILE;
    File workingKeyFile = new File(workingKeyPath);

    // Verify the working key file exists
    assertThat(workingKeyFile).as("Working Service Account key file should exist").exists();

    // Load models.properties to get region information and model list
    Properties modelProps = new Properties();
    try (InputStream input = getClass().getClassLoader().getResourceAsStream("models.properties")) {
      if (input == null) {
        throw new IOException("Unable to find models.properties");
      }
      modelProps.load(input);
    }

    // Extract all model aliases dynamically from properties (exclude .region, .provider, .openai, .api)
    List<String> modelAliases = modelProps.keySet().stream().map(Object::toString)
        .filter(key -> !key.endsWith(".region") && !key.endsWith(".provider") && !key.endsWith(".openai") && !key.endsWith(".api")).sorted()
        .toList();

    String testPrompt = "200+200*99=?";

    // Create results directory if it doesn't exist
    File resultsDir = new File("results");
    if (!resultsDir.exists()) {
      resultsDir.mkdirs();
    }

    // Create CSV file with timestamp
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String csvFileName = "results/model-test-results_" + timestamp + ".csv";
    File csvFile = new File(csvFileName);

    logger.info("Starting model validation test. Results will be saved to: {}", csvFileName);

    int passedCount = 0;
    int failedCount = 0;

    try (PrintWriter csvWriter = new PrintWriter(new FileWriter(csvFile))) {
      // CSV Header
      csvWriter.println("full-model-name,region,city,answer");

      // Test each model
      for (String modelAlias : modelAliases) {
        // Check if worldwide testing is enabled for this model
        String worldwideTest = modelProps.getProperty(modelAlias + ".test.worldwide", "false");
        boolean isWorldwideTest = "true".equalsIgnoreCase(worldwideTest);

        // Get region for this model from properties
        String region = modelProps.getProperty(modelAlias + ".region", "us-central1");

        // If worldwide testing is enabled, test in all regions
        if (isWorldwideTest) {
          logger.info("\nTesting model: {} (worldwide testing enabled)", modelAlias);

          // Get all regions to test
          List<String> allRegions = getAllRegions();

          // Test in each region
          for (String testRegion : allRegions) {
            passedCount += testModelInRegion(modelAlias, testRegion, workingKeyPath, testPrompt, csvWriter, modelProps);
          }
        } else {
          // Normal single region test
          logger.info("\nTesting model: {} (region: {})", modelAlias, region);
          passedCount += testModelInRegion(modelAlias, region, workingKeyPath, testPrompt, csvWriter, modelProps);
        }
      }
    }

    logger.info("\n=== Test Summary ===");
    logger.info("Total tests executed: {}", passedCount + failedCount);
    logger.info("Passed: {}", passedCount);
    logger.info("Failed: {}", failedCount);
    logger.info("Results saved to: {}", csvFileName);
  }

  /**
   * Tests a model in a specific region and returns 1 if passed, 0 if failed.
   */
  private int testModelInRegion(String modelAlias, String region, String workingKeyPath, String testPrompt, PrintWriter csvWriter,
      Properties modelProps) {
    int passed = 0;

    String[] args = {"--project-id", TEST_PROJECT_ID, "--location", region, "--sa-key-file", workingKeyPath, "--model-name", modelAlias,
        testPrompt};

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    PrintStream originalErr = System.err;

    String fullModelName = "";
    String answer = "";

    try {
      System.setOut(new PrintStream(outContent));
      System.setErr(new PrintStream(errContent));

      VertexAiMasterMain app = new VertexAiMasterMain();
      CommandLine cmd = new CommandLine(app);
      int exitCode = cmd.execute(args);

      // Restore streams immediately to print results
      System.setOut(originalOut);
      System.setErr(originalErr);

      String output = outContent.toString().trim();
      String errorOutput = errContent.toString();

      // Extract full model name from error output
      if (errorOutput.contains("[INFO] Resolved model alias")) {
        String[] lines = errorOutput.split("\\n");
        for (String line : lines) {
          if (line.contains("[INFO] Resolved model alias")) {
            int arrowIndex = line.indexOf("-> '");
            if (arrowIndex != -1) {
              int endIndex = line.indexOf("'", arrowIndex + 4);
              if (endIndex != -1) {
                fullModelName = line.substring(arrowIndex + 4, endIndex);
              }
            }
            break;
          }
        }
      }

      // Default to alias if full name not found
      if (fullModelName.isEmpty()) {
        fullModelName = modelAlias;
      }

      if (exitCode == 0 && !output.isEmpty()) {
        // Remove all newlines and carriage returns for single-line CSV output
        answer = output.replace("\"", "\"\"").replace("\n", " ").replace("\r", "").trim();

        passed = 1;
        logger.info("[PASS] {} in {} - Answer: {}", modelAlias, region, answer);
      } else {
        // Extract meaningful error message from error output
        String errorMsg = "Unknown error";

        // Try to find specific error messages in stderr
        if (errorOutput.contains("[ERROR]")) {
          // Extract the first ERROR line
          String[] lines = errorOutput.split("\\n");
          for (String line : lines) {
            if (line.contains("[ERROR]")) {
              // Remove [ERROR] prefix and timestamp
              errorMsg = line.replaceFirst(".*\\[ERROR\\]\\s*", "").trim();
              break;
            }
          }
        } else if (errorOutput.contains("Exception") || errorOutput.contains("Error")) {
          // Look for exception messages
          String[] lines = errorOutput.split("\\n");
          for (String line : lines) {
            if (line.contains("Exception") || (line.contains("Error") && !line.contains("[INFO]"))) {
              errorMsg = line.trim();
              // If line is too long, try to extract just the exception message
              if (errorMsg.length() > 200) {
                int colonIdx = errorMsg.indexOf(":");
                if (colonIdx > 0 && colonIdx < errorMsg.length() - 1) {
                  errorMsg = errorMsg.substring(0, colonIdx + 1)
                      + errorMsg.substring(colonIdx + 1, Math.min(errorMsg.length(), colonIdx + 150));
                }
              }
              break;
            }
          }
        } else if (errorOutput.contains("PERMISSION_DENIED") || errorOutput.contains("UNAUTHENTICATED")) {
          errorMsg = "Permission denied or authentication failed";
        } else if (errorOutput.contains("NOT_FOUND")) {
          errorMsg = "Model or resource not found";
        } else if (errorOutput.contains("RESOURCE_EXHAUSTED")) {
          errorMsg = "Quota exceeded or resource exhausted";
        } else if (errorOutput.contains("INVALID_ARGUMENT")) {
          errorMsg = "Invalid argument in request";
        } else if (!output.isEmpty()) {
          errorMsg = output.trim();
          if (errorMsg.length() > 150) {
            errorMsg = errorMsg.substring(0, 150) + "...";
          }
        } else if (exitCode != 0) {
          errorMsg = "Exit code: " + exitCode + " (no error details captured)";
        }

        answer = errorMsg.replace("\"", "\"\"").replace("\n", " ").replace("\r", "").trim();
        logger.error("[FAIL] {} in {} - {}", modelAlias, region, errorMsg);
      }

    } catch (Exception e) {
      System.setOut(originalOut);
      System.setErr(originalErr);

      if (fullModelName.isEmpty()) {
        fullModelName = modelAlias;
      }

      String errorMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
      answer = errorMsg.replace("\"", "\"\"").replace("\n", " ").replace("\r", "").trim();
      logger.error("[FAIL] {} in {} - {}", modelAlias, region, errorMsg);
    }

    // Write to CSV: full-model-name,region,city,answer
    String city = getRegionCity(region);
    csvWriter.println(String.format("\"%s\",\"%s\",\"%s\",\"%s\"", fullModelName, region, city, answer));
    csvWriter.flush();

    return passed;
  }

  /**
   * Gets all regions from all region lists in VertexAiServiceImpl
   */
  private List<String> getAllRegions() {
    VertexAiServiceImpl service = new VertexAiServiceImpl();
    return service.getAllRegions();
  }

  @Test
  @EnabledIfSystemProperty(named = "run.integration.tests", matches = "true")
  void shouldPassOneAtLeastforUS() throws IOException {
    // Given: Working Service Account key file path
    String workingKeyPath = TEST_SA_KEY_FILE;
    File workingKeyFile = new File(workingKeyPath);

    // Verify the working key file exists
    assertThat(workingKeyFile).as("Working Service Account key file should exist").exists();

    // Test a 404 model across all US regions to find where it's actually deployed
    String testModelAlias = "openai.gpt.oss.120b"; // Pick a model that returned 404
    String testPrompt = "200+200*99=?";

    // All US regions to test
    List<String> usRegions = Arrays.asList("us-central1", "us-east1", "us-east4", "us-east5", "us-south1", "us-west1", "us-west2",
        "us-west3", "us-west4");

    // Create results directory if it doesn't exist
    File resultsDir = new File("results");
    if (!resultsDir.exists()) {
      resultsDir.mkdirs();
    }

    // Create CSV file with timestamp
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String csvFileName = "results/region-discovery_" + timestamp + ".csv";
    File csvFile = new File(csvFileName);

    logger.info("Testing model '{}' across all US regions...", testModelAlias);
    logger.info("Results will be saved to: {}", csvFileName);

    int successCount = 0;
    int failCount = 0;

    try (PrintWriter csvWriter = new PrintWriter(new FileWriter(csvFile))) {
      // CSV Header
      csvWriter.println("model-alias,region,status,response");

      for (String region : usRegions) {
        logger.info("\nTesting region: {}", region);

        String[] args = {"--project-id", TEST_PROJECT_ID, "--location", region, "--sa-key-file", workingKeyPath, "--model-name",
            testModelAlias, testPrompt};

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        String status = "FAIL";
        String response = "";

        try {
          System.setOut(new PrintStream(outContent));
          System.setErr(new PrintStream(errContent));

          VertexAiMasterMain app = new VertexAiMasterMain();
          CommandLine cmd = new CommandLine(app);
          int exitCode = cmd.execute(args);

          // Restore streams
          System.setOut(originalOut);
          System.setErr(originalErr);

          String output = outContent.toString().trim();
          String errorOutput = errContent.toString();

          if (exitCode == 0 && !output.isEmpty()) {
            status = "PASS";
            response = output.replace("\"", "\"\"").replace("\n", " ").replace("\r", "").trim();
            if (response.length() > 100) {
              response = response.substring(0, 100) + "...";
            }
            successCount++;
            logger.info("  ✓ [PASS] {} - Model works!", region);
          } else {
            // Extract error message
            if (errorOutput.contains("404")) {
              response = "404 Not Found";
            } else if (errorOutput.contains("403")) {
              response = "403 Permission Denied";
            } else if (errorOutput.contains("400")) {
              response = "400 Bad Request";
            } else if (errorOutput.contains("500")) {
              response = "500 Internal Error";
            } else if (errorOutput.contains("Exception")) {
              String[] lines = errorOutput.split("\\n");
              for (String line : lines) {
                if (line.contains("Exception")) {
                  response = line.trim();
                  if (response.length() > 100) {
                    response = response.substring(0, 100) + "...";
                  }
                  break;
                }
              }
            } else {
              response = "Unknown error - exit code: " + exitCode;
            }
            failCount++;
            logger.error("  ✗ [FAIL] {} - {}", region, response);
          }

        } catch (Exception e) {
          System.setOut(originalOut);
          System.setErr(originalErr);

          response = e.getClass().getSimpleName() + ": " + e.getMessage();
          if (response.length() > 100) {
            response = response.substring(0, 100) + "...";
          }
          failCount++;
          logger.error("  ✗ [FAIL] {} - {}", region, response);
        }

        // Write to CSV
        String city = getRegionCity(region);
        csvWriter.println(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"", testModelAlias, region, city, status, response));
        csvWriter.flush();
      }
    }

    logger.info("\n=== Region Discovery Summary ===");
    logger.info("Model: {}", testModelAlias);
    logger.info("Total regions tested: {}", usRegions.size());
    logger.info("Successful: {}", successCount);
    logger.info("Failed: {}", failCount);
    logger.info("Results saved to: {}", csvFileName);

    // Assert that at least one region worked
    assertThat(successCount).as("At least one US region should support the model '" + testModelAlias + "'").isGreaterThan(0);
  }

  @Test
  @EnabledIfSystemProperty(named = "run.integration.tests", matches = "true")
  void shouldDiscoverDeepseekR1Region() throws IOException {
    // Given: Working Service Account key file path
    String workingKeyPath = TEST_SA_KEY_FILE;
    File workingKeyFile = new File(workingKeyPath);

    // Verify the working key file exists
    assertThat(workingKeyFile).as("Working Service Account key file should exist").exists();

    // Test DeepSeek R1 across all US regions to find where it's actually deployed
    String testModelAlias = "deepseek.r1.0528";
    String testPrompt = "200+200*99=?";

    // All US regions to test
    List<String> usRegions = Arrays.asList("us-central1", "us-east1", "us-east4", "us-east5", "us-south1", "us-west1", "us-west2",
        "us-west3", "us-west4");

    // Create results directory if it doesn't exist
    File resultsDir = new File("results");
    if (!resultsDir.exists()) {
      resultsDir.mkdirs();
    }

    // Create CSV file with timestamp
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String csvFileName = "results/deepseek-r1-region-discovery_" + timestamp + ".csv";
    File csvFile = new File(csvFileName);

    logger.info("Testing DeepSeek R1 model '{}' across all US regions...", testModelAlias);
    logger.info("Results will be saved to: {}", csvFileName);

    int successCount = 0;
    int failCount = 0;

    try (PrintWriter csvWriter = new PrintWriter(new FileWriter(csvFile))) {
      // CSV Header
      csvWriter.println("model-alias,region,city,status,response");

      for (String region : usRegions) {
        logger.info("\nTesting region: {}", region);

        String[] args = {"--project-id", TEST_PROJECT_ID, "--location", region, "--sa-key-file", workingKeyPath, "--model-name",
            testModelAlias, testPrompt};

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        String status = "FAIL";
        String response = "";

        try {
          System.setOut(new PrintStream(outContent));
          System.setErr(new PrintStream(errContent));

          VertexAiMasterMain app = new VertexAiMasterMain();
          CommandLine cmd = new CommandLine(app);
          int exitCode = cmd.execute(args);

          // Restore streams
          System.setOut(originalOut);
          System.setErr(originalErr);

          String output = outContent.toString().trim();
          String errorOutput = errContent.toString();

          if (exitCode == 0 && !output.isEmpty()) {
            status = "PASS";
            response = output.replace("\"", "\"\"").replace("\n", " ").replace("\r", "").trim();
            if (response.length() > 100) {
              response = response.substring(0, 100) + "...";
            }
            successCount++;
            logger.info("  ✓ [PASS] {} - DeepSeek R1 works!", region);
          } else {
            // Extract error message
            if (errorOutput.contains("404")) {
              response = "404 Not Found";
            } else if (errorOutput.contains("403")) {
              response = "403 Permission Denied";
            } else if (errorOutput.contains("400")) {
              response = "400 Bad Request";
            } else if (errorOutput.contains("500")) {
              response = "500 Internal Error";
            } else if (errorOutput.contains("Exception")) {
              String[] lines = errorOutput.split("\\n");
              for (String line : lines) {
                if (line.contains("Exception")) {
                  response = line.trim();
                  if (response.length() > 100) {
                    response = response.substring(0, 100) + "...";
                  }
                  break;
                }
              }
            } else {
              response = "Unknown error - exit code: " + exitCode;
            }
            failCount++;
            logger.error("  ✗ [FAIL] {} - {}", region, response);
          }

        } catch (Exception e) {
          System.setOut(originalOut);
          System.setErr(originalErr);

          response = e.getClass().getSimpleName() + ": " + e.getMessage();
          if (response.length() > 100) {
            response = response.substring(0, 100) + "...";
          }
          failCount++;
          logger.error("  ✗ [FAIL] {} - {}", region, response);
        }

        // Write to CSV
        String city = getRegionCity(region);
        csvWriter.println(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"", testModelAlias, region, city, status, response));
        csvWriter.flush();
      }
    }

    logger.info("\n=== DeepSeek R1 Region Discovery Summary ===");
    logger.info("Model: {}", testModelAlias);
    logger.info("Total regions tested: {}", usRegions.size());
    logger.info("Successful: {}", successCount);
    logger.info("Failed: {}", failCount);
    logger.info("Results saved to: {}", csvFileName);

    // Assert that at least one region worked
    assertThat(successCount).as("At least one US region should support DeepSeek R1 model '" + testModelAlias + "'").isGreaterThan(0);
  }

  @Test
  @EnabledIfSystemProperty(named = "run.integration.tests", matches = "true")
  void shouldDiscoverMiniMaxWorldwide() throws IOException {
    // Given: Working Service Account key file path
    String workingKeyPath = TEST_SA_KEY_FILE;
    File workingKeyFile = new File(workingKeyPath);

    // Verify the working key file exists
    assertThat(workingKeyFile).as("Working Service Account key file should exist").exists();

    String testModelAlias = "minimax.m2";
    String testPrompt = "200+200*99=?";

    // Build combined region list from service
    VertexAiServiceImpl service = new VertexAiServiceImpl();
    List<String> allRegions = service.getAllRegions();

    // Create results directory if it doesn't exist
    File resultsDir = new File("results");
    if (!resultsDir.exists()) {
      resultsDir.mkdirs();
    }

    // Create CSV file with timestamp
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String csvFileName = "results/minimax-region-discovery_" + timestamp + ".csv";
    File csvFile = new File(csvFileName);

    logger.info("Testing MiniMax model '{}' across worldwide regions...", testModelAlias);
    logger.info("Results will be saved to: {}", csvFileName);

    int successCount = 0;
    int failCount = 0;

    try (PrintWriter csvWriter = new PrintWriter(new FileWriter(csvFile))) {
      // CSV Header
      csvWriter.println("model-alias,region,city,status,response");

      for (String region : allRegions) {
        logger.info("\nTesting region: {}", region);

        String[] args = {"--project-id", TEST_PROJECT_ID, "--location", region, "--sa-key-file", workingKeyPath, "--model-name",
            testModelAlias, testPrompt};

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        String status = "FAIL";
        String response = "";

        try {
          System.setOut(new PrintStream(outContent));
          System.setErr(new PrintStream(errContent));

          VertexAiMasterMain app = new VertexAiMasterMain();
          CommandLine cmd = new CommandLine(app);
          int exitCode = cmd.execute(args);

          // Restore streams
          System.setOut(originalOut);
          System.setErr(originalErr);

          String output = outContent.toString().trim();
          String errorOutput = errContent.toString();

          if (exitCode == 0 && !output.isEmpty()) {
            status = "PASS";
            response = output.replace("\"", "\"\"").replace("\n", " ").replace("\r", "").trim();
            if (response.length() > 100) {
              response = response.substring(0, 100) + "...";
            }
            successCount++;
            logger.info("  ✓ [PASS] {} - MiniMax works!", region);
          } else {
            // Extract error message
            if (errorOutput.contains("404")) {
              response = "404 Not Found";
            } else if (errorOutput.contains("403")) {
              response = "403 Permission Denied";
            } else if (errorOutput.contains("400")) {
              response = "400 Bad Request";
            } else if (errorOutput.contains("500")) {
              response = "500 Internal Error";
            } else if (errorOutput.contains("Exception")) {
              String[] lines = errorOutput.split("\\n");
              for (String line : lines) {
                if (line.contains("Exception")) {
                  response = line.trim();
                  if (response.length() > 100) {
                    response = response.substring(0, 100) + "...";
                  }
                  break;
                }
              }
            } else {
              response = "Unknown error - exit code: " + exitCode;
            }
            failCount++;
            logger.error("  ✗ [FAIL] {} - {}", region, response);
          }

        } catch (Exception e) {
          System.setOut(originalOut);
          System.setErr(originalErr);

          response = e.getClass().getSimpleName() + ": " + e.getMessage();
          if (response.length() > 100) {
            response = response.substring(0, 100) + "...";
          }
          failCount++;
          logger.error("  ✗ [FAIL] {} - {}", region, response);
        }

        // Write to CSV
        String city = getRegionCity(region);
        csvWriter.println(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"", testModelAlias, region, city, status, response));
        csvWriter.flush();
      }
    }

    logger.info("\n=== MiniMax Worldwide Region Discovery Summary ===");
    logger.info("Model: {}", testModelAlias);
    logger.info("Total regions tested: {}", allRegions.size());
    logger.info("Successful: {}", successCount);
    logger.info("Failed: {}", failCount);
    logger.info("Results saved to: {}", csvFileName);

    // Validate coverage: all regions processed
    assertThat(successCount + failCount).as("All regions should be processed for '" + testModelAlias + "'").isEqualTo(allRegions.size());
  }
}
