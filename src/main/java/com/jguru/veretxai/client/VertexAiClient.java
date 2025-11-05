package com.jguru.veretxai.client;

import com.google.genai.client.Client;
import com.google.genai.client.GenerativeModel;
import com.google.genai.responses.GenerateContentResponse;

import java.io.IOException;

public class VertexAiClient {

    private final Client client;

    /**
     * Constructor for API Key authentication (Vertex AI Express Mode).
     */
    public VertexAiClient(String apiKey) {
        this.client = new Client.builder()
                .setApiKey(apiKey)
                .setVertexAI(true)
                .build();
    }

    /**
     * Constructor for Service Account authentication (via ADC).
     */
    public VertexAiClient(String projectId, String location) {
         this.client = new Client.builder()
                .setProject(projectId)
                .setLocation(location)
                .setVertexAI(true)
                .build();
    }

    public GenerateContentResponse callVertexAi(String modelName, String text) throws IOException {
        GenerativeModel model = this.client.getGenerativeModel(modelName);
        return model.generateContent(text);
    }
}
