package com.jguru.vertexai.service.dto;

/**
 * Result object for content generation.
 */
public class GenerationResult {
  private final String content;
  private final boolean success;
  private final String errorMessage;

  public GenerationResult(String content, boolean success, String errorMessage) {
    this.content = content;
    this.success = success;
    this.errorMessage = errorMessage;
  }

  public GenerationResult(String content) {
    this(content, true, null);
  }

  public String getContent() {
    return content;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getText() {
    return content;
  }

  public static GenerationResult success(String content) {
    return new GenerationResult(content, true, null);
  }

  public static GenerationResult failure(String errorMessage) {
    return new GenerationResult(null, false, errorMessage);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String content;
    private boolean success = true;
    private String errorMessage;

    public Builder withContent(String content) {
      this.content = content;
      return this;
    }

    public Builder withText(String text) {
      this.content = text;
      return this;
    }

    public Builder withSuccess(boolean success) {
      this.success = success;
      return this;
    }

    public Builder withErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
      return this;
    }

    public GenerationResult build() {
      return new GenerationResult(content, success, errorMessage);
    }
  }
}
