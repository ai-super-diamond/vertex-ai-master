package com.jguru.vertexai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Implementation of RegionProvider that loads region data from configuration files.
 */
public class RegionProviderImpl implements RegionProvider {

  private static final Logger logger = LoggerFactory.getLogger(RegionProviderImpl.class);

  private Properties regionProperties = null;

  // Default regions (fallback if config file is not found)
  private static final List<String> DEFAULT_US_REGIONS = Arrays.asList("us-central1", "us-east1",
      "us-east4", "us-east5", "us-south1", "us-west1", "us-west2", "us-west3", "us-west4");

  private static final List<String> DEFAULT_EUROPE_REGIONS = Arrays.asList("europe-central2",
      "europe-north1", "europe-southwest1", "europe-west1", "europe-west2", "europe-west3",
      "europe-west4", "europe-west6", "europe-west8", "europe-west9", "europe-west12");

  private static final List<String> DEFAULT_ASIA_REGIONS = Arrays.asList("asia-east1", "asia-east2",
      "asia-northeast1", "asia-northeast2", "asia-northeast3", "asia-south1", "asia-south2",
      "asia-southeast1", "asia-southeast2", "australia-southeast1", "australia-southeast2");

  private static final List<String> DEFAULT_MIDDLE_EAST_REGIONS = Arrays.asList("me-central1",
      "me-central2", "me-west1");

  private static final List<String> DEFAULT_AFRICA_REGIONS = Arrays.asList("africa-south1");

  private static final List<String> DEFAULT_CANADA_REGIONS = Arrays
      .asList("northamerica-northeast1", "northamerica-northeast2");

  private static final List<String> DEFAULT_SOUTH_AMERICA_REGIONS = Arrays
      .asList("southamerica-east1", "southamerica-west1");

  /**
   * Loads region properties from external file or embedded resource.
   */
  private Properties getRegionProperties() {
    if (regionProperties == null) {
      regionProperties = new Properties();

      // Try external file first (from -Dregions.config system property)
      String externalConfig = System.getProperty("regions.config");
      if (externalConfig != null) {
        Path configPath = Paths.get(externalConfig);
        if (Files.exists(configPath)) {
          try (InputStream is = new FileInputStream(configPath.toFile())) {
            regionProperties.load(is);
            logger.info("Loaded regions from: {}", configPath);
            return regionProperties;
          } catch (IOException e) {
            logger.warn("Failed to load external regions.properties: {}", e.getMessage());
          }
        }
      }

      // Try loading from resources/regions.properties
      try (InputStream is = getClass().getResourceAsStream("/regions.properties")) {
        if (is != null) {
          regionProperties.load(is);
          logger.info("Loaded embedded regions.properties");
          return regionProperties;
        }
      } catch (IOException e) {
        logger.warn("Failed to load embedded regions.properties: {}", e.getMessage());
      }

      // If no config file found, use default regions
      logger.info("Using default region configuration");
    }
    return regionProperties;
  }

  @Override
  public List<String> getRegionsForCluster(String clusterName) {
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
      return getAllDefaultRegions();
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
      return getAllDefaultRegions();
    }

    return allRegions;
  }

  /**
   * Gets default regions for a specified cluster.
   */
  private List<String> getDefaultRegionsForCluster(String clusterName) {
    String upperCluster = clusterName.toUpperCase();
    switch (upperCluster) {
      case "US" :
      case "USA" :
        return new ArrayList<>(DEFAULT_US_REGIONS);
      case "EU" :
      case "EUROPE" :
        return new ArrayList<>(DEFAULT_EUROPE_REGIONS);
      case "ASIA" :
      case "APAC" :
      case "ASIA_PACIFIC" :
        return new ArrayList<>(DEFAULT_ASIA_REGIONS);
      case "MIDDLE_EAST" :
      case "ME" :
        return new ArrayList<>(DEFAULT_MIDDLE_EAST_REGIONS);
      case "AFRICA" :
        return new ArrayList<>(DEFAULT_AFRICA_REGIONS);
      case "CANADA" :
      case "CA" :
        return new ArrayList<>(DEFAULT_CANADA_REGIONS);
      case "SOUTH_AMERICA" :
      case "SA" :
        return new ArrayList<>(DEFAULT_SOUTH_AMERICA_REGIONS);
      default :
        return null;
    }
  }

  /**
   * Gets all default regions across all clusters.
   */
  private List<String> getAllDefaultRegions() {
    List<String> allRegions = new ArrayList<>();
    allRegions.addAll(DEFAULT_US_REGIONS);
    allRegions.addAll(DEFAULT_EUROPE_REGIONS);
    allRegions.addAll(DEFAULT_ASIA_REGIONS);
    allRegions.addAll(DEFAULT_MIDDLE_EAST_REGIONS);
    allRegions.addAll(DEFAULT_AFRICA_REGIONS);
    allRegions.addAll(DEFAULT_CANADA_REGIONS);
    allRegions.addAll(DEFAULT_SOUTH_AMERICA_REGIONS);
    return allRegions;
  }
}
