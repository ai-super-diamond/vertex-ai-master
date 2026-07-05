package com.jguru.vertexai;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BinScriptsValidationTest {

  private static final Path REPO_ROOT = Paths.get("").toAbsolutePath().normalize();
  private static final Path BIN_DIR = REPO_ROOT.resolve("bin");
  private static final Map<String, String> CMD_ONLY_EXCEPTIONS = Map.of("build-exe",
      "Native-image build helper is intentionally Windows-only.");
  private static final List<String> DOCUMENTATION_FILES = List.of("README.md", "QUICK START GUIDE.md", "QUICK START GUIDE- ADC.md",
      "AGENTS.md");
  private static final Pattern SCRIPT_NAME_PATTERN = Pattern.compile("\\b[a-z0-9-]+\\.(?:cmd|sh)\\b");

  @Test
  void shouldKeepCmdAndShScriptBasenamesInSync() throws IOException {
    Set<String> cmdBasenames = basenamesFor(".cmd");
    Set<String> shBasenames = basenamesFor(".sh");

    assertThat(cmdBasenames).isNotEmpty();
    assertThat(shBasenames).isNotEmpty();
    assertThat(difference(cmdBasenames, shBasenames)).containsExactlyElementsOf(new TreeSet<>(CMD_ONLY_EXCEPTIONS.keySet()));
    assertThat(difference(shBasenames, cmdBasenames)).isEmpty();
  }

  @Test
  void shouldUseScriptRelativePathsForRuntimeFiles() throws IOException {
    assertThat(Files.exists(BIN_DIR.resolve("models.properties"))).isTrue();
    assertThat(Files.exists(BIN_DIR.resolve("regions.properties"))).isTrue();
    assertThat(Files.exists(REPO_ROOT.resolve("src/main/resources/models.properties"))).isTrue();
    assertThat(Files.exists(REPO_ROOT.resolve("src/main/resources/regions.properties"))).isTrue();

    for (Path script : binScripts()) {
      String content = read(script);

      if (content.contains("vertex-latest.jar")) {
        assertHasScriptRelativePath(script, content, "vertex-latest.jar");
      }
      if (content.contains("--model-file")) {
        assertHasScriptRelativePath(script, content, "models.properties");
      }
      if (content.contains("--regions-file")) {
        assertHasScriptRelativePath(script, content, "regions.properties");
      }
      if (content.contains("--sa-key-file") && !basename(script.getFileName().toString()).startsWith("doctor")) {
        assertHasScriptRelativeKeyPath(script, content);
      }

      assertNoBareRuntimePaths(script, content);
    }
  }

  @Test
  void shouldMatchScriptNamesToExpectedCliFlags() throws IOException {
    for (Path script : binScripts()) {
      String fileName = script.getFileName().toString();
      String baseName = basename(fileName);
      String content = read(script);

      if (baseName.startsWith("build-") || baseName.startsWith("doctor")) {
        continue;
      }

      switch (baseName) {
        case "cli-help" -> {
          assertContainsAll(fileName, content, "java -jar", "vertex-latest.jar", "--help");
          assertContainsNone(fileName, content, "--debug", "--adc", "--sa-key-file", "--check-all-regions", "--worldwide", "--model-file",
              "--regions-file");
        }
        case "debug-all-us" -> {
          assertContainsAll(fileName, content, "--sa-key-file", "--check-all-regions", "--cluster US", "--model-file", "--text", "--debug");
          assertContainsNone(fileName, content, "--adc", "--project", "--cluster EU", "--cluster GLOBAL", "--worldwide");
        }
        case "debug-all-eu" -> {
          assertContainsAll(fileName, content, "--sa-key-file", "--check-all-regions", "--cluster EU", "--model-file", "--text", "--debug");
          assertContainsNone(fileName, content, "--adc", "--project", "--cluster US", "--cluster GLOBAL", "--worldwide");
        }
        case "test-all-us" -> {
          assertContainsAll(fileName, content, "--sa-key-file", "--check-all-regions", "--cluster US", "--model-file", "--text");
          assertContainsNone(fileName, content, "--debug", "--adc", "--project", "--cluster EU", "--cluster GLOBAL", "--worldwide");
        }
        case "test-all-eu" -> {
          assertContainsAll(fileName, content, "--sa-key-file", "--check-all-regions", "--cluster EU", "--model-file", "--text");
          assertContainsNone(fileName, content, "--debug", "--adc", "--project", "--cluster US", "--cluster GLOBAL", "--worldwide");
        }
        case "test-all-eu-adc" -> {
          assertContainsAll(fileName, content, "--adc", "--project", "--check-all-regions", "--cluster EU", "--model-file", "--text");
          assertContainsNone(fileName, content, "--debug", "--sa-key-file", "--cluster US", "--cluster GLOBAL", "--worldwide");
        }
        case "test-global" -> {
          assertContainsAll(fileName, content, "--sa-key-file", "--check-all-regions", "--cluster GLOBAL", "--model-file", "--text");
          assertContainsNone(fileName, content, "--debug", "--adc", "--project", "--cluster US", "--cluster EU", "--worldwide");
        }
        case "test-worldwide" -> {
          assertContainsAll(fileName, content, "--sa-key-file", "--worldwide", "--model-file", "--regions-file", "--text");
          assertContainsNone(fileName, content, "--debug", "--adc", "--project", "--check-all-regions", "--cluster US", "--cluster EU",
              "--cluster GLOBAL");
        }
        case "test-enterprise-eu" -> {
          assertContainsAll(fileName, content, "--sa-key-file", "--location eu", "--model-file", "--text");
          assertContainsNone(fileName, content, "--debug", "--adc", "--project", "--adc-location", "--check-all-regions", "--worldwide",
              "--cluster ");
        }
        case "test-enterprise-eu-adc" -> {
          assertContainsAll(fileName, content, "--adc", "--project", "--adc-location eu", "--model-file", "--text");
          assertContainsNone(fileName, content, "--debug", "--sa-key-file", "--location eu", "--check-all-regions", "--worldwide",
              "--cluster ");
        }
        default -> assertThat(baseName).as("Unhandled script contract for %s", fileName).isEmpty();
      }
    }
  }

  @Test
  void shouldUseStableBinJarForRunnableScripts() throws IOException {
    for (Path script : binScripts()) {
      String fileName = script.getFileName().toString();
      if (fileName.startsWith("build-jar.") || fileName.startsWith("build-exe.") || fileName.startsWith("doctor.")) {
        continue;
      }

      String content = read(script);
      assertHasScriptRelativePath(script, content, "vertex-latest.jar");
      assertThat(content).doesNotContain("vertex-1.0.1.jar");
      assertThat(content).doesNotContainPattern("vertex-\\d+\\.\\d+\\.\\d+\\.jar");
    }
  }

  @Test
  void shouldValidateBuildScriptsAgainstPackagedArtifactContract() throws IOException {
    String buildJarCmd = read(BIN_DIR.resolve("build-jar.cmd"));
    assertThat(buildJarCmd).contains("mvn clean package -DskipTests");
    assertThat(buildJarCmd).contains("target\\vertex-*.jar");
    assertThat(buildJarCmd).contains("dependency-reduced");
    assertThat(buildJarCmd).contains("copy /Y \"%JAR_FILE%\" \"%SCRIPT_DIR%\\vertex-latest.jar\"");
    assertThat(buildJarCmd).contains("if errorlevel 1 (");

    String buildJarSh = read(BIN_DIR.resolve("build-jar.sh"));
    assertThat(buildJarSh).contains("mvn clean package -DskipTests");
    assertThat(buildJarSh).contains("vertex-*.jar");
    assertThat(buildJarSh).contains("dependency-reduced");
    assertThat(buildJarSh).contains("$SCRIPT_DIR/vertex-latest.jar");
    assertThat(buildJarSh).contains("if [ $? -ne 0 ]; then");

    String buildExeCmd = read(BIN_DIR.resolve("build-exe.cmd"));
    assertThat(buildExeCmd).contains("mvn -Pnative package");
    assertThat(buildExeCmd).contains("if defined GRAALVM_HOME set JAVA_HOME=%GRAALVM_HOME%");
    assertThat(buildExeCmd).contains("vertex.exe");
  }

  @Test
  void shouldAvoidUnsafeBatchQuotingPatterns() throws IOException {
    for (Path script : binScripts()) {
      if (!script.getFileName().toString().endsWith(".cmd")) {
        continue;
      }

      String fileName = script.getFileName().toString();
      String content = read(script);
      assertContainsNone(fileName, content, "--sa-key-file %KEY%", "--model-file %MODELS_FILE%", "--regions-file %REGIONS_FILE%",
          "--project %PROJECT%");
    }
  }

  @Test
  void shouldPauseAtEndOfWindowsScripts() throws IOException {
    for (Path script : binScripts()) {
      if (!script.getFileName().toString().endsWith(".cmd")) {
        continue;
      }

      String fileName = script.getFileName().toString();
      String content = read(script);
      assertThat(content).as("%s should pause before the script window closes", fileName).contains("pause");
    }
  }

  @Test
  void shouldPauseAtEndOfShellScripts() throws IOException {
    for (Path script : binScripts()) {
      if (!script.getFileName().toString().endsWith(".sh")) {
        continue;
      }

      String fileName = script.getFileName().toString();
      String content = read(script);
      assertThat(content).as("%s should prompt before the shell exits", fileName)
          .contains("read -r -p \"Press Enter to continue . . .\" _");
    }
  }

  @Test
  void shouldPropagateExitCodesForEnterpriseScripts() throws IOException {
    String enterpriseCmd = read(BIN_DIR.resolve("test-enterprise-eu.cmd"));
    assertThat(enterpriseCmd).contains("set EXIT_CODE=%ERRORLEVEL%", "exit /b %EXIT_CODE%");

    String enterpriseAdcCmd = read(BIN_DIR.resolve("test-enterprise-eu-adc.cmd"));
    assertThat(enterpriseAdcCmd).contains("set EXIT_CODE=%ERRORLEVEL%", "exit /b %EXIT_CODE%");

    String enterpriseSh = read(BIN_DIR.resolve("test-enterprise-eu.sh"));
    assertThat(enterpriseSh).contains("exit_code=$?", "exit \"$exit_code\"");

    String enterpriseAdcSh = read(BIN_DIR.resolve("test-enterprise-eu-adc.sh"));
    assertThat(enterpriseAdcSh).contains("exit_code=$?", "exit \"$exit_code\"");
  }

  @Test
  void shouldParseShellScriptsWhenBashIsAvailable() throws IOException {
    Assumptions.assumeTrue(isBashAvailable(), "bash is not available on PATH");

    for (Path script : binScripts()) {
      if (!script.getFileName().toString().endsWith(".sh")) {
        continue;
      }

      String scriptPath = REPO_ROOT.relativize(script).toString().replace('\\', '/');
      ProcessResult result = runCommand(List.of("bash", "-n", scriptPath));
      assertThat(result.exitCode).as("bash -n should succeed for %s. Output:%n%s", script.getFileName(), result.output).isZero();
    }
  }

  @Test
  void shouldKeepDocumentedScriptNamesPointingAtExistingFiles() throws IOException {
    Set<String> existingScriptNames = binScripts().stream().map(path -> path.getFileName().toString())
        .collect(Collectors.toCollection(TreeSet::new));

    for (String doc : DOCUMENTATION_FILES) {
      String content = read(REPO_ROOT.resolve(doc));
      Matcher matcher = SCRIPT_NAME_PATTERN.matcher(content);
      Set<String> referencedScriptNames = new TreeSet<>();
      while (matcher.find()) {
        referencedScriptNames.add(matcher.group());
      }

      assertThat(existingScriptNames).as("Documented script names should exist for %s", doc).containsAll(referencedScriptNames);
    }
  }

  @Test
  void shouldStageRuntimePropertyFilesIntoBinDuringPackage() throws IOException {
    String pom = read(REPO_ROOT.resolve("pom.xml"));

    assertThat(pom).contains("<artifactId>maven-resources-plugin</artifactId>");
    assertThat(pom).contains("<outputDirectory>${project.basedir}/bin</outputDirectory>");
    assertThat(pom).contains("<include>models.properties</include>");
    assertThat(pom).contains("<include>regions.properties</include>");
  }

  private static void assertContainsAll(String fileName, String content, String... snippets) {
    assertThat(content).as("%s should contain the expected CLI flags", fileName).contains(snippets);
  }

  private static void assertContainsNone(String fileName, String content, String... snippets) {
    assertThat(content).as("%s should not contain unexpected CLI flags or interactive behavior", fileName).doesNotContain(snippets);
  }

  private static void assertNoBareRuntimePaths(Path script, String content) {
    String fileName = script.getFileName().toString();
    if (fileName.endsWith(".cmd")) {
      assertContainsNone(fileName, content, "set MODELS_FILE=models.properties", "set REGIONS_FILE=regions.properties",
          "set KEY=..\\keys\\sa_key.json");
      return;
    }

    assertContainsNone(fileName, content, "MODELS_FILE=models.properties", "REGIONS_FILE=regions.properties", "KEY=../keys/sa_key.json",
        "read -n 1 -s -r -p");
  }

  private static void assertHasScriptRelativePath(Path script, String content, String fileName) {
    if (script.getFileName().toString().endsWith(".cmd")) {
      assertThat(content).as("%s should use a script-relative path for %s", script.getFileName(), fileName)
          .containsAnyOf("%~dp0" + fileName, "%SCRIPT_DIR%\\" + fileName);
      return;
    }

    assertThat(content).as("%s should use a script-relative path for %s", script.getFileName(), fileName)
        .contains("$SCRIPT_DIR/" + fileName);
  }

  private static void assertHasScriptRelativeKeyPath(Path script, String content) {
    if (script.getFileName().toString().endsWith(".cmd")) {
      assertThat(content).as("%s should use a script-relative service account key path", script.getFileName())
          .containsAnyOf("%~dp0..\\keys\\sa_key.json", "%SCRIPT_DIR%\\..\\keys\\sa_key.json");
      return;
    }

    assertThat(content).as("%s should use a script-relative service account key path", script.getFileName())
        .contains("$SCRIPT_DIR/../keys/sa_key.json");
  }

  private static String basename(String fileName) {
    int extensionIndex = fileName.lastIndexOf('.');
    return extensionIndex >= 0 ? fileName.substring(0, extensionIndex) : fileName;
  }

  private static Set<String> basenamesFor(String extension) throws IOException {
    return binScripts().stream().map(path -> path.getFileName().toString()).filter(name -> name.endsWith(extension))
        .map(name -> name.substring(0, name.length() - extension.length())).collect(Collectors.toCollection(TreeSet::new));
  }

  private static Set<String> difference(Set<String> left, Set<String> right) {
    Set<String> difference = new TreeSet<>(left);
    difference.removeAll(right);
    return difference;
  }

  private static List<Path> binScripts() throws IOException {
    try (Stream<Path> paths = Files.list(BIN_DIR)) {
      return paths.filter(Files::isRegularFile).filter(path -> {
        String name = path.getFileName().toString();
        return name.endsWith(".cmd") || name.endsWith(".sh");
      }).sorted().toList();
    }
  }

  private static boolean isBashAvailable() {
    try {
      return runCommand(List.of("bash", "--version")).exitCode == 0;
    } catch (IOException exception) {
      return false;
    }
  }

  private static ProcessResult runCommand(List<String> command) throws IOException {
    Process process = new ProcessBuilder(command).directory(REPO_ROOT.toFile()).redirectErrorStream(true).start();

    try {
      int exitCode = process.waitFor();
      String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      return new ProcessResult(exitCode, output);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while running command: " + String.join(" ", command), exception);
    }
  }

  private static String read(Path path) throws IOException {
    return Files.readString(path).replace("\r\n", "\n");
  }

  private record ProcessResult(int exitCode, String output) {
  }
}
