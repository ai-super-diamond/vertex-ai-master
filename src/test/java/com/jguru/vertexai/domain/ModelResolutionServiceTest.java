package com.jguru.vertexai.domain;

import com.jguru.vertexai.domain.entity.Model;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class ModelResolutionServiceTest {

  @Test
  @DisplayName("Should have method to resolve model alias to full name")
  public void shouldHaveMethodToResolveModelAliasToFullName() {
    // This test ensures that the ModelResolutionService interface has the method
    ModelResolutionService service = new ModelResolutionService() {
      @Override
      public Model resolveModel(String alias) {
        return new Model("gemini.pro", "gemini-1.5-pro-001");
      }

      @Override
      public boolean isValidModelAlias(String alias) {
        return true;
      }
    };

    Model resolvedModel = service.resolveModel("gemini.pro");
    assertNotNull(resolvedModel);
    assertEquals("gemini.pro", resolvedModel.getAlias());
    assertEquals("gemini-1.5-pro-001", resolvedModel.getFullName());
  }

  @Test
  @DisplayName("Should have method to validate model alias")
  public void shouldHaveMethodToValidateModelAlias() {
    // This test ensures that the ModelResolutionService interface has the method
    ModelResolutionService service = new ModelResolutionService() {
      @Override
      public Model resolveModel(String alias) {
        return null;
      }

      @Override
      public boolean isValidModelAlias(String alias) {
        return true;
      }
    };

    boolean isValid = service.isValidModelAlias("gemini.pro");
    assertTrue(isValid);
  }
}
