package com.jguru.vertexai.infrastructure.client;

import com.jguru.vertexai.service.RegionProviderImpl;
import com.jguru.vertexai.utils.PropertiesLoader;
import org.slf4j.LoggerFactory;
import com.jguru.vertexai.service.VertexAiService;
import com.jguru.vertexai.service.VertexAiServiceImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorldwideAvailabilityClientTest {

  @Test
  void shouldCreateWorldwideAvailabilityClient() {
    // Given: A real VertexAiService
    VertexAiService service = new VertexAiServiceImpl(
        new RegionProviderImpl(
            PropertiesLoader.load(LoggerFactory.getLogger(RegionProviderImpl.class), "regions.config", "regions.properties")),
        new VertexAiClientFactory(),
        PropertiesLoader.load(LoggerFactory.getLogger(VertexAiServiceImpl.class), "models.config", "models.properties"));
    WorldwideAvailabilityClient client = new WorldwideAvailabilityClient(service);

    // Then: Client should be created successfully
    assertThat(client).isNotNull();
  }

  @Test
  void shouldHaveCorrectPackage() {
    // Given: The client class
    VertexAiService service = new VertexAiServiceImpl(
        new RegionProviderImpl(
            PropertiesLoader.load(LoggerFactory.getLogger(RegionProviderImpl.class), "regions.config", "regions.properties")),
        new VertexAiClientFactory(),
        PropertiesLoader.load(LoggerFactory.getLogger(VertexAiServiceImpl.class), "models.config", "models.properties"));
    WorldwideAvailabilityClient client = new WorldwideAvailabilityClient(service);

    // Then: It should be in the correct package
    assertThat(client.getClass().getPackage().getName()).isEqualTo("com.jguru.vertexai.infrastructure.client");
  }
}
