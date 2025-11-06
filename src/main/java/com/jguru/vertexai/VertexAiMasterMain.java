package com.jguru.vertexai;

import com.jguru.vertexai.utils.VertexUtils;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

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

  @Option(names = "--model-name", description = "The name of the model to use.", defaultValue = "gemini-1.5-pro-001")
  private String modelName;

  @Parameters(index = "0", description = "The text prompt to send to the model.")
  private String text;

  @Override
  public Integer call() throws Exception {
    String response;
    if (auth.apiKeyAuth != null) {
      response = VertexUtils.generateContent(auth.apiKeyAuth.apiKey, modelName, text);
    } else if (auth.saAuth != null) {
      if (auth.saAuth.saKeyFile != null && !auth.saAuth.saKeyFile.isEmpty()) {
        // Use explicit Service Account JSON key file
        response = VertexUtils.generateContent(auth.saAuth.saKeyFile, auth.saAuth.projectId,
            auth.saAuth.location, modelName, text);
      } else {
        // Fallback to ADC
        response = VertexUtils.generateContent(auth.saAuth.projectId, auth.saAuth.location,
            modelName, text);
      }
    } else {
      System.err.println("Error: Please provide either API key or Service Account credentials.");
      return 1;
    }

    System.out.println(response);
    return 0;
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new VertexAiMasterMain()).execute(args);
    System.exit(exitCode);
  }
}
