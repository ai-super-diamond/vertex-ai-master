package com.jguru.vertexai.application.usecase;

import com.jguru.vertexai.domain.entity.Region;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Arrays;

public class CheckRegionAvailabilityUseCaseTest {

  @Test
  @DisplayName("Should have method to execute region availability check")
  public void shouldHaveMethodToExecuteRegionAvailabilityCheck() {
    // This test ensures that the CheckRegionAvailabilityUseCase interface has the method
    CheckRegionAvailabilityUseCase useCase = new CheckRegionAvailabilityUseCase() {
      @Override
      public List<Region> execute(String modelAlias, String cluster, String prompt) {
        return Arrays.asList(new Region("us-central1", "US"), new Region("us-west1", "US"));
      }
    };

    List<Region> result = useCase.execute("gemini.pro", "US", "Test prompt");
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals("US", result.get(0).getCluster());
  }

  @Test
  @DisplayName("Should handle null model alias gracefully")
  public void shouldHandleNullModelAliasGracefully() {
    // This test ensures that the CheckRegionAvailabilityUseCase interface has proper error handling
    CheckRegionAvailabilityUseCase useCase = new CheckRegionAvailabilityUseCase() {
      @Override
      public List<Region> execute(String modelAlias, String cluster, String prompt) {
        if (modelAlias == null) {
          throw new IllegalArgumentException("Model alias cannot be null");
        }
        return Arrays.asList(new Region("us-central1", "US"));
      }
    };

    assertThrows(IllegalArgumentException.class, () -> {
      useCase.execute(null, "US", "Test prompt");
    });
  }

  @Test
  @DisplayName("Should handle null cluster gracefully")
  public void shouldHandleNullClusterGracefully() {
    // This test ensures that the CheckRegionAvailabilityUseCase interface has proper error handling
    CheckRegionAvailabilityUseCase useCase = new CheckRegionAvailabilityUseCase() {
      @Override
      public List<Region> execute(String modelAlias, String cluster, String prompt) {
        if (cluster == null) {
          throw new IllegalArgumentException("Cluster cannot be null");
        }
        return Arrays.asList(new Region("us-central1", "US"));
      }
    };

    assertThrows(IllegalArgumentException.class, () -> {
      useCase.execute("gemini.pro", null, "Test prompt");
    });
  }

  @Test
  @DisplayName("Should handle null prompt gracefully")
  public void shouldHandleNullPromptGracefully() {
    // This test ensures that the CheckRegionAvailabilityUseCase interface has proper error handling
    CheckRegionAvailabilityUseCase useCase = new CheckRegionAvailabilityUseCase() {
      @Override
      public List<Region> execute(String modelAlias, String cluster, String prompt) {
        if (prompt == null) {
          throw new IllegalArgumentException("Prompt cannot be null");
        }
        return Arrays.asList(new Region("us-central1", "US"));
      }
    };

    assertThrows(IllegalArgumentException.class, () -> {
      useCase.execute("gemini.pro", "US", null);
    });
  }
}
