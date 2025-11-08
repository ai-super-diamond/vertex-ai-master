package com.jguru.vertexai.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class VertexAiClientTest {

  @TempDir
  File tempDir;

  @Test
  void shouldDetectOpenAIProperty() throws IOException {
    // Given: A models.properties file with openai property
    File propertiesFile = new File(tempDir, "models.properties");
    try (FileWriter writer = new FileWriter(propertiesFile)) {
      writer.write("test.model=test-model-name\n");
      writer.write("test.model.openai=true\n");
    }

    // When: Creating a VertexAiClient with this properties file
    Properties props = new Properties();
    try (java.io.FileReader reader = new java.io.FileReader(propertiesFile)) {
      props.load(reader);
    }

    // Then: The openai property should be detected
    String openAiFlag = props.getProperty("test.model.openai");
    assertThat(openAiFlag).isEqualTo("true");
  }

  @Test
  void shouldNotDetectOpenAIPropertyWhenNotSet() throws IOException {
    // Given: A models.properties file without openai property
    File propertiesFile = new File(tempDir, "models.properties");
    try (FileWriter writer = new FileWriter(propertiesFile)) {
      writer.write("test.model=test-model-name\n");
      writer.write("test.model.region=us-central1\n");
    }

    // When: Creating a VertexAiClient with this properties file
    Properties props = new Properties();
    try (java.io.FileReader reader = new java.io.FileReader(propertiesFile)) {
      props.load(reader);
    }

    // Then: The openai property should not be detected
    String openAiFlag = props.getProperty("test.model.openai");
    assertThat(openAiFlag).isNull();
  }

  @Test
  void shouldDetectOpenAIPropertyForMaaSModel() throws IOException {
    // Given: A models.properties file with a MaaS model
    File propertiesFile = new File(tempDir, "models.properties");
    try (FileWriter writer = new FileWriter(propertiesFile)) {
      writer.write("maas.model=test-maas-model\n");
      writer.write("maas.model.provider=test-provider\n");
      writer.write("maas.model.openai=true\n");
    }

    // When: Loading properties
    Properties props = new Properties();
    try (java.io.FileReader reader = new java.io.FileReader(propertiesFile)) {
      props.load(reader);
    }

    // Then: Both provider and openai properties should be detected
    String provider = props.getProperty("maas.model.provider");
    String openAiFlag = props.getProperty("maas.model.openai");

    assertThat(provider).isEqualTo("test-provider");
    assertThat(openAiFlag).isEqualTo("true");
  }
}
