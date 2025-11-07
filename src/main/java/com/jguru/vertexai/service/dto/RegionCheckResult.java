package com.jguru.vertexai.service.dto;

import java.util.Map;

/**
 * Result object for region availability checking.
 */
public class RegionCheckResult {
  private final Map<String, String> regionResults;
  private final int successCount;
  private final int failCount;
  private final int totalCount;

  public RegionCheckResult(Map<String, String> regionResults) {
    this.regionResults = regionResults;

    int success = 0;
    int fail = 0;

    for (String result : regionResults.values()) {
      if ("SUCCESS".equals(result)) {
        success++;
      } else {
        fail++;
      }
    }

    this.successCount = success;
    this.failCount = fail;
    this.totalCount = regionResults.size();
  }

  public Map<String, String> getRegionResults() {
    return regionResults;
  }

  public int getSuccessCount() {
    return successCount;
  }

  public int getFailCount() {
    return failCount;
  }

  public int getTotalCount() {
    return totalCount;
  }

  public boolean hasSuccess() {
    return successCount > 0;
  }
}
