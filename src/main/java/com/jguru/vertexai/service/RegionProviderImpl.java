package com.jguru.vertexai.service;

import com.jguru.vertexai.utils.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Implementation of RegionProvider that loads region data from configuration files.
 */
public class RegionProviderImpl implements RegionProvider {

  private static final Logger logger = LoggerFactory.getLogger(RegionProviderImpl.class);

  private Properties regionProperties = null;

  /**
   * Loads region properties from external file or embedded resource.
   */
  private Properties getRegionProperties() {
    if (regionProperties == null) {
      regionProperties = PropertiesLoader.load(logger, "regions.config", "regions.properties");
    }
    return regionProperties;
  }

  @Override
  public List<String> getRegionsForCluster(String clusterName) {
    if (clusterName == null || clusterName.isBlank()) {
      logger.warn("Cluster name must be provided to resolve regions");
      return null;
    }

    Properties props = getRegionProperties();

    // If no properties loaded, use defaults
    if (props == null || props.isEmpty()) {
      return getDefaultRegionsForCluster(clusterName);
    }

    String upperCluster = clusterName.toUpperCase();
    String propertyKey = upperCluster + "_REGIONS";

    // Try to get regions from properties
    String regionsStr = props.getProperty(propertyKey);
    if (regionsStr != null && !regionsStr.isEmpty()) {
      String[] regions = regionsStr.split(",");
      List<String> regionList = new ArrayList<>();
      for (String region : regions) {
        regionList.add(region.trim());
      }
      return regionList;
    }

    // Fallback to defaults
    return getDefaultRegionsForCluster(clusterName);
  }

  @Override
  public List<String> getAllRegions() {
    Properties props = getRegionProperties();

    // If no properties loaded, use defaults
    if (props == null || props.isEmpty()) {
      return RegionCatalog.getAllRegions();
    }

    List<String> allRegions = new ArrayList<>();

    // Try to get all regions from properties
    String[] clusterKeys = {"US", "EUROPE", "ASIA", "MIDDLE_EAST", "AFRICA", "CANADA",
        "SOUTH_AMERICA"};

    for (String clusterKey : clusterKeys) {
      String propertyKey = clusterKey + "_REGIONS";
      String regionsStr = props.getProperty(propertyKey);
      if (regionsStr != null && !regionsStr.isEmpty()) {
        String[] regions = regionsStr.split(",");
        for (String region : regions) {
          allRegions.add(region.trim());
        }
      }
    }

    // If no regions found in properties, fallback to defaults
    if (allRegions.isEmpty()) {
      return RegionCatalog.getAllRegions();
    }

    return allRegions;
  }

  /**
   * Gets default regions for a specified cluster.
   */
  private List<String> getDefaultRegionsForCluster(String clusterName) {
    return RegionCatalog.findCluster(clusterName).map(RegionCatalog::getRegions).orElse(null);
  }
}
