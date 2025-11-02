package com.example.demo;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;


@Command(name = "vertexai", mixinStandardHelpOptions = true, version = "vertexai 1.0",
        description = "Interacts with the Vertex AI API.")
public class VertexAiMasterMain implements Callable<Integer> {

    @Option(names = {"-saKey"}, description = "Path to the service account key JSON file.", required = true)
    private String saKeyPath;

    @Option(names = {"-region"}, description = "The Google Cloud region (e.g., us-central1).", required = true)
    private String region;

    @Option(names = {"-model"}, description = "The model name (e.g., gemini-1.5-pro-preview-0409).", required = true)
    private String modelName;

    @Option(names = {"-m"}, description = "The message to send to the model.", required = true)
    private String message;

    @Override
    public Integer call() throws Exception {
        try {
            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(new FileInputStream(saKeyPath));
            VertexAI vertexAI = new VertexAI("master-vertexai", region, credentials);
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
            GenerateContentResponse response = model.generateContent(message);
            System.out.println(response);

        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new VertexAiMasterMain()).execute(args);
        System.exit(exitCode);
    }
}
