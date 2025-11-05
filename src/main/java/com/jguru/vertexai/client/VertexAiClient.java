package com.jguru.vertexai.client;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import java.io.IOException;

public class VertexAiClient {

    private final GenerativeModel vertexAiModel;
    private com.google.generativeai.client.GenerativeModel generativeModel;
    private final boolean isServiceAccount;
    private final String modelName;

    /**
     * Constructor for API Key authentication.
     *
     * @param apiKey The API key for authentication
     */
    public VertexAiClient(String apiKey) {
        this.modelName = "gemini-1.5-pro-001"; // default model
        this.generativeModel = new com.google.generativeai.client.GenerativeModel(modelName, apiKey);
        this.vertexAiModel = null;
        this.isServiceAccount = false;
    }

    /**
     * Constructor for Service Account authentication.
     *
     * @param serviceAccountKeyPath Path to service account JSON key
     * @param projectId             Google Cloud project ID
     * @param location              Google Cloud location
     */
    public VertexAiClient(String serviceAccountKeyPath, String projectId, String location) throws IOException {
        this.modelName = "gemini-1.5-pro-001"; // default model
        VertexAI vertexAI = new VertexAI(projectId, location);
        this.vertexAiModel = new GenerativeModel(modelName, vertexAI);
        this.generativeModel = null;
        this.isServiceAccount = true;
    }

    /**
     * Calls Vertex AI to generate content.
     *
     * @param modelName The model to use
     * @param text      The prompt text
     * @return Generated response
     */
    public GenerateContentResponse callVertexAi() throws IOException {
        if (isServiceAccount) {
            String responseText = ResponseHandler.getText(vertexAiModel.generateContent(ContentMaker.fromString(text)));
            // Wrap response in a compatible format
            return new GenerateContentResponse() {
                @Override
                public String text() {
                    return responseText;
                }
            };
        } else {
            return generativeModel.generateContent(text);
        }
    }

    public String callGemini(String modelName, String text) {
        // Instantiate the client. The client by default uses the Gemini API. It
        //  gets the API key from the environment variable `GOOGLE_API_KEY`.
        GenerateContentResponse response;
        try (Client client = new Client()) {

            response = client.models.generateContent(modelName, text, null);
        }

        // Gets the text string from the response by the quick accessor method `text()`.
       return response.text();
    }
}

