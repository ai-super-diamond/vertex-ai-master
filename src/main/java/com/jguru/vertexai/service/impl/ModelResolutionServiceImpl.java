package com.jguru.vertexai.service.impl;

import com.jguru.vertexai.domain.ModelResolutionService;
import com.jguru.vertexai.domain.entity.Model;
import com.jguru.vertexai.domain.repository.ModelRepository;

public class ModelResolutionServiceImpl implements ModelResolutionService {
  private final ModelRepository modelRepository;

  public ModelResolutionServiceImpl(ModelRepository modelRepository) {
    this.modelRepository = modelRepository;
  }

  @Override
  public Model resolveModel(String alias) {
    if (alias == null || alias.trim().isEmpty()) {
      return null;
    }
    return modelRepository.findByAlias(alias.trim());
  }

  @Override
  public boolean isValidModelAlias(String alias) {
    if (alias == null || alias.trim().isEmpty()) {
      return false;
    }
    return modelRepository.existsByAlias(alias.trim());
  }
}
