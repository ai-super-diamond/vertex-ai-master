package com.jguru.vertexai.client;

import com.jguru.vertexai.service.dto.RegionCheckRequest;
import com.jguru.vertexai.service.dto.RegionCheckResult;
import com.jguru.vertexai.service.VertexAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client for checking model availability across worldwide regions.
 */
public class WorldwideAvailabilityClient {

  private static final Logger logger = LoggerFactory.getLogger(WorldwideAvailabilityClient.class);

  private final VertexAiService vertexAiService;

  public WorldwideAvailabilityClient(VertexAiService vertexAiService) {
    this.vertexAiService = vertexAiService;
  }

  /**
   * Checks model availability across all worldwide regions.
   *
   * @param request
   *          The region check request containing model name, authentication config, and test prompt
   * @return RegionCheckResult with results for all regions
   * @throws Exception
   *           if there's an error during the check
   */
  public RegionCheckResult checkWorldwideAvailability(RegionCheckRequest request) throws Exception {
    // Build combined region list from the service to avoid duplicate region definitions
    List<String> allRegions = List.copyOf(vertexAiService.getAllRegions());

    logger.info("Testing model '{}' across worldwide regions...", request.getModelName());
    logger.info("Total regions to test: {}", allRegions.size());

    Map<String, String> results = new HashMap<>();

    // Test each region
    for (String region : allRegions) {
      try {
        // Create a new request for this specific region
        RegionCheckRequest regionRequest = RegionCheckRequest.builder().withAuthenticationConfig(request.getAuthenticationConfig())
            .withModelName(request.getModelName()).withTestPrompt(request.getTestPrompt()).withRegions(List.of(region)).build();

        // Check this specific region
        RegionCheckResult regionResult = vertexAiService.checkRegionAvailability(regionRequest);

        // Add results
        for (Map.Entry<String, String> entry : regionResult.getRegionResults().entrySet()) {
          String status = entry.getValue();
          results.put(entry.getKey(), status);
          if ("SUCCESS".equals(status)) {
            logger.info("✓ [SUCCESS] {} - Model works!", region);
          } else {
            logger.error("✗ [FAIL] {} - {}", region, status);
          }
        }
      } catch (Exception e) {
        results.put(region, "ERROR: " + e.getMessage());
        logger.error("✗ [ERROR] {} - {}", region, e.getMessage());
      }
    }

    return new RegionCheckResult(results);
  }
}
