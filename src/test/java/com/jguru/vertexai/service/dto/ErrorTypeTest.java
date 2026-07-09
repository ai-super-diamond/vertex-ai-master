package com.jguru.vertexai.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ErrorTypeTest {

  @Test
  void shouldFormatUnknownErrorWithNullMessageWithoutThrowing() {
    ErrorType errorType = ErrorType.fromMessage(null);

    assertThat(errorType).isEqualTo(ErrorType.UNKNOWN_ERROR);
    assertThat(errorType.formatMessage(null)).isEqualTo("ERROR: Unknown error");
  }

  @Test
  void shouldTruncateLongUnknownErrorMessages() {
    String longMessage = "x".repeat(150);

    String formatted = ErrorType.UNKNOWN_ERROR.formatMessage(longMessage);

    assertThat(formatted).isEqualTo("ERROR: " + "x".repeat(100) + "...");
  }

  @Test
  void shouldReturnDisplayMessageForKnownErrorTypesRegardlessOfOriginal() {
    assertThat(ErrorType.NOT_FOUND_404.formatMessage(null)).isEqualTo("404 Not Found");
  }

  @Test
  void shouldExposeRateLimitedAndNetworkErrorDisplayMessages() {
    assertThat(ErrorType.RATE_LIMITED_429.getDisplayMessage()).isEqualTo("429 Rate Limited");
    assertThat(ErrorType.NETWORK_ERROR.getDisplayMessage()).isEqualTo("Network Error");
  }

  @Test
  void shouldClassifyCommonNonNumericFallbackMessages() {
    assertThat(ErrorType.fromMessage("model not found in this region")).isEqualTo(ErrorType.NOT_FOUND_404);
    assertThat(ErrorType.fromMessage("permission denied for service account")).isEqualTo(ErrorType.PERMISSION_DENIED_403);
    assertThat(ErrorType.fromMessage("rate limit exceeded")).isEqualTo(ErrorType.RATE_LIMITED_429);
    assertThat(ErrorType.fromMessage("request timeout while connecting")).isEqualTo(ErrorType.NETWORK_ERROR);
  }
}
