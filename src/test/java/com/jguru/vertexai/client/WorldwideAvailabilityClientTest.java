package com.jguru.vertexai.client;

import com.jguru.vertexai.service.VertexAiService;
import com.jguru.vertexai.service.VertexAiServiceImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorldwideAvailabilityClientTest {

  @Test
  void shouldCreateWorldwideAvailabilityClient() {
    // Given: A real VertexAiService
    VertexAiService service = new VertexAiServiceImpl();
    WorldwideAvailabilityClient client = new WorldwideAvailabilityClient(service);

    // Then: Client should be created successfully
    assertThat(client).isNotNull();
  }

  @Test
  void shouldHaveCorrectPackage() {
    // Given: The client class
    WorldwideAvailabilityClient client = new WorldwideAvailabilityClient(new VertexAiServiceImpl());

    // Then: It should be in the correct package
    assertThat(client.getClass().getPackage().getName()).isEqualTo("com.jguru.vertexai.client");
  }
}
