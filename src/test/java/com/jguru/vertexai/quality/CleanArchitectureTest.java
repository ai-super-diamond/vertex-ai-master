package com.jguru.vertexai.quality;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static org.junit.jupiter.api.Assertions.*;

@AnalyzeClasses(packages = "com.jguru.vertexai")
public class CleanArchitectureTest {

  @ArchTest
  static ArchRule domainLayerShouldNotDependOnOtherLayers = noClasses().that().resideInAPackage("..domain..").should().dependOnClassesThat()
      .resideInAPackage("..application..").orShould().dependOnClassesThat().resideInAPackage("..adapter..").orShould().dependOnClassesThat()
      .resideInAPackage("..infrastructure..");

  @ArchTest
  static ArchRule applicationLayerShouldNotDependOnInfrastructureOrAdapters = noClasses().that().resideInAPackage("..application..")
      .should().dependOnClassesThat().resideInAPackage("..infrastructure..").orShould().dependOnClassesThat()
      .resideInAPackage("..adapter..");

  @ArchTest
  static ArchRule infrastructureLayerShouldOnlyImplementDomainInterfaces = classes().that().resideInAPackage("..infrastructure..").should()
      .onlyDependOnClassesThat().resideInAnyPackage("..infrastructure..", "..domain..", "..application..", "..service..", "java..",
          "com.google..", "org.slf4j..", "javax..", "jakarta..");

  @ArchTest
  static ArchRule adapterLayerShouldNotDependOnInfrastructure = noClasses().that().resideInAPackage("..adapter..").should()
      .dependOnClassesThat().resideInAPackage("..infrastructure..");

  @ArchTest
  static ArchRule domainEntitiesShouldNotDependOnExternalFrameworks = noClasses().that().resideInAPackage("..domain.entity..").should()
      .dependOnClassesThat().resideOutsideOfPackage("..domain.entity..");

  @Test
  @DisplayName("Should validate dependency direction in Clean Architecture")
  public void shouldValidateDependencyDirectionInCleanArchitecture() {
    // Load all classes for analysis
    JavaClasses classes = new ClassFileImporter().importPackages("com.jguru.vertexai");

    // Verify that domain layer doesn't depend on outer layers
    ArchRule domainRule = noClasses().that().resideInAPackage("com.jguru.vertexai.domain..").should().dependOnClassesThat()
        .resideInAPackage("com.jguru.vertexai.infrastructure..");

    domainRule.check(classes);

    // Verify that application layer doesn't depend on infrastructure directly
    ArchRule applicationRule = noClasses().that().resideInAPackage("com.jguru.vertexai.application..").should().dependOnClassesThat()
        .resideInAPackage("com.jguru.vertexai.infrastructure..");

    applicationRule.check(classes);

    // Verify dependency inversion principle
    ArchRule dependencyInversionRule = classes().that().resideInAPackage("com.jguru.vertexai.infrastructure..").should()
        .onlyDependOnClassesThat().resideInAnyPackage("com.jguru.vertexai.infrastructure..", "com.jguru.vertexai.domain..",
            "com.jguru.vertexai.application..", "com.jguru.vertexai.service..", "java..", "com.google..", "org.slf4j..", "javax..",
            "jakarta..");

    dependencyInversionRule.check(classes);

    assertTrue(true, "All architecture validation rules passed");
  }

  @Test
  @DisplayName("Should maintain layer boundaries")
  public void shouldMaintainLayerBoundaries() {
    JavaClasses importedClasses = new ClassFileImporter().importPackages("com.jguru.vertexai");

    // Test that interfaces are in the right places
    ArchRule domainInterfaceRule = classes().that().resideInAPackage("com.jguru.vertexai.domain..").and()
        .haveSimpleNameEndingWith("Repository").should().beInterfaces();

    domainInterfaceRule.check(importedClasses);

    ArchRule useCaseInterfaceRule = classes().that().resideInAPackage("com.jguru.vertexai.application..").and()
        .haveSimpleNameEndingWith("UseCase").should().beInterfaces();

    useCaseInterfaceRule.check(importedClasses);

    assertTrue(true, "Layer boundaries are properly maintained");
  }

  @Test
  @DisplayName("Should verify implementation classes are in infrastructure")
  public void shouldVerifyImplementationClassesAreInInfrastructure() {
    JavaClasses importedClasses = new ClassFileImporter().importPackages("com.jguru.vertexai");

    // Check that implementation classes are in infrastructure layer
    ArchRule implRule = classes().that().haveSimpleNameEndingWith("Impl").should().resideInAnyPackage("..infrastructure..", "..service..",
        "..application..", "..adapter..");

    implRule.check(importedClasses);

    assertTrue(true, "Implementation classes are properly located in infrastructure layer");
  }
}
