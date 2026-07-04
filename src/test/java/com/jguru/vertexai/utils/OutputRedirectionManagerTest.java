package com.jguru.vertexai.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OutputRedirectionManagerTest {

  private OutputRedirectionManager manager;
  private PrintStream originalOut;
  private PrintStream originalErr;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    manager = new OutputRedirectionManager();
    originalOut = System.out;
    originalErr = System.err;
  }

  @AfterEach
  void tearDown() {
    manager.closeOutputRedirection();
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  @Test
  void shouldNotRedirectWhenNoOutputFileAndNoCapture() throws Exception {
    manager.setupOutputRedirection(null, false);

    PrintStream currentOut = System.out;
    assertThat(currentOut).isSameAs(originalOut);
  }

  @Test
  void shouldCreateOutputFileWhenCaptureRequested() throws Exception {
    manager.setupOutputRedirection(null, true);

    PrintStream currentOut = System.out;
    assertThat(currentOut).isNotSameAs(originalOut);

    manager.closeOutputRedirection();

    Path resultsDir = Paths.get("results");
    assertThat(Files.exists(resultsDir)).isTrue();
  }

  @Test
  void shouldRedirectToSpecifiedFile() throws Exception {
    String outputFile = tempDir.resolve("test-output.txt").toString();

    manager.setupOutputRedirection(outputFile, false);

    PrintStream currentOut = System.out;
    assertThat(currentOut).isNotSameAs(originalOut);

    System.out.println("Test output");
    manager.closeOutputRedirection();

    Path outputPath = Paths.get(outputFile);
    assertThat(Files.exists(outputPath)).isTrue();
    String content = Files.readString(outputPath);
    assertThat(content).contains("Test output");
  }

  @Test
  void shouldRestoreOriginalStreamsAfterClose() throws Exception {
    String outputFile = tempDir.resolve("test-output.txt").toString();

    manager.setupOutputRedirection(outputFile, false);
    assertThat(System.out).isNotSameAs(originalOut);

    manager.closeOutputRedirection();
    assertThat(System.out).isSameAs(originalOut);
    assertThat(System.err).isSameAs(originalErr);
  }

  @Test
  void shouldCreateParentDirectoriesIfNeeded() throws Exception {
    String outputFile = tempDir.resolve("subdir/nested/output.txt").toString();

    manager.setupOutputRedirection(outputFile, false);

    System.out.println("Test");
    manager.closeOutputRedirection();

    Path outputPath = Paths.get(outputFile);
    assertThat(Files.exists(outputPath)).isTrue();
    assertThat(Files.exists(outputPath.getParent())).isTrue();
  }

  @Test
  void shouldHandleMultipleCloseCallsGracefully() throws Exception {
    String outputFile = tempDir.resolve("test.txt").toString();

    manager.setupOutputRedirection(outputFile, false);
    manager.closeOutputRedirection();
    manager.closeOutputRedirection();

    assertThat(System.out).isSameAs(originalOut);
  }
}
