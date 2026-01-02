package com.jguru.vertexai.domain.entity;

import java.util.Objects;

public class Region {
  private final String name;
  private final String cluster;

  public Region(String name, String cluster) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Name cannot be null or empty");
    }
    if (cluster == null || cluster.trim().isEmpty()) {
      throw new IllegalArgumentException("Cluster cannot be null or empty");
    }
    this.name = name.trim();
    this.cluster = cluster.trim();
  }

  public String getName() {
    return name;
  }

  public String getCluster() {
    return cluster;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Region region = (Region) o;
    return Objects.equals(name, region.name) && Objects.equals(cluster, region.cluster);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, cluster);
  }

  @Override
  public String toString() {
    return "Region{" + "name='" + name + '\'' + ", cluster='" + cluster + '\'' + '}';
  }
}
