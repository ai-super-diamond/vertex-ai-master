package com.jguru.vertexai.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class GenerateContentPresenterTest {

  @Test
  @DisplayName("Should format successful response correctly")
  public void shouldFormatSuccessfulResponseCorrectly() {
    // Arrange
    GenerateContentPresenter presenter = new GenerateContentPresenter();

    // Act
    String formattedResponse = presenter.presentSuccess("Generated content response");

    // Assert
    assertNotNull(formattedResponse);
    assertTrue(formattedResponse.contains("Generated content response"));
    assertTrue(formattedResponse.startsWith("SUCCESS:"));
  }

  @Test
  @DisplayName("Should format error response correctly")
  public void shouldFormatErrorResponseCorrectly() {
    // Arrange
    GenerateContentPresenter presenter = new GenerateContentPresenter();

    // Act
    String formattedResponse = presenter.presentError("Model not found");

    // Assert
    assertNotNull(formattedResponse);
    assertTrue(formattedResponse.contains("Model not found"));
    assertTrue(formattedResponse.startsWith("ERROR:"));
  }

  @Test
  @DisplayName("Should handle null success message gracefully")
  public void shouldHandleNullSuccessMessageGracefully() {
    // Arrange
    GenerateContentPresenter presenter = new GenerateContentPresenter();

    // Act
    String formattedResponse = presenter.presentSuccess(null);

    // Assert
    assertNotNull(formattedResponse);
    assertTrue(formattedResponse.contains("null"));
    assertTrue(formattedResponse.startsWith("SUCCESS:"));
  }

  @Test
  @DisplayName("Should handle null error message gracefully")
  public void shouldHandleNullErrorMessageGracefully() {
    // Arrange
    GenerateContentPresenter presenter = new GenerateContentPresenter();

    // Act
    String formattedResponse = presenter.presentError(null);

    // Assert
    assertNotNull(formattedResponse);
    assertTrue(formattedResponse.contains("null"));
    assertTrue(formattedResponse.startsWith("ERROR:"));
  }

  @Test
  @DisplayName("Should format response with proper structure")
  public void shouldFormatResponseWithProperStructure() {
    // Arrange
    GenerateContentPresenter presenter = new GenerateContentPresenter();

    // Act
    String successResponse = presenter.presentSuccess("Test content");
    String errorResponse = presenter.presentError("Test error");

    // Assert
    assertTrue(successResponse.matches("SUCCESS: Test content"));
    assertTrue(errorResponse.matches("ERROR: Test error"));
  }
}
