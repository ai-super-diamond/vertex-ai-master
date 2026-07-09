package com.jguru.vertexai.integration;

import com.jguru.vertexai.adapter.ModelController;
import com.jguru.vertexai.config.ApplicationModule;
import com.jguru.vertexai.service.ModelClient;
import com.jguru.vertexai.service.ModelClientFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EndToEndFlowTest {

  @Test
  @DisplayName("Should execute complete flow from controller to infrastructure and back")
  public void shouldExecuteCompleteFlowFromControllerToInfrastructureAndBack() throws Exception {
    // Arrange - Create the complete application module with all dependencies
    ApplicationModule module = new ApplicationModule();
    module.setApiKey("test-api-key");

    // Mock the infrastructure dependency to avoid real API calls
    ModelClientFactory mockFactory = mock(ModelClientFactory.class);
    ModelClient mockClient = mock(ModelClient.class);
    when(mockFactory.createClient(any())).thenReturn(mockClient);
    when(mockClient.callVertexAi(anyString(), anyString()))
        .thenAnswer(invocation -> "Mock response for " + invocation.getArgument(0) + " with prompt: " + invocation.getArgument(1));
    module.setModelClientFactory(mockFactory);

    // Act - Get the fully configured controller (this wires up the entire dependency chain)
    ModelController controller = module.provideModelController(module.provideGenerateContentUseCase(module.provideModelRepository(),
        module.provideModelResolutionService(module.provideModelRepository())));

    // Act - Execute the complete flow: Controller -> UseCase -> DomainService -> Repository -> Infrastructure -> Response formatting
    String result = controller.generateContent("gemini.pro", "Hello, Vertex AI!");

    // Assert - Verify the complete flow worked
    assertNotNull(result, "Result should not be null");
    assertTrue(result.startsWith("SUCCESS:"), "Result should be formatted as success");
    assertTrue(result.contains("gemini-3.1-pro-preview"), "Result should contain resolved model name");
    assertTrue(result.contains("Hello, Vertex AI!"), "Result should contain original prompt");

    // Additional assertion to ensure it's not the raw mock response
    assertTrue(result.contains("Mock response"), "Result should come from infrastructure adapter");
  }

  @Test
  @DisplayName("Should handle error flow properly through all layers")
  public void shouldHandleErrorFlowProperlyThroughAllLayers() throws Exception {
    // Arrange - Create the complete application module
    ApplicationModule module = new ApplicationModule();
    module.setApiKey("test-api-key");

    // Mock the infrastructure dependency to avoid real API calls
    ModelClientFactory mockFactory = mock(ModelClientFactory.class);
    ModelClient mockClient = mock(ModelClient.class);
    when(mockFactory.createClient(any())).thenReturn(mockClient);
    when(mockClient.callVertexAi(anyString(), anyString()))
        .thenAnswer(invocation -> "Mock response for " + invocation.getArgument(0) + " with prompt: " + invocation.getArgument(1));
    module.setModelClientFactory(mockFactory);

    // Act - Get the controller
    ModelController controller = module.provideModelController(module.provideGenerateContentUseCase(module.provideModelRepository(),
        module.provideModelResolutionService(module.provideModelRepository())));

    // Act - Execute with invalid model alias (this triggers error flow)
    String result = controller.generateContent("invalid.model", "Hello, Vertex AI!");

    // Assert - Verify the error flow worked properly
    assertNotNull(result, "Error result should not be null");
    assertTrue(result.startsWith("ERROR:"), "Error result should be formatted as error");
    assertTrue(result.contains("not found"), "Error should indicate model not found");
  }

  @Test
  @DisplayName("Should demonstrate dependency inversion working end-to-end")
  public void shouldDemonstrateDependencyInversionWorkingEndToEnd() throws Exception {
    // Arrange - Create the application module
    ApplicationModule module = new ApplicationModule();
    module.setApiKey("test-api-key");

    // Mock the infrastructure dependency to avoid real API calls
    ModelClientFactory mockFactory = mock(ModelClientFactory.class);
    ModelClient mockClient = mock(ModelClient.class);
    when(mockFactory.createClient(any())).thenReturn(mockClient);
    when(mockClient.callVertexAi(anyString(), anyString()))
        .thenAnswer(invocation -> "Mock response for " + invocation.getArgument(0) + " with prompt: " + invocation.getArgument(1));
    module.setModelClientFactory(mockFactory);

    // Act - The domain layer uses repository interface, but infrastructure provides implementation
    // This demonstrates dependency inversion: domain doesn't know about infrastructure details
    var modelRepository = module.provideModelRepository();
    var modelResolutionService = module.provideModelResolutionService(modelRepository);
    var generateContentUseCase = module.provideGenerateContentUseCase(modelRepository, modelResolutionService);
    var modelController = module.provideModelController(generateContentUseCase);

    // Act - Execute the full flow
    String result = modelController.generateContent("gemini.pro", "Test prompt");

    // Assert - Verify that all layers work together despite dependency inversion
    assertNotNull(result);
    assertTrue(result.startsWith("SUCCESS:"), "Should return success format");
    assertTrue(result.contains("gemini-3.1-pro-preview"), "Should contain resolved model name");
    assertTrue(result.contains("Test prompt"), "Should contain original prompt");

    // The key assertion: The domain layer works with interface,
    // infrastructure layer provides implementation, but domain doesn't depend on infrastructure
    // This proves dependency inversion principle is working
    assertTrue(modelRepository.getClass().getSimpleName().contains("Impl"), "Repository is implemented in infrastructure layer");
  }

  @Test
  @DisplayName("Should maintain clean architecture boundaries throughout execution")
  public void shouldMaintainCleanArchitectureBoundariesThroughoutExecution() throws Exception {
    // Arrange - Create the application module to set up clean architecture
    ApplicationModule module = new ApplicationModule();
    module.setApiKey("test-api-key");

    // Mock the infrastructure dependency to avoid real API calls
    ModelClientFactory mockFactory = mock(ModelClientFactory.class);
    ModelClient mockClient = mock(ModelClient.class);
    when(mockFactory.createClient(any())).thenReturn(mockClient);
    when(mockClient.callVertexAi(anyString(), anyString()))
        .thenAnswer(invocation -> "Mock response for " + invocation.getArgument(0) + " with prompt: " + invocation.getArgument(1));
    module.setModelClientFactory(mockFactory);

    // Act - Get all the key components across different layers
    var modelRepository = module.provideModelRepository();
    var modelResolutionService = module.provideModelResolutionService(modelRepository);
    var generateContentUseCase = module.provideGenerateContentUseCase(modelRepository, modelResolutionService);
    var modelController = module.provideModelController(generateContentUseCase);

    // Act - Execute the complete flow
    String result = modelController.generateContent("gemini.pro", "Architecture test");

    // Assert - Verify the clean architecture boundaries are maintained:
    // 1. Entities are at the core with no dependencies
    // 2. Use cases depend on entities and interfaces (not implementations)
    // 3. Interface adapters depend on use cases
    // 4. Frameworks/infrastructure depend on interfaces

    // Check that the flow completed successfully (indicating proper boundaries)
    assertNotNull(result);
    assertTrue(result.startsWith("SUCCESS:"));
    assertTrue(result.contains("gemini-3.1-pro-preview"));
    assertTrue(result.contains("Architecture test"));

    // Verify that the repository is from infrastructure but used by domain
    assertTrue(modelRepository.getClass().getName().contains("infrastructure"), "Repository implementation is in infrastructure layer");
    assertEquals("com.jguru.vertexai.domain.repository.ModelRepository", modelRepository.getClass().getInterfaces()[0].getName(),
        "Repository implements domain interface");

    // Verify the controller is from adapter layer
    assertTrue(modelController.getClass().getName().contains("adapter"), "Controller implementation is in adapter layer");
  }

  @Test
  @DisplayName("Should validate that domain layer remains independent")
  public void shouldValidateThatDomainLayerRemainsIndependent() throws Exception {
    // Arrange - The domain layer should be independent of other layers
    ApplicationModule module = new ApplicationModule();
    module.setApiKey("test-api-key");

    // Mock the infrastructure dependency to avoid real API calls
    ModelClientFactory mockFactory = mock(ModelClientFactory.class);
    ModelClient mockClient = mock(ModelClient.class);
    when(mockFactory.createClient(any())).thenReturn(mockClient);
    when(mockClient.callVertexAi(anyString(), anyString()))
        .thenAnswer(invocation -> "Mock response for " + invocation.getArgument(0) + " with prompt: " + invocation.getArgument(1));
    module.setModelClientFactory(mockFactory);

    // Act - Get the domain service with its repository dependency
    var modelRepository = module.provideModelRepository();
    var modelResolutionService = module.provideModelResolutionService(modelRepository);

    // Act - Use domain service directly (without going through use cases or adapters)
    var resolvedModel = modelResolutionService.resolveModel("gemini.pro");

    // Assert - Domain layer works independently
    assertNotNull(resolvedModel, "Domain service should work independently");
    assertEquals("gemini.pro", resolvedModel.getAlias());
    assertEquals("gemini-3.1-pro-preview", resolvedModel.getFullName());

    // Verify domain validation
    assertTrue(modelResolutionService.isValidModelAlias("gemini.pro"), "Domain service validates correctly");
    assertFalse(modelResolutionService.isValidModelAlias("invalid.model"), "Domain service validates correctly");

    // Key assertion: Domain layer functions without knowing about infrastructure details
    assertTrue(resolvedModel.getClass().getPackageName().startsWith("com.jguru.vertexai.domain"), "Resolved model is a domain entity");
  }
}
