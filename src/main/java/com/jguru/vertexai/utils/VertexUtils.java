package com.jguru.vertexai.utils;

import com.google.genai.responses.GenerateContentResponse;
import com.jguru.veretxai.client.VertexAiClient;

import java.io.IOException;

public class VertexUtils {

    /**
     * Generates content using API Key authentication.
     */
    public static String generateContent(String apiKey, String modelName, String text) throws IOException {
        VertexAiClient client = new VertexAiClient(apiKey);
        GenerateContentResponse response = client.callVertexAi(modelName, text);
        return response.getText();
    }

    /**
     * Generates content using Service Account authentication.
     */
    public static String generateContent(String projectId, String location, String modelName, String text) throws IOException {
        VertexAiClient client = new VertexAiClient(projectId, location);
        GenerateContentResponse response = client.callVertexAi(modelName, text);
        return response.getText();
    }
}
