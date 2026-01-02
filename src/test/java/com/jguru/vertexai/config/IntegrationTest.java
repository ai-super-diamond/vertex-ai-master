package com.jguru.vertexai.config;

import com.jguru.vertexai.adapter.ModelController;
import com.jguru.vertexai.application.usecase.GenerateContentUseCase;
import com.jguru.vertexai.domain.ModelResolutionService;
import com.jguru.vertexai.domain.entity.Model;
import com.jguru.vertexai.domain.repository.ModelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {

  @Test
  @DisplayName("Complete dependency injection flow should work end-to-end")
  public void completeDependencyInjectionFlowShouldWorkEndToEnd() {
    // Arrange - Create the configuration module
    ApplicationModule module = new ApplicationModule();

    // Act - Get the fully configured controller through dependency injection
    ModelController controller = module.provideModelController(module.provideGenerateContentUseCase(module.provideModelRepository(),
        module.provideModelResolutionService(module.provideModelRepository())));

    // Assert - Verify the controller was created successfully
    assertNotNull(controller, "ModelController should be created successfully");

    // Act - Use the controller to generate content
    String result = controller.generateContent("gemini.pro", "Hello, world!");

    // Assert - Verify the result
    assertNotNull(result);
    assertTrue(result.startsWith("SUCCESS:"), "Result should be formatted as success");
    assertTrue(result.contains("gemini-1.5-pro-001"), "Result should contain the resolved model name");
    assertTrue(result.contains("Hello, world!"), "Result should contain the original prompt");
  }

  @Test
  @DisplayName("Dependency inversion should ensure domain doesn't depend on infrastructure")
  public void dependencyInversionShouldEnsureDomainDoesNotDependOnInfrastructure() {
    // Arrange - Create the configuration module
    ApplicationModule module = new ApplicationModule();

    // Act - Get the model repository (domain interface implementation)
    ModelRepository modelRepository = module.provideModelRepository();
    ModelResolutionService modelResolutionService = module.provideModelResolutionService(modelRepository);
    Model resolvedModel = modelResolutionService.resolveModel("gemini.pro");

    // Assert - Verify that domain layer works independently
    assertNotNull(resolvedModel, "Model should be resolved by domain service");
    assertEquals("gemini.pro", resolvedModel.getAlias(), "Resolved model should have correct alias");
    assertEquals("gemini-1.5-pro-001", resolvedModel.getFullName(), "Resolved model should have correct full name");
    assertFalse(resolvedModel.isGlobal(), "Resolved model should have correct global setting");
  }

  @Test
  @DisplayName("Use case should be properly connected to domain services")
  public void useCaseShouldBeProperlyConnectedToDomainServices() {
    // Arrange - Create the configuration module
    ApplicationModule module = new ApplicationModule();

    // Act - Get the use case with all its dependencies
    GenerateContentUseCase useCase = module.provideGenerateContentUseCase(module.provideModelRepository(),
        module.provideModelResolutionService(module.provideModelRepository()));

    // Act - Execute the use case
    String result = useCase.execute("gemini.pro", "Test prompt");

    // Assert - Verify the result
    assertNotNull(result);
    assertTrue(result.contains("gemini-1.5-pro-001"), "Result should contain the resolved model name");
    assertTrue(result.contains("Test prompt"), "Result should contain the original prompt");
  }

  @Test
  @DisplayName("Configuration should maintain single instances where appropriate")
  public void configurationShouldMaintainSingleInstancesWhereAppropriate() {
    // Arrange - Create the configuration module
    ApplicationModule module = new ApplicationModule();

    // Act - Get the model repository multiple times
    ModelRepository firstInstance = module.provideModelRepository();
    ModelRepository secondInstance = module.provideModelRepository();
    ModelRepository thirdInstance = module.provideModelRepository();

    // Assert - Verify it's the same instance (singleton behavior for repository)
    assertSame(firstInstance, secondInstance, "ModelRepository should return same instance");
    assertSame(secondInstance, thirdInstance, "ModelRepository should return same instance");
  }
}
