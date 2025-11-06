package com.example.demo;

import com.jguru.vertexai.VertexAiMasterMain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

class VertexAiMasterMainTest {

  @Test
  void simpleTest() {
    assertThat(true).isTrue();
  }

  @Test
  @EnabledIfSystemProperty(named = "run.integration.tests", matches = "true")
  void testVertexAiWithServiceAccountKey() {
    // Given: Service Account key file path
    String saKeyPath = "c:\\java\\backup\\GCP\\Vertex\\skorec.json";
    File saKeyFile = new File(saKeyPath);

    // Verify the SA key file exists
    assertThat(saKeyFile).as("Service Account key file should exist").exists();

    // Given: CLI arguments
    String[] args = {"--project-id", "vertex-ai-project-skorec", "--location", "us-central1",
        "--sa-key-file", saKeyPath, "--model-name", "gemini.pro",
        "What is 2+2? Answer in one word."};

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

      // Verify model alias resolution in stderr
      assertThat(errorOutput).as("Should resolve gemini.pro alias")
          .contains("[INFO] Resolved model alias 'gemini.pro'");

      // Verify response is not empty
      assertThat(output.trim()).as("Response should not be empty").isNotEmpty();

      System.out.println("[TEST] Response received: " + output.trim());

    } finally {
      // Restore original streams FIRST
      System.setOut(originalOut);
      System.setErr(originalErr);
    }

    // Print captured output for debugging (after streams are restored)
    if (outContent.size() > 0) {
      System.out.println("=== CLI Output ===");
      System.out.println(outContent.toString());
    }
    if (errContent.size() > 0) {
      System.err.println("=== CLI Error Output ===");
      System.err.println(errContent.toString());
    }
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
    String[] args = {"--project-id", "vertex-ai-project-skorec", "--location", "us-central1",
        "--sa-key-file", expiredKeyPath, "--model-name", "gemini.pro", "Test prompt"};

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
      assertThat(exitCode).as(
          "CLI must fail with non-zero exit code when expired key is provided - ADC fallback must not occur")
          .isNotEqualTo(0);

    } finally {
      // Restore original streams FIRST
      System.setOut(originalOut);
      System.setErr(originalErr);
    }

    String output = outContent.toString();
    String errorOutput = errContent.toString();

    // Print output for debugging (after stream is restored)
    System.err.println("=== Error Output (Expired Key Test) ===");
    System.err.println(errorOutput);
    if (output.length() > 0) {
      System.out.println("=== Output (Expired Key Test) ===");
      System.out.println(output);
    }

    // Verify error message indicates authentication/token failure (not ADC fallback)
    assertThat(errorOutput)
        .as("Error output must indicate authentication/token failure (no ADC fallback)")
        .containsAnyOf("TokenResponseException", "invalid_grant", "401", "403", "expired",
            "unauthorized", "Permission", "denied", "access denied", "authentication",
            "credentials", "Failed to load service account key", "ADC fallback is disabled");
  }
}
