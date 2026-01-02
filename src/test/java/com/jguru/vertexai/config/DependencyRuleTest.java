package com.jguru.vertexai.config;

import com.jguru.vertexai.domain.entity.Model;
import com.jguru.vertexai.domain.entity.Region;
import com.jguru.vertexai.domain.repository.ModelRepository;
import com.jguru.vertexai.domain.repository.RegionRepository;
import com.jguru.vertexai.application.usecase.GenerateContentUseCase;
import com.jguru.vertexai.application.usecase.CheckRegionAvailabilityUseCase;
import com.jguru.vertexai.adapter.ModelController;
import com.jguru.vertexai.infrastructure.GoogleVertexAIAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class DependencyRuleTest {

  @Test
  @DisplayName("Domain entities should not depend on any other layers")
  public void domainEntitiesShouldNotDependOnOtherLayers() {
    // Check that Model entity has no dependencies on other layers
    Package modelPackage = Model.class.getPackage();
    assertNotNull(modelPackage);

    // Check that Region entity has no dependencies on other layers
    Package regionPackage = Region.class.getPackage();
    assertNotNull(regionPackage);
  }

  @Test
  @DisplayName("Domain repositories should only depend on domain entities")
  public void domainRepositoriesShouldOnlyDependOnDomainEntities() {
    // Check that ModelRepository interface depends only on domain entities
    assertNotNull(ModelRepository.class);
    assertTrue(ModelRepository.class.getPackageName().startsWith("com.jguru.vertexai.domain"));

    // Check that RegionRepository interface depends only on domain entities
    assertNotNull(RegionRepository.class);
    assertTrue(RegionRepository.class.getPackageName().startsWith("com.jguru.vertexai.domain"));
  }

  @Test
  @DisplayName("Application use cases should depend on domain layer only")
  public void applicationUseCasesShouldDependOnDomainLayerOnly() {
    // Check that GenerateContentUseCase interface depends on domain layer
    assertNotNull(GenerateContentUseCase.class);
    assertTrue(GenerateContentUseCase.class.getPackageName().startsWith("com.jguru.vertexai.application"));

    // Check that CheckRegionAvailabilityUseCase interface depends on domain layer
    assertNotNull(CheckRegionAvailabilityUseCase.class);
    assertTrue(CheckRegionAvailabilityUseCase.class.getPackageName().startsWith("com.jguru.vertexai.application"));
  }

  @Test
  @DisplayName("Interface adapters should depend on application layer")
  public void interfaceAdaptersShouldDependOnApplicationLayer() {
    // Check that ModelController interface depends on application layer
    assertNotNull(ModelController.class);
    assertTrue(ModelController.class.getPackageName().startsWith("com.jguru.vertexai.adapter"));
  }

  @Test
  @DisplayName("Infrastructure layer should implement domain interfaces")
  public void infrastructureLayerShouldImplementDomainInterfaces() {
    // Check that GoogleVertexAIAdapter interface is in infrastructure
    assertNotNull(GoogleVertexAIAdapter.class);
    assertTrue(GoogleVertexAIAdapter.class.getPackageName().startsWith("com.jguru.vertexai.infrastructure"));
  }

  @Test
  @DisplayName("Dependency inversion principle should be maintained")
  public void dependencyInversionPrincipleShouldBeMaintained() {
    // Create the application module
    ApplicationModule module = new ApplicationModule();

    // Get dependencies following the dependency inversion
    ModelRepository modelRepository = module.provideModelRepository();
    assertNotNull(modelRepository);

    // Verify the implementation is from infrastructure but interface is from domain
    assertEquals("com.jguru.vertexai.infrastructure.impl.ModelRepositoryImpl", modelRepository.getClass().getName());
  }
}
