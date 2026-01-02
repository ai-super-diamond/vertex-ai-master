package com.jguru.vertexai.unit;

import com.jguru.vertexai.domain.entity.Model;
import com.jguru.vertexai.domain.entity.Region;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class DomainEntityValidationTest {

  @Test
  @DisplayName("Should create Model with valid properties")
  public void shouldCreateModelWithValidProperties() {
    // Arrange & Act
    Model model = new Model("gemini.pro", "gemini-1.5-pro-001", true);

    // Assert
    assertEquals("gemini.pro", model.getAlias());
    assertEquals("gemini-1.5-pro-001", model.getFullName());
    assertTrue(model.isGlobal());
  }

  @Test
  @DisplayName("Should create Region with valid properties")
  public void shouldCreateRegionWithValidProperties() {
    // Arrange & Act
    Region region = new Region("us-central1", "US");

    // Assert
    assertEquals("us-central1", region.getName());
    assertEquals("US", region.getCluster());
  }

  @Test
  @DisplayName("Should throw exception when Model alias is null")
  public void shouldThrowExceptionWhenModelAliasIsNull() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new Model(null, "gemini-1.5-pro-001");
    });
  }

  @Test
  @DisplayName("Should throw exception when Model full name is null")
  public void shouldThrowExceptionWhenModelFullNameIsNull() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new Model("gemini.pro", null);
    });
  }

  @Test
  @DisplayName("Should throw exception when Region name is null")
  public void shouldThrowExceptionWhenRegionNameIsNull() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new Region(null, "US");
    });
  }

  @Test
  @DisplayName("Should throw exception when Region cluster is null")
  public void shouldThrowExceptionWhenRegionClusterIsNull() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new Region("us-central1", null);
    });
  }

  @Test
  @DisplayName("Should trim Model alias and full name during construction")
  public void shouldTrimModelAliasAndFullNameDuringConstruction() {
    // Arrange & Act
    Model model = new Model("  gemini.pro  ", "  gemini-1.5-pro-001  ");

    // Assert
    assertEquals("gemini.pro", model.getAlias());
    assertEquals("gemini-1.5-pro-001", model.getFullName());
  }

  @Test
  @DisplayName("Should trim Region name and cluster during construction")
  public void shouldTrimRegionNameAndClusterDuringConstruction() {
    // Arrange & Act
    Region region = new Region("  us-central1  ", "  US  ");

    // Assert
    assertEquals("us-central1", region.getName());
    assertEquals("US", region.getCluster());
  }
}
