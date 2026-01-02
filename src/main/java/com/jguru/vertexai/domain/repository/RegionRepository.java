package com.jguru.vertexai.domain.repository;

import com.jguru.vertexai.domain.entity.Region;
import java.util.List;

public interface RegionRepository {
  List<Region> findByCluster(String cluster);
  List<Region> findAll();
}
