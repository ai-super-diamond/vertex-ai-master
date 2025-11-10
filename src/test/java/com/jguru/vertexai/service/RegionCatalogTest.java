package com.jguru.vertexai.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegionCatalogTest {

  @Test
  void shouldResolveClusterAliasesCaseInsensitive() {
    assertThat(RegionCatalog.findCluster("us")).contains(RegionCatalog.Cluster.US);
    assertThat(RegionCatalog.findCluster("eUrOpE")).contains(RegionCatalog.Cluster.EUROPE);
    assertThat(RegionCatalog.findCluster("unknown")).isEmpty();
  }

  @Test
  void shouldReturnDefensiveCopies() {
    List<String> regions = RegionCatalog.getRegions(RegionCatalog.Cluster.US);
    regions.add("custom-region");

    List<String> freshList = RegionCatalog.getRegions(RegionCatalog.Cluster.US);
    assertThat(freshList).doesNotContain("custom-region");
  }

  @Test
  void shouldReturnCombinedRegionList() {
    assertThat(RegionCatalog.getAllRegions()).isNotEmpty()
        .containsAll(RegionCatalog.getRegions(RegionCatalog.Cluster.CANADA));
  }
}
