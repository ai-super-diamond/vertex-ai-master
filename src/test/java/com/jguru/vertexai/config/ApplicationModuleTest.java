package com.jguru.vertexai.config;

import com.jguru.vertexai.adapter.ModelController;
import com.jguru.vertexai.application.usecase.GenerateContentUseCase;
import com.jguru.vertexai.domain.ModelResolutionService;
import com.jguru.vertexai.domain.repository.ModelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class ApplicationModuleTest {

  @Test
  @DisplayName("Should create and configure all dependencies properly")
  public void shouldCreateAndConfigureAllDependenciesProperly() {
    // Arrange
    ApplicationModule module = new ApplicationModule();

    // Act
    ModelRepository modelRepository = module.provideModelRepository();
    ModelResolutionService modelResolutionService = module.provideModelResolutionService(modelRepository);
    GenerateContentUseCase generateContentUseCase = module.provideGenerateContentUseCase(modelRepository, modelResolutionService);
    ModelController modelController = module.provideModelController(generateContentUseCase);

    // Assert
    assertNotNull(modelRepository);
    assertNotNull(modelResolutionService);
    assertNotNull(generateContentUseCase);
    assertNotNull(modelController);
  }

  @Test
  @DisplayName("Should create the same instance of ModelRepository when called multiple times")
  public void shouldCreateSameInstanceWhenCalledMultipleTimes() {
    // Arrange
    ApplicationModule module = new ApplicationModule();

    // Act
    ModelRepository firstInstance = module.provideModelRepository();
    ModelRepository secondInstance = module.provideModelRepository();

    // Assert
    assertSame(firstInstance, secondInstance);
  }

  @Test
  @DisplayName("Should create different instances of use case implementations")
  public void shouldCreateDifferentInstancesOfUseCases() {
    // Arrange
    ApplicationModule module = new ApplicationModule();

    // Act
    GenerateContentUseCase firstInstance = module.provideGenerateContentUseCase(module.provideModelRepository(),
        module.provideModelResolutionService(module.provideModelRepository()));
    GenerateContentUseCase secondInstance = module.provideGenerateContentUseCase(module.provideModelRepository(),
        module.provideModelResolutionService(module.provideModelRepository()));

    // Assert
    // Note: These will be different instances as each use case instance is created with new
    assertNotSame(firstInstance, secondInstance);
  }
}
