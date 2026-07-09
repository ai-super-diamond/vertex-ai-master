package com.jguru.vertexai.infrastructure.impl;

import com.jguru.vertexai.domain.entity.Model;
import com.jguru.vertexai.domain.repository.ModelRepository;
import java.util.HashMap;
import java.util.Map;

public class ModelRepositoryImpl implements ModelRepository {
  private final Map<String, Model> models;

  public ModelRepositoryImpl() {
    this.models = new HashMap<>();
    // Initialize with some default models
    models.put("gemini.pro", new Model("gemini.pro", "gemini-3.1-pro-preview"));
    models.put("gemini.flash", new Model("gemini.flash", "gemini-3.5-flash"));
    models.put("claude", new Model("claude", "claude-opus-4-8@default"));
  }

  @Override
  public Model findByAlias(String alias) {
    if (alias == null || alias.trim().isEmpty()) {
      return null;
    }
    return models.get(alias.trim());
  }

  @Override
  public boolean existsByAlias(String alias) {
    if (alias == null || alias.trim().isEmpty()) {
      return false;
    }
    return models.containsKey(alias.trim());
  }
}
