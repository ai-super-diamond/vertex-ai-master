package com.jguru.vertexai.utils;

import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

/**
 * Loads {@link Properties} files from either an external filesystem override or a classpath resource. This consolidates the repeated
 * boilerplate across services, clients, and utilities.
 */
public final class PropertiesLoader {

  private PropertiesLoader() {
    // Utility class
  }

  /**
   * Loads properties using the following precedence:
   * <ol>
   * <li>If the system property referenced by {@code systemPropertyKey} is set and points to an existing file, that file is loaded.</li>
   * <li>Otherwise a classpath resource located at {@code resourcePath} is loaded, when present.</li>
   * </ol>
   *
   * @param logger
   *          logger used for informational output (required)
   * @param systemPropertyKey
   *          system property holding an external configuration path
   * @param resourcePath
   *          classpath resource path (with or without a leading slash)
   * @return populated properties instance (may be empty when nothing was found)
   */
  public static Properties load(Logger logger, String systemPropertyKey, String resourcePath) {
    Objects.requireNonNull(logger, "logger");
    Objects.requireNonNull(resourcePath, "resourcePath");

    Properties properties = new Properties();
    boolean loaded = false;

    if (systemPropertyKey != null) {
      String externalLocation = System.getProperty(systemPropertyKey);
      if (externalLocation != null) {
        Path configPath = Paths.get(externalLocation);
        if (Files.exists(configPath)) {
          try (InputStream is = new FileInputStream(configPath.toFile())) {
            properties.load(is);
            logger.info("Loaded {} from {}", resourcePath, configPath);
            loaded = true;
          } catch (IOException e) {
            logger.warn("Failed to load {} from {}: {}", resourcePath, configPath, e.getMessage());
          }
        } else {
          logger.warn("System property {} points to missing file: {}", systemPropertyKey, configPath);
        }
      }
    }

    if (!loaded) {
      String normalizedPath = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
      try (InputStream is = PropertiesLoader.class.getResourceAsStream(normalizedPath)) {
        if (is != null) {
          properties.load(is);
          logger.info("Loaded embedded resource {}", normalizedPath);
          loaded = true;
        }
      } catch (IOException e) {
        logger.warn("Failed to load embedded resource {}: {}", normalizedPath, e.getMessage());
      }
    }

    if (!loaded) {
      logger.warn("No configuration found for {}", resourcePath);
    }

    return properties;
  }
}
