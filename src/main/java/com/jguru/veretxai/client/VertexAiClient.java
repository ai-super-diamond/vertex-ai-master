package com.jguru.veretxai.client;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;

import java.io.IOException;

public class VertexAiClient {

    private final GenerativeModel vertexAiModel;
    private com.google.generativeai.client.GenerativeModel generativeModel;
    private final boolean isServiceAccount;

    /**
     * Constructor for API Key authentication.
     */
    public VertexAiClient(String apiKey, String modelName) {
        this.generativeModel = new com.google.generativeai.client.GenerativeModel(modelName, apiKey);
        this.vertexAiModel = null;
        this.isServiceAccount = false;
    }

    /**
     * Constructor for Service Account authentication.
     */
    public VertexAiClient(String projectId, String location, String modelName) throws IOException {
        VertexAI vertexAI = new VertexAI(projectId, location);
        this.vertexAiModel = new GenerativeModel(modelName, vertexAI);
        this.generativeModel = null;
        this.isServiceAccount = true;
    }

    public String callVertexAi(String text) throws IOException {
        if (isServiceAccount) {
            return ResponseHandler.getText(vertexAiModel.generateContent(ContentMaker.fromString(text)));
        } else {
            return generativeModel.generateContent(text).getText();
        }
    }
}
