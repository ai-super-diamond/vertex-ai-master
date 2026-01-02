package com.jguru.vertexai.domain;

import com.jguru.vertexai.domain.entity.Model;

public interface ModelResolutionService {
  Model resolveModel(String alias);
  boolean isValidModelAlias(String alias);
}
