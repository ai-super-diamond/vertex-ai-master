package com.jguru.vertexai.service;

import java.util.List;

/**
 * Provider interface for region data.
 */
public interface RegionProvider {

  /**
   * Gets regions for a specified cluster.
   *
   * @param clusterName
   *          the cluster name (e.g., US, EU, ASIA, etc.)
   * @return list of regions for the cluster, or null if invalid cluster
   */
  List<String> getRegionsForCluster(String clusterName);

  /**
   * Gets all regions across all clusters.
   *
   * @return list of all regions
   */
  List<String> getAllRegions();
}
