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
    models.put("gemini.pro", new Model("gemini.pro", "gemini-1.5-pro-001"));
    models.put("gpt4", new Model("gpt4", "gpt-4-turbo"));
    models.put("claude", new Model("claude", "claude-3-opus"));
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
