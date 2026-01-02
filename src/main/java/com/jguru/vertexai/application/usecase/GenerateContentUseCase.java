package com.jguru.vertexai.application.usecase;

public interface GenerateContentUseCase {
  String execute(String modelAlias, String prompt);
}
