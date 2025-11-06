package com.jguru.vertexai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

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

  @Test
  @EnabledIfSystemProperty(named = "run.integration.tests", matches = "true")
  void shouldAllModelsPass() {
    // Given: Working Service Account key file path
    String workingKeyPath = "keys\\working.json";
    File workingKeyFile = new File(workingKeyPath);

    // Verify the working key file exists
    assertThat(workingKeyFile).as("Working Service Account key file should exist").exists();

    // All model aliases from models.properties
    List<String> modelAliases = Arrays.asList("gemini.pro", "openai.gpt.oss.120b", "llama.3_3.70b",
        "llama.4.maverick.17b.128e", "llama.4.scout.17b.16e", "llama.3_2.90b.vision",
        "llama.3_1.405b", "llama.3_1.70b", "deepseek.r1.0528", "deepseek.ocr", "qwen3.235b.a22b",
        "qwen3.coder.480b.a35b", "qwen3.next.80b.a3b", "qwen3.next.80b.a3b.thinking", "minimax.m2",
        "codestral.2");

    String testPrompt = "200+200*99=?";

    // Test each model
    for (String modelAlias : modelAliases) {
      System.out.println("\n=== Testing model: " + modelAlias + " ===");

      String[] args = {"--project-id", "vertex-ai-project-skorec", "--location", "us-central1",
          "--sa-key-file", workingKeyPath, "--model-name", modelAlias, testPrompt};

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

        // Restore streams immediately to print results
        System.setOut(originalOut);
        System.setErr(originalErr);

        String output = outContent.toString().trim();
        String errorOutput = errContent.toString();

        // Then: Verify successful execution
        assertThat(exitCode).as("Model " + modelAlias + " should execute successfully")
            .isEqualTo(0);

        // Verify response is not empty
        assertThat(output).as("Model " + modelAlias + " should return a response").isNotEmpty();

        // Print results
        System.out.println("[PASS] " + modelAlias + " - Response: " + output);
        if (errorOutput.contains("[INFO]")) {
          System.out.println("       Model resolved: "
              + errorOutput.substring(errorOutput.indexOf("[INFO]")).split("\\n")[0]);
        }

      } catch (Exception e) {
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.err.println("[FAIL] " + modelAlias + " - Error: " + e.getMessage());
        throw new AssertionError("Model " + modelAlias + " failed", e);
      }
    }

    System.out.println("\n=== All " + modelAliases.size() + " models passed! ===");
  }
}
