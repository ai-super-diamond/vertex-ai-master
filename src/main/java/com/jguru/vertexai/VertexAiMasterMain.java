package com.jguru.vertexai;

import com.jguru.vertexai.service.ModelClientFactory;
import com.jguru.vertexai.service.AuthenticationConfigFactory;
import com.jguru.vertexai.service.RegionCheckUseCase;
import com.jguru.vertexai.service.RegionProvider;
import com.jguru.vertexai.service.RegionProviderImpl;
import com.jguru.vertexai.service.VertexAiService;
import com.jguru.vertexai.service.VertexAiServiceImpl;
import com.jguru.vertexai.service.WorldwideCheckUseCase;
import com.jguru.vertexai.domain.dto.AuthenticationConfig;
import com.jguru.vertexai.service.dto.GenerationRequest;
import com.jguru.vertexai.domain.dto.GenerationResult;
import com.jguru.vertexai.service.dto.RegionCheckResult;
import com.jguru.vertexai.utils.OutputRedirectionManager;
import com.jguru.vertexai.utils.PropertiesLoader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

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

    @Option(names = {"-regions-file"}, description = "Override default region configuration (.properties).")
    String regionsFile;
  }

  @ArgGroup
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

  private VertexAiService vertexAiService;
  private RegionProvider regionProvider;
  private final AuthenticationConfigFactory authConfigFactory;
  private final OutputRedirectionManager outputManager;

  public VertexAiMasterMain() {
    this(null, null, new AuthenticationConfigFactory(), new OutputRedirectionManager());
  }

  public VertexAiMasterMain(VertexAiService vertexAiService, RegionProvider regionProvider, AuthenticationConfigFactory authConfigFactory,
      OutputRedirectionManager outputManager) {
    this.vertexAiService = vertexAiService;
    this.regionProvider = regionProvider;
    this.authConfigFactory = authConfigFactory;
    this.outputManager = outputManager;
  }

  private static RegionProvider createDefaultRegionProvider() {
    Properties regionProperties = PropertiesLoader.load(LoggerFactory.getLogger(RegionProviderImpl.class), "regions.config",
        "regions.properties");
    return new RegionProviderImpl(regionProperties);
  }

  private static VertexAiService createDefaultVertexAiService(RegionProvider regionProvider) {
    ModelClientFactory modelClientFactory = new com.jguru.vertexai.infrastructure.client.VertexAiClientFactory();
    Properties modelProperties = PropertiesLoader.load(LoggerFactory.getLogger(VertexAiServiceImpl.class), "models.config",
        "models.properties");
    return new VertexAiServiceImpl(regionProvider, modelClientFactory, modelProperties);
  }

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
    return (modelSource != null && modelSource.modelFile != null && !modelSource.modelFile.isBlank());
  }

  private String getModelFile() {
    return (modelSource != null && modelSource.modelFile != null) ? modelSource.modelFile : null;
  }

  @Override
  public Integer call() throws Exception {
    if (modelSource != null && modelSource.modelFile != null && !modelSource.modelFile.isBlank()) {
      System.setProperty("models.config", modelSource.modelFile);
    }

    if (modelSource != null && modelSource.regionsFile != null && !modelSource.regionsFile.isBlank()) {
      System.setProperty("regions.config", modelSource.regionsFile);
    } else if (java.nio.file.Files.exists(java.nio.file.Paths.get("regions.properties"))) {
      System.setProperty("regions.config", "regions.properties");
    }

    if (this.regionProvider == null) {
      this.regionProvider = createDefaultRegionProvider();
    }
    if (this.vertexAiService == null) {
      this.vertexAiService = createDefaultVertexAiService(this.regionProvider);
    }

    outputManager.setupOutputRedirection(outputFile, debug, checkAllRegions, worldwide);

    try {
      if (checkAllRegions) {
        return performRegionCheck();
      }

      if (worldwide) {
        return performWorldwideCheck();
      }

      return performNormalGeneration();
    } finally {
      outputManager.closeOutputRedirection();
    }
  }

  private Integer performNormalGeneration() throws Exception {
    String prompt = textOption != null ? textOption : text;
    if (prompt == null || prompt.isEmpty()) {
      logger.error("No prompt text provided.");
      return 1;
    }

    AuthenticationConfig authConfig = createAuthenticationConfig();
    if (authConfig == null) {
      return 1;
    }

    GenerationRequest request = GenerationRequest.builder().withAuthenticationConfig(authConfig).withModelName(getEffectiveModelName())
        .withText(prompt).build();

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

    List<String> regions = regionProvider.getRegionsForCluster(cluster);
    if (regions == null || regions.isEmpty()) {
      logger.error("Unknown cluster '{}'. Valid options: US, EU, ASIA, MIDDLE_EAST, AFRICA, CANADA, SOUTH_AMERICA", cluster);
      return 1;
    }

    String testPrompt = (textOption != null && !textOption.isEmpty()) ? textOption : (text != null ? text : "200+200*99=?");
    AuthenticationConfig authConfig = createAuthenticationConfig();
    if (authConfig == null) {
      throw new IllegalStateException("Service account configuration is required for region availability checks.");
    }

    RegionCheckUseCase useCase = new RegionCheckUseCase(vertexAiService);

    if (isTestingAllModels()) {
      return performAllModelsCheck(useCase, authConfig, regions, testPrompt);
    }

    RegionCheckResult result = useCase.execute(authConfig, getEffectiveModelName(), cluster, regions, testPrompt, debug);
    return result.hasSuccess() ? 0 : 1;
  }

  private Integer performAllModelsCheck(RegionCheckUseCase useCase, AuthenticationConfig authConfig, List<String> regions,
      String testPrompt) throws Exception {
    String modelFile = getModelFile();
    System.setProperty("models.config", modelFile);

    Properties modelProperties = loadModelProperties(modelFile);
    if (modelProperties == null) {
      return 1;
    }

    return useCase.executeAllModels(authConfig, cluster, regions, testPrompt, debug, modelFile, modelProperties);
  }

  private Properties loadModelProperties(String modelFile) {
    Properties props = new Properties();
    try (FileInputStream fis = new FileInputStream(modelFile)) {
      props.load(fis);
      return props;
    } catch (IOException e) {
      logger.error("Failed to load model file: {}", modelFile, e);
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

    AuthenticationConfig authConfig = createAuthenticationConfig();
    if (authConfig == null) {
      throw new IllegalStateException("Service account configuration is required for worldwide availability checks.");
    }

    WorldwideCheckUseCase useCase = new WorldwideCheckUseCase(vertexAiService);
    RegionCheckResult result = useCase.execute(authConfig, modelAlias, testPrompt, debug);

    String modelFile = getModelFile();
    Properties modelProperties = loadModelPropertiesOrDefault(modelFile);
    useCase.generateReport(result, modelAlias, testPrompt, authConfig.getProjectId(), modelFile, modelProperties);

    return result.hasSuccess() ? 0 : 1;
  }

  private Properties loadModelPropertiesOrDefault(String modelFile) {
    if (modelFile == null) {
      modelFile = "src/main/resources/models.properties";
    }
    try (FileInputStream fis = new FileInputStream(modelFile)) {
      Properties props = new Properties();
      props.load(fis);
      return props;
    } catch (IOException e) {
      logger.warn("Failed to load model properties for report: {}", e.getMessage());
      return new Properties();
    }
  }

  private AuthenticationConfig createAuthenticationConfig() {
    try {
      if (auth.apiKeyAuth != null) {
        return authConfigFactory.createApiKeyConfig(auth.apiKeyAuth.apiKey);
      } else if (auth.saAuth != null) {
        return authConfigFactory.createServiceAccountConfig(auth.saAuth.projectId, auth.saAuth.location, auth.saAuth.saKeyFile,
            checkAllRegions, worldwide, cluster, regionProvider);
      } else {
        logger.error("Please provide either API key or Service Account credentials.");
        return null;
      }
    } catch (IllegalArgumentException | IllegalStateException e) {
      logger.error("Invalid authentication configuration: {}", e.getMessage());
      return null;
    }
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new VertexAiMasterMain()).execute(args);
    System.exit(exitCode);
  }
}
