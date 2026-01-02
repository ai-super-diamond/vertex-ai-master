package com.jguru.vertexai.domain.entity;

import java.util.Objects;

public class Model {

  private final String alias;
  private final String fullName;
  private final boolean isGlobal;

  public Model(String alias, String fullName) {
    this(alias, fullName, false);
  }

  public Model(String alias, String fullName, boolean isGlobal) {
    if (alias == null || alias.trim().isEmpty()) {
      throw new IllegalArgumentException("Alias cannot be null or empty");
    }
    if (fullName == null || fullName.trim().isEmpty()) {
      throw new IllegalArgumentException("Full name cannot be null or empty");
    }
    this.alias = alias.trim();
    this.fullName = fullName.trim();
    this.isGlobal = isGlobal;
  }

  public String getAlias() {
    return alias;
  }

  public String getFullName() {
    return fullName;
  }

  public boolean isGlobal() {
    return isGlobal;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Model model = (Model) o;
    return (isGlobal == model.isGlobal && Objects.equals(alias, model.alias) && Objects.equals(fullName, model.fullName));
  }

  @Override
  public int hashCode() {
    return Objects.hash(alias, fullName, isGlobal);
  }

  @Override
  public String toString() {
    return ("Model{" + "alias='" + alias + '\'' + ", fullName='" + fullName + '\'' + ", global=" + isGlobal + '}');
  }
}
