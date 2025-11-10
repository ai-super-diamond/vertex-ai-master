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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "vertex-ai", mixinStandardHelpOptions = true, version = "0.0.1", description = "A CLI for interacting with the Vertex AI API.")
public class VertexAiMasterMain implements Callable<Integer> {
  private static final Logger logger = LoggerFactory.getLogger(VertexAiMasterMain.class);

  static class ApiKeyAuth {
    @Option(names = "--api-key", description = "Your Vertex AI API key.", required = true)
    String apiKey;
  }

  static class ServiceAccountAuth {
    @Option(names = "--project-id", description = "Your Google Cloud project ID.", required = true)
    String projectId;

    @Option(names = "--location", description = "The Google Cloud location (e.g., us-central1).", required = true)
    String location;

    @Option(names = "--sa-key-file", description = "Path to Service Account JSON key file.")
    String saKeyFile;
  }

  @ArgGroup(exclusive = true, multiplicity = "1")
  Auth auth;

  static class Auth {
    @ArgGroup(exclusive = false)
    ApiKeyAuth apiKeyAuth;

    @ArgGroup(exclusive = false)
    ServiceAccountAuth saAuth;
  }

  @Option(names = {"--model-name",
      "-m"}, description = "The name of the model to use.", defaultValue = "gemini-1.5-pro-001")
  private String modelName;

  @Option(names = {"--check-all-regions",
      "-car"}, description = "Check model availability across all regions in a cluster.")
  private boolean checkAllRegions;

  @Option(names = {"--cluster",
      "-c"}, description = "Region cluster to test (US, EU, ASIA, etc.). Used with --check-all-regions.")
  private String cluster;

  @Option(names = {"--worldwide",
      "-w"}, description = "Check model availability across all worldwide regions.")
  private boolean worldwide;

  @Option(names = {"--text", "-t"}, description = "The test prompt text (for region check mode).")
  private String textOption;

  @Parameters(index = "0", arity = "0..1", description = "The text prompt to send to the model.")
  private String text;

  private final VertexAiService vertexAiService = new VertexAiServiceImpl();

  @Override
  public Integer call() throws Exception {
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
    GenerationRequest request = GenerationRequest.builder().withAuthenticationConfig(authConfig)
        .withModelName(modelName).withText(prompt).build();

    // Generate content using service
    GenerationResult result = vertexAiService.generateContent(request);

    if (result.isSuccess()) {
      System.out.println(result.getContent());
      return 0;
    } else {
      logger.error("Error generating content: {}", result.getErrorMessage());
      return 1;
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
      logger.error(
          "Unknown cluster '{}'. Valid options: US, EU, ASIA, MIDDLE_EAST, AFRICA, CANADA, SOUTH_AMERICA",
          cluster);
      return 1;
    }

    String testPrompt = (textOption != null && !textOption.isEmpty())
        ? textOption
        : (text != null ? text : "200+200*99=?");
    System.out.println("\n=== Region Availability Check ===");
    System.out.println("Model: " + modelName);
    System.out.println("Cluster: " + cluster);
    System.out.println("Regions to test: " + regions.size());
    System.out.println("Test prompt: " + testPrompt);
    System.out.println("\nTesting...");

    // Create authentication config
    AuthenticationConfig authConfig = resolveServiceAccountAuthentication();
    if (authConfig == null) {
      throw new IllegalStateException(
          "Service account configuration is required for region availability checks.");
    }

    // Create region check request
    RegionCheckRequest request = RegionCheckRequest.builder().withAuthenticationConfig(authConfig)
        .withModelName(modelName).withCluster(cluster).withTestPrompt(testPrompt)
        .withRegions(regions).build();

    // Check region availability using service
    RegionCheckResult result = vertexAiService.checkRegionAvailability(request);

    // Display results
    int successCount = result.getSuccessCount();
    int failCount = result.getFailCount();

    System.out.println("\n=== Results ===");
    for (Map.Entry<String, String> entry : result.getRegionResults().entrySet()) {
      String region = entry.getKey();
      String status = entry.getValue();
      boolean success = "SUCCESS".equals(status);

      if (success) {
        System.out.println("✓ " + region + ": " + status);
      } else {
        System.err.println("✗ " + region + ": " + status);
      }
    }

    System.out.println("\n=== Summary ===");
    System.out.println("Total: " + result.getTotalCount());
    System.out.println("Success: " + successCount);
    System.out.println("Failed: " + failCount);

    return result.hasSuccess() ? 0 : 1;
  }

