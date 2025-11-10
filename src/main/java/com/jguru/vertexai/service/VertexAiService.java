package com.jguru.vertexai.service;

import com.jguru.vertexai.service.dto.GenerationRequest;
import com.jguru.vertexai.service.dto.GenerationResult;
import com.jguru.vertexai.service.dto.RegionCheckRequest;
import com.jguru.vertexai.service.dto.RegionCheckResult;
import java.util.List;

/**
 * Service interface for Vertex AI operations.
 */
public interface VertexAiService {

  /**
   * Generates content based on the provided request.
   *
   * @param request
   *          the generation request
   * @return the result containing the generated content
   * @throws Exception
   *           if generation fails
   */
  GenerationResult generateContent(GenerationRequest request) throws Exception;

  /**
   * Checks model availability across multiple regions.
   *
   * @param request
   *          the region check request
   * @return the result containing region availability information
   * @throws Exception
   *           if region check fails
   */
  RegionCheckResult checkRegionAvailability(RegionCheckRequest request) throws Exception;

  /**
   * Resolves a model name or alias to the actual model name.
   *
   * @param modelName
   *          the model name or alias
   * @return the resolved model name
   */
  String resolveModelName(String modelName);

  /**
   * Gets regions for a specified cluster.
   *
   * @param clusterName
   *          the cluster name (e.g., US, EU, ASIA, etc.)
   * @return list of regions for the cluster, or null if invalid cluster
   */
  List<String> getRegionsForCluster(String clusterName);

  /**
   * Gets all regions across every cluster.
   *
   * @return list of all regions
   */
  List<String> getAllRegions();
}
