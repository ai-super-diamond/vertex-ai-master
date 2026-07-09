package com.jguru.vertexai.service.dto;

import java.util.Locale;

public enum ErrorType {
  NOT_FOUND_404("404 Not Found"), PERMISSION_DENIED_403("403 Permission Denied"), BAD_REQUEST_400("400 Bad Request"), RATE_LIMITED_429(
      "429 Rate Limited"), NETWORK_ERROR("Network Error"), INTERNAL_ERROR_500("500 Internal Error"), UNKNOWN_ERROR("UNKNOWN_ERROR");

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
    String normalized = msg.toLowerCase(Locale.ROOT);
    if (normalized.contains("404") || normalized.contains("not found") || normalized.contains("not_found")) {
      return NOT_FOUND_404;
    }
    if (normalized.contains("403") || normalized.contains("permission denied") || normalized.contains("forbidden")) {
      return PERMISSION_DENIED_403;
    }
    if (normalized.contains("400") || normalized.contains("bad request") || normalized.contains("invalid argument")) {
      return BAD_REQUEST_400;
    }
    if (normalized.contains("429") || normalized.contains("rate limit") || normalized.contains("resource exhausted")
        || normalized.contains("quota exceeded")) {
      return RATE_LIMITED_429;
    }
    if (normalized.contains("timeout") || normalized.contains("timed out") || normalized.contains("network")
        || normalized.contains("connection")) {
      return NETWORK_ERROR;
    }
    if (normalized.contains("500") || normalized.contains("internal error")) {
      return INTERNAL_ERROR_500;
    }
    return UNKNOWN_ERROR;
  }
  public String formatMessage(String original) {
    if (this != UNKNOWN_ERROR) {
      return getDisplayMessage();
    }
    if (original == null) {
      return "ERROR: Unknown error";
    }
    String shortMsg = original.length() > 100 ? original.substring(0, 100) + "..." : original;
    return "ERROR: " + shortMsg;
  }
}