  private List<String> getRegionsForCluster(String clusterName) {
    return ((VertexAiServiceImpl) vertexAiService).getRegionsForCluster(clusterName);

  }

  private AuthenticationConfig resolveServiceAccountAuthentication() {
    if (auth.saAuth == null) {
      logger.error("Service account credentials are required for this operation.");
      return null;
    }

    boolean hasKeyFile = auth.saAuth.saKeyFile != null && !auth.saAuth.saKeyFile.isBlank();
    AuthenticationConfig.Builder builder = AuthenticationConfig.builder()
        .withProjectId(auth.saAuth.projectId).withLocation(auth.saAuth.location);

    if (hasKeyFile) {
      builder.withType(AuthenticationType.SERVICE_ACCOUNT_EXPLICIT_KEY)
          .withSaKeyFile(auth.saAuth.saKeyFile);
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
        return AuthenticationConfig.builder().withType(AuthenticationType.API_KEY)
            .withApiKey(auth.apiKeyAuth.apiKey).build();
      } else if (auth.saAuth != null) {
        if (auth.saAuth.saKeyFile != null && !auth.saAuth.saKeyFile.isEmpty()) {
          // Use explicit Service Account JSON key file
          return AuthenticationConfig.builder()
              .withType(AuthenticationType.SERVICE_ACCOUNT_EXPLICIT_KEY)
              .withProjectId(auth.saAuth.projectId).withLocation(auth.saAuth.location)
              .withSaKeyFile(auth.saAuth.saKeyFile).build();
        } else {
          // Fallback to ADC
          return AuthenticationConfig.builder().withType(AuthenticationType.SERVICE_ACCOUNT_ADC)
              .withProjectId(auth.saAuth.projectId).withLocation(auth.saAuth.location).build();
        }
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

    String testPrompt = (textOption != null && !textOption.isEmpty())
        ? textOption
        : (text != null ? text : "200+200*99=?");
    System.out.println("\n=== Worldwide Region Availability Check ===");
    System.out.println("Model: " + modelName);
    System.out.println("Test prompt: " + testPrompt);
    System.out.println("\nTesting...");

    // Create authentication config
    AuthenticationConfig authConfig = resolveServiceAccountAuthentication();
    if (authConfig == null) {
      throw new IllegalStateException(
          "Service account configuration is required for worldwide availability checks.");
    }

    // Create region check request
    RegionCheckRequest request = RegionCheckRequest.builder().withAuthenticationConfig(authConfig)
        .withModelName(modelName).withTestPrompt(testPrompt).build();

    // Check worldwide availability using the client
    WorldwideAvailabilityClient client = new WorldwideAvailabilityClient(vertexAiService);
    RegionCheckResult result = client.checkWorldwideAvailability(request);

    // Display results
    int successCount = result.getSuccessCount();
    int failCount = result.getFailCount();

    System.out.println("\n=== Results ===");
    for (Map.Entry<String, String> entry : result.getRegionResults().entrySet()) {
      String region = entry.getKey();
      String status = entry.getValue();
      boolean success = "SUCCESS".equals(status);

      if (success) {
        System.out.println("✓ " + region + ": " + status);
      } else {
        System.err.println("✗ " + region + ": " + status);
      }
    }

    System.out.println("\n=== Summary ===");
    System.out.println("Total: " + result.getTotalCount());
    System.out.println("Success: " + successCount);
    System.out.println("Failed: " + failCount);

    return result.hasSuccess() ? 0 : 1;
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new VertexAiMasterMain()).execute(args);
    System.exit(exitCode);
  }
}
