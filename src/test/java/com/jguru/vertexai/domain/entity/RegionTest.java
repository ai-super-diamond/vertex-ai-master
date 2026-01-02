package com.jguru.vertexai.domain.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class RegionTest {

  @Test
  @DisplayName("Should create Region with valid name and cluster")
  public void shouldCreateRegionWithValidNameAndCluster() {
    // Arrange & Act
    Region region = new Region("us-central1", "US");

    // Assert
    assertEquals("us-central1", region.getName());
    assertEquals("US", region.getCluster());
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when name is null")
  public void shouldThrowIllegalArgumentExceptionWhenNameIsNull() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new Region(null, "US");
    });
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when name is empty")
  public void shouldThrowIllegalArgumentExceptionWhenNameIsEmpty() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new Region("", "US");
    });
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when name is blank")
  public void shouldThrowIllegalArgumentExceptionWhenNameIsBlank() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new Region("   ", "US");
    });
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when cluster is null")
  public void shouldThrowIllegalArgumentExceptionWhenClusterIsNull() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new Region("us-central1", null);
    });
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when cluster is empty")
  public void shouldThrowIllegalArgumentExceptionWhenClusterIsEmpty() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new Region("us-central1", "");
    });
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when cluster is blank")
  public void shouldThrowIllegalArgumentExceptionWhenClusterIsBlank() {
    // Assert
    assertThrows(IllegalArgumentException.class, () -> {
      new Region("us-central1", "   ");
    });
  }

  @Test
  @DisplayName("Should have correct string representation")
  public void shouldHaveCorrectStringRepresentation() {
    // Arrange
    Region region = new Region("us-central1", "US");

    // Act
    String regionString = region.toString();

    // Assert
    assertTrue(regionString.contains("us-central1"));
    assertTrue(regionString.contains("US"));
  }

  @Test
  @DisplayName("Should be equal to another Region with same name and cluster")
  public void shouldEqualAnotherRegionWithSameNameAndCluster() {
    // Arrange
    Region region1 = new Region("us-central1", "US");
    Region region2 = new Region("us-central1", "US");

    // Assert
    assertEquals(region1, region2);
    assertEquals(region1.hashCode(), region2.hashCode());
  }

  @Test
  @DisplayName("Should not be equal to another Region with different name")
  public void shouldNotEqualAnotherRegionWithDifferentName() {
    // Arrange
    Region region1 = new Region("us-central1", "US");
    Region region2 = new Region("us-west1", "US");

    // Assert
    assertNotEquals(region1, region2);
  }

  @Test
  @DisplayName("Should not be equal to another Region with different cluster")
  public void shouldNotEqualAnotherRegionWithDifferentCluster() {
    // Arrange
    Region region1 = new Region("us-central1", "US");
    Region region2 = new Region("us-central1", "EU");

    // Assert
    assertNotEquals(region1, region2);
  }
}
