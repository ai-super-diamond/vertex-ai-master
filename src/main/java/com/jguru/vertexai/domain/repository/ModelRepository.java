package com.jguru.vertexai.domain.repository;

import com.jguru.vertexai.domain.entity.Model;

public interface ModelRepository {
  Model findByAlias(String alias);
  boolean existsByAlias(String alias);
}
