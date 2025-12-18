package com.jguru.vertexai.service.dto;

public enum ErrorType {
  NOT_FOUND_404("404 Not Found"), PERMISSION_DENIED_403("403 Permission Denied"), BAD_REQUEST_400("400 Bad Request"), INTERNAL_ERROR_500(
      "500 Internal Error"), UNKNOWN_ERROR("UNKNOWN_ERROR");

  private final String displayMessage;

  ErrorType(String displayMessage) {
    this.displayMessage = displayMessage;
  }

  public String getDisplayMessage() {
    return displayMessage;
  }

  public static ErrorType fromMessage(String msg) {
    if (msg == null) {
      return UNKNOWN_ERROR;
    }
    if (msg.contains("404")) {
      return NOT_FOUND_404;
    }
    if (msg.contains("403")) {
      return PERMISSION_DENIED_403;
    }
    if (msg.contains("400")) {
      return BAD_REQUEST_400;
    }
    if (msg.contains("500")) {
      return INTERNAL_ERROR_500;
    }
    return UNKNOWN_ERROR;
  }

  public String formatMessage(String original) {
    if (this != UNKNOWN_ERROR) {
      return getDisplayMessage();
    }
    String shortMsg = original.length() > 100 ? original.substring(0, 100) + "..." : original;
    return "ERROR: " + shortMsg;
  }
}
