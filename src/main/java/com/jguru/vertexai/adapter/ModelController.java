package com.jguru.vertexai.adapter;

public interface ModelController {
  String generateContent(String modelAlias, String prompt);
}
