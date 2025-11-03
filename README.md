# Vertex AI Master CLI

A command-line interface (CLI) for interacting with Google's Vertex AI generative models, built with Java, Picocli, and GraalVM.

## Prerequisites

Before you begin, ensure you have the following installed and configured:

1.  **Java Development Kit (JDK) 17:** This project requires Java 17. You can verify your installation by running `java -version`.
2.  **Apache Maven:** Used for project build and dependency management. Verify your installation with `mvn -v`.
3.  **Google Cloud SDK (gcloud):** While not strictly required to run the final executable, it is essential for managing your Google Cloud project and authentication. [Installation Guide](https://cloud.google.com/sdk/docs/install).
4.  **Google Cloud Project:** You need a Google Cloud project with the Vertex AI API enabled.
5.  **Service Account Key:**
    *   Create a service account in your Google Cloud project with the "Vertex AI User" role.
    *   Download the JSON key file for this service account.
6.  **GraalVM (for native executable):** To build the native Windows executable, you need GraalVM for JDK 17.
    *   [Download GraalVM](https://www.graalvm.org/downloads/).
    *   Set the `GRAALVM_HOME` environment variable to your GraalVM installation directory.
    *   Ensure the GraalVM `bin` directory is in your system's `PATH`.

## Configuration

1.  **Service Account:** The application requires credentials to authenticate with the Google Cloud API. You will need to pass the path to your service account JSON key file using the `--sa-key-file` option when running the application.

2.  **Models:** The available models are configured in the `src/main/resources/models.properties` file. You can add or modify the models in this file.

## Building and Running

### Running as a Java Application

You can run the application directly using Maven. This is useful for development and testing.

```sh
# Example command
mvn exec:java -Dexec.mainClass="com.example.demo.VertexAiMasterMain" -Dexec.args="--sa-key-file /path/to/your/key.json --model-key gemini.pro"
```

### Building the Native Executable (Windows)

A native executable offers faster startup times and can be run without a JVM.

1.  Ensure you have met all the prerequisites, especially GraalVM.
2.  Run the `build-exe.cmd` script in the project root:

```sh
.\build-exe.cmd
```

This script will compile the application into a native executable named `vertex.exe` and place it in the project root directory.

## Usage

Once you have the `vertex.exe` executable, you can run it from your command line.

The application uses default values for the project ID (`vertex-ai-project-skorec`), the region (`us-central1`) and the prompt (`Give me example snippet of Java virtual thread.`).

### Basic Usage (with defaults)

```sh
# You must provide your service account key
./vertex.exe --sa-key-file "C:\path\to\your\key.json" --model-key gemini.pro
```

### Overriding Defaults

You can override the default project, region and text prompt:

```sh
./vertex.exe --project another-gcp-project --region us-east1 --sa-key-file "C:\path\to\your\key.json" --model-key gemini.pro --text "What is the capital of France?"
```

### Using a Different Model

Specify a different model key from your `models.properties` file:

```sh
./vertex.exe --sa-key-file "C:\path\to\your\key.json" --model-key gemini.flash
```
