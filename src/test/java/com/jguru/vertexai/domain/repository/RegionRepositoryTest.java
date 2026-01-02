package com.jguru.vertexai.domain.repository;

import com.jguru.vertexai.domain.entity.Region;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Arrays;

public class RegionRepositoryTest {

  @Test
  @DisplayName("Should have method to find regions by cluster")
  public void shouldHaveMethodToFindRegionsByCluster() {
    // This test ensures that the RegionRepository interface has the method
    RegionRepository repository = new RegionRepository() {
      @Override
      public List<Region> findByCluster(String cluster) {
        return Arrays.asList(new Region("us-central1", "US"), new Region("us-west1", "US"));
      }

      @Override
      public List<Region> findAll() {
        return Arrays.asList(new Region("us-central1", "US"), new Region("europe-west1", "EU"));
      }
    };

    List<Region> regions = repository.findByCluster("US");
    assertNotNull(regions);
    assertFalse(regions.isEmpty());
    assertEquals("US", regions.get(0).getCluster());
  }

  @Test
  @DisplayName("Should have method to find all regions")
  public void shouldHaveMethodToFindAllRegions() {
    // This test ensures that the RegionRepository interface has the method
    RegionRepository repository = new RegionRepository() {
      @Override
      public List<Region> findByCluster(String cluster) {
        return Arrays.asList(new Region("us-central1", "US"));
      }

      @Override
      public List<Region> findAll() {
        return Arrays.asList(new Region("us-central1", "US"), new Region("europe-west1", "EU"));
      }
    };

    List<Region> allRegions = repository.findAll();
    assertNotNull(allRegions);
    assertFalse(allRegions.isEmpty());
    assertTrue(allRegions.size() >= 2);
  }
}
