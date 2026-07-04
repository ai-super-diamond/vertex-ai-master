package com.jguru.vertexai.infrastructure.client;

import com.google.genai.Client;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ChatCompletionsClientTest {

  private Client extractSdkClient(ChatCompletionsClient client) throws Exception {
    Field clientField = ChatCompletionsClient.class.getDeclaredField("client");
    clientField.setAccessible(true);
    return (Client) clientField.get(client);
  }

  private String baseUrl(Client client) throws Exception {
    Method baseUrlMethod = Client.class.getDeclaredMethod("baseUrl");
    baseUrlMethod.setAccessible(true);
    Optional<?> baseUrl = (Optional<?>) baseUrlMethod.invoke(client);
    return (String) baseUrl.orElseThrow();
  }

  @Test
  void shouldUseMultiRegionEndpointForEuLocation() throws Exception {
    ChatCompletionsClient chatClient = new ChatCompletionsClient("test-project", "eu",
        mock(com.google.auth.oauth2.GoogleCredentials.class));

    Client sdkClient = extractSdkClient(chatClient);

    assertThat(baseUrl(sdkClient)).isEqualTo("https://aiplatform.eu.rep.googleapis.com");
    assertThat(sdkClient.location()).isEqualTo("eu");
  }
}
