package com.jguru.vertexai.service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Central catalogue of Google Cloud regions grouped by geographic cluster.
 *
 * <p>
 * The catalogue keeps the region definitions in a single place so that they can be reused across clients, services, and utilities without
 * duplicating large literal lists. Callers receive defensive copies making it safe to modify the returned lists in tests.
 * </p>
 */
public final class RegionCatalog {

  private RegionCatalog() {
    // Utility class
  }

  /**
   * Well-known region clusters with their aliases and region membership.
   */
  public enum Cluster {
    US(Set.of("US", "USA", "UNITED_STATES"),
        List.of("us-central1", "us-east1", "us-east4", "us-east5", "us-south1", "us-west1", "us-west2", "us-west3", "us-west4")), EUROPE(
            Set.of("EUROPE", "EU"),
            List.of("europe-central2", "europe-north1", "europe-southwest1", "europe-west1", "europe-west2", "europe-west3", "europe-west4",
                "europe-west6", "europe-west8", "europe-west9", "europe-west12")), ASIA(
                    Set.of("ASIA", "APAC", "ASIA_PACIFIC"),
                    List.of("asia-east1", "asia-east2", "asia-northeast1", "asia-northeast2", "asia-northeast3", "asia-south1",
                        "asia-south2", "asia-southeast1", "asia-southeast2", "australia-southeast1", "australia-southeast2")), MIDDLE_EAST(
                            Set.of("MIDDLE_EAST", "ME"),
                            List.of("me-central1", "me-central2", "me-west1")), AFRICA(Set.of("AFRICA"), List.of("africa-south1")), CANADA(
                                Set.of("CANADA", "CA"), List.of("northamerica-northeast1", "northamerica-northeast2")), SOUTH_AMERICA(
                                    Set.of("SOUTH_AMERICA", "SA"), List.of("southamerica-east1", "southamerica-west1"));

    private final Set<String> aliases;
    private final List<String> regions;

    Cluster(Set<String> aliases, List<String> regions) {
      this.aliases = aliases;
      this.regions = List.copyOf(regions);
    }

    Set<String> getAliases() {
      return aliases;
    }

    List<String> getRegions() {
      return regions;
    }
  }

  private static final Map<String, Cluster> NAME_INDEX = buildNameIndex();
  private static final EnumMap<Cluster, List<String>> CLUSTER_REGIONS = buildClusterRegions();

  private static Map<String, Cluster> buildNameIndex() {
    Map<String, Cluster> index = new HashMap<>();
    for (Cluster cluster : Cluster.values()) {
      for (String alias : cluster.getAliases()) {
        index.put(alias.toUpperCase(Locale.ROOT), cluster);
      }
    }
    return index;
  }

  private static EnumMap<Cluster, List<String>> buildClusterRegions() {
    EnumMap<Cluster, List<String>> map = new EnumMap<>(Cluster.class);
    for (Cluster cluster : Cluster.values()) {
      map.put(cluster, cluster.getRegions());
    }
    return map;
  }

  /**
   * Finds the cluster for the provided name or alias.
   *
   * @param clusterName
   *          cluster identifier (case insensitive)
   * @return optional cluster if the name is recognised
   */
  public static Optional<Cluster> findCluster(String clusterName) {
    if (clusterName == null || clusterName.isBlank()) {
      return Optional.empty();
    }
    Cluster cluster = NAME_INDEX.get(clusterName.toUpperCase(Locale.ROOT));
    return Optional.ofNullable(cluster);
  }

  /**
   * Returns a defensive copy of the regions for the specified cluster.
   *
   * @param cluster
   *          the region cluster
   * @return mutable copy of regions for that cluster
   */
  public static List<String> getRegions(Cluster cluster) {
    List<String> regions = CLUSTER_REGIONS.get(cluster);
    return regions == null ? List.of() : new ArrayList<>(regions);
  }

  /**
   * Returns a defensive copy of regions for the specified cluster name or alias.
   *
   * @param clusterName
   *          cluster identifier (case insensitive)
   * @return mutable copy of regions, or empty list when the cluster was not found
   */
  public static List<String> getRegions(String clusterName) {
    return findCluster(clusterName).map(RegionCatalog::getRegions).orElseGet(List::of);
  }

  /**
   * Returns all known regions across all clusters.
   *
   * @return combined list of all regions (mutable copy)
   */
  public static List<String> getAllRegions() {
    List<String> allRegions = new ArrayList<>();
    for (Cluster cluster : Cluster.values()) {
      allRegions.addAll(CLUSTER_REGIONS.get(cluster));
    }
    return allRegions;
  }
}
