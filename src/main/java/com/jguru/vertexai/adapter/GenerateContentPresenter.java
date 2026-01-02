package com.jguru.vertexai.adapter;

public class GenerateContentPresenter {

  public String presentSuccess(String content) {
    return "SUCCESS: " + content;
  }

  public String presentError(String error) {
    return "ERROR: " + error;
  }
}
