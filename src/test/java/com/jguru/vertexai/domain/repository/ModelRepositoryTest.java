package com.jguru.vertexai.domain.repository;

import com.jguru.vertexai.domain.entity.Model;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class ModelRepositoryTest {

  @Test
  @DisplayName("Should have method to find model by alias")
  public void shouldHaveMethodToFindModelByAlias() {
    // This test ensures that the ModelRepository interface has the method
    // We're testing that it can be compiled and that it returns the expected type
    ModelRepository repository = new ModelRepository() {
      @Override
      public Model findByAlias(String alias) {
        return new Model("gemini.pro", "gemini-1.5-pro-001");
      }

      @Override
      public boolean existsByAlias(String alias) {
        return true;
      }
    };

    Model model = repository.findByAlias("gemini.pro");
    assertNotNull(model);
    assertEquals("gemini.pro", model.getAlias());
  }

  @Test
  @DisplayName("Should have method to check if model exists by alias")
  public void shouldHaveMethodToCheckIfModelExistsByAlias() {
    // This test ensures that the ModelRepository interface has the method
    ModelRepository repository = new ModelRepository() {
      @Override
      public Model findByAlias(String alias) {
        return null;
      }

      @Override
      public boolean existsByAlias(String alias) {
        return true;
      }
    };

    boolean exists = repository.existsByAlias("gemini.pro");
    assertTrue(exists);
  }
}
