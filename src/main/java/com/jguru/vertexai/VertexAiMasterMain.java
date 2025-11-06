package com.jguru.vertexai;

import com.jguru.vertexai.utils.VertexUtils;
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

  @Option(names = {"--text", "-t"}, description = "The test prompt text (for region check mode).")
  private String textOption;

  @Parameters(index = "0", arity = "0..1", description = "The text prompt to send to the model.")
  private String text;

  @Override
  public Integer call() throws Exception {
    // Check if we're in region check mode
    if (checkAllRegions) {
      return performRegionCheck();
    }

    // Normal mode: generate content
    String prompt = textOption != null ? textOption : text;
    if (prompt == null || prompt.isEmpty()) {
      System.err.println("Error: No prompt text provided.");
      return 1;
    }

    String response;
    if (auth.apiKeyAuth != null) {
      response = VertexUtils.generateContent(auth.apiKeyAuth.apiKey, modelName, prompt);
    } else if (auth.saAuth != null) {
      if (auth.saAuth.saKeyFile != null && !auth.saAuth.saKeyFile.isEmpty()) {
        // Use explicit Service Account JSON key file
        response = VertexUtils.generateContent(auth.saAuth.saKeyFile, auth.saAuth.projectId,
            auth.saAuth.location, modelName, prompt);
      } else {
        // Fallback to ADC
        response = VertexUtils.generateContent(auth.saAuth.projectId, auth.saAuth.location,
            modelName, prompt);
      }
    } else {
      System.err.println("Error: Please provide either API key or Service Account credentials.");
      return 1;
    }

    System.out.println(response);
    return 0;
  }

  private Integer performRegionCheck() {
    if (auth.saAuth == null) {
      System.err.println("Error: Region check requires Service Account authentication.");
      return 1;
    }

    if (cluster == null || cluster.isEmpty()) {
      System.err.println("Error: --cluster (-c) is required with --check-all-regions.");
      return 1;
    }

    // Get regions for the specified cluster
    List<String> regions = getRegionsForCluster(cluster);
    if (regions == null || regions.isEmpty()) {
      System.err.println("Error: Unknown cluster '" + cluster
          + "'. Valid options: US, EU, ASIA, MIDDLE_EAST, AFRICA, CANADA, SOUTH_AMERICA");
      return 1;
    }

    String testPrompt = (textOption != null && !textOption.isEmpty())
        ? textOption
        : (text != null ? text : "200+200*99=?");
    String saKeyFile = auth.saAuth.saKeyFile != null ? auth.saAuth.saKeyFile : null;

    System.out.println("\n=== Region Availability Check ===");
    System.out.println("Model: " + modelName);
    System.out.println("Cluster: " + cluster);
    System.out.println("Regions to test: " + regions.size());
    System.out.println("Test prompt: " + testPrompt);
    System.out.println("\nTesting...");

    Map<String, String> results = VertexUtils.checkConnectivityAvailability(saKeyFile,
        auth.saAuth.projectId, modelName, regions, testPrompt);

    // Display results
    int successCount = 0;
    int failCount = 0;

    System.out.println("\n=== Results ===");
    for (Map.Entry<String, String> entry : results.entrySet()) {
      String region = entry.getKey();
      String result = entry.getValue();
      boolean success = "SUCCESS".equals(result);

      if (success) {
        System.out.println("✓ " + region + ": " + result);
        successCount++;
      } else {
        System.err.println("✗ " + region + ": " + result);
        failCount++;
      }
    }

    System.out.println("\n=== Summary ===");
    System.out.println("Total: " + regions.size());
    System.out.println("Success: " + successCount);
    System.out.println("Failed: " + failCount);

    return successCount > 0 ? 0 : 1;
  }

  private List<String> getRegionsForCluster(String clusterName) {
    String upperCluster = clusterName.toUpperCase();
    switch (upperCluster) {
      case "US" :
      case "USA" :
        return VertexUtils.US_REGIONS;
      case "EU" :
      case "EUROPE" :
        return VertexUtils.EUROPE_REGIONS;
      case "ASIA" :
      case "APAC" :
      case "ASIA_PACIFIC" :
        return VertexUtils.ASIA_REGIONS;
      case "MIDDLE_EAST" :
      case "ME" :
        return VertexUtils.MIDDLE_EAST_REGIONS;
      case "AFRICA" :
        return VertexUtils.AFRICA_REGIONS;
      case "CANADA" :
      case "CA" :
        return VertexUtils.CANADA_REGIONS;
      case "SOUTH_AMERICA" :
      case "SA" :
        return VertexUtils.SOUTH_AMERICA_REGIONS;
      default :
        return null;
    }
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new VertexAiMasterMain()).execute(args);
    System.exit(exitCode);
  }
}
