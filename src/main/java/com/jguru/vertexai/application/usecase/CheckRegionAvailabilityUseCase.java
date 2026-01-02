package com.jguru.vertexai.application.usecase;

import com.jguru.vertexai.domain.entity.Region;
import java.util.List;

public interface CheckRegionAvailabilityUseCase {
  List<Region> execute(String modelAlias, String cluster, String prompt);
}
