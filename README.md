# Vertex AI Master CLI

A command-line interface (CLI) for interacting with Google's Vertex AI generative models, built with Java, Picocli, and a clean layered architecture.

## 🚀 Overview

Vertex AI Master CLI provides a powerful interface to experiment with and test various LLMs hosted on Google Cloud Vertex AI. It supports both the standard Vertex AI SDK (for Gemini/Llama) and the Chat Completions API for Model-as-a-Service (MaaS) offerings like DeepSeek and Qwen.

**Key Features:**
- **Dual API Support:** Seamlessly switches between Vertex AI SDK and Chat Completions API.
- **Region Availability Testing:** Check model availability across 40+ GCP regions or specific clusters (US, EU, etc.).
- **Model Alias System:** Define short names for complex model IDs in `models.properties`.
- **Flexible Authentication:** Supports API Key, Service Account JSON, and Application Default Credentials (ADC).
- **Native Executable:** Built with GraalVM for near-instant startup on Windows.

## 🛠️ Tech Stack

- **Language:** Java 25
- **Framework:** [Picocli](https://picocli.info/) for CLI parsing
- **SDKs:** Google Cloud GenAI SDK, Google Auth Library
- **Build System:** Maven 3.9+
- **Native Image:** GraalVM (for `vertex.exe`)
- **Logging:** SLF4J + Logback

## 📋 Prerequisites

- **Java Development Kit (JDK) 25:** Ensure `java -version` shows version 25.
- **Apache Maven 3.9+:** For building the project.
- **Google Cloud Project:** A project with the Vertex AI API enabled.
- **GraalVM (Optional):** Required only if you want to build the native Windows executable (`vertex.exe`).
- **Google Cloud SDK (gcloud):** Recommended for managing credentials.

## 🚀 Setup and Installation

### Building from Source

```sh
# Clone the repository
git clone <repo-url>
cd vertex-ai-master

# Build the JAR
mvn clean package
```

### Building the Native Executable (Windows)

Ensure `GRAALVM_HOME` is set and points to your GraalVM installation.

```powershell
.\bin\build-exe.cmd
```

This generates `bin\vertex.exe`.

## 💻 Usage

### Basic Usage

```powershell
# Using the native executable
.\bin\vertex.exe --project-id YOUR_PROJECT --location us-central1 -m gemini.pro "What is the capital of France?"

# Using Maven (Development)
mvn exec:java -Dexec.mainClass="com.jguru.vertexai.VertexAiMasterMain" -Dexec.args="--project-id YOUR_PROJECT --location us-central1 -m gemini.pro 'Hello World'"
```

### Authentication Modes

1.  **API Key (Gemini API):**
    ```sh
    .\bin\vertex.exe --api-key YOUR_API_KEY -m gemini.pro "Tell me a joke"
    ```
2.  **Service Account Key:**
    ```sh
    .\bin\vertex.exe --project-id PROJECT --location us-central1 --sa-key-file path/to/key.json -m gemini.pro "Prompt"
    ```
3.  **Application Default Credentials (ADC):**
    ```sh
    # Ensure GOOGLE_APPLICATION_CREDENTIALS is set
    .\bin\vertex.exe --project-id PROJECT --location us-central1 -m gemini.pro "Prompt"
    ```

### Availability Checks

**Cluster-wide Check:**
```sh
.\bin\vertex.exe --project-id PROJECT --sa-key-file key.json --check-all-regions --cluster US -m deepseek.r1.0528 "Test"
```

**Worldwide Check:**
```sh
.\bin\vertex.exe --project-id PROJECT --sa-key-file key.json --worldwide -m gemini.pro "Test"
```

## 📜 Scripts

| Script | Description |
| :--- | :--- |
| `rebuild.cmd` | Cleans, builds the JAR, and runs a smoke test. |
| `bin\build-exe.cmd` | Compiles the application into a native Windows executable using GraalVM. |
| `bin\vert.cmd` | A wrapper to run the JAR with a local `models.properties` configuration. |
| `bin\test-all-us.cmd` | Batch script to test model availability across US regions. |
| `bin\test-all-eu.cmd` | Batch script to test model availability across EU regions. |
| `run-dry-new-versions.cmd` | OpenRewrite dry run to check for dependency updates. |
| `run-apply-new-versions.cmd` | OpenRewrite run to apply dependency updates. |

## ⚙️ Configuration

### `models.properties`

Located in `src/main/resources/models.properties`. It defines model aliases and their routing:

```properties
# Standard model
gemini.pro=gemini-1.5-pro-001
gemini.pro.region=us-central1

# MaaS model
deepseek.r1.0528=deepseek-r1-0528-maas
deepseek.r1.0528.provider=deepseek-ai
```

## 🌐 Environment Variables

| Variable | Purpose |
| :--- | :--- |
| `GOOGLE_APPLICATION_CREDENTIALS` | Path to your GCP Service Account JSON key (used by ADC). |
| `GRAALVM_HOME` | Path to GraalVM installation directory (required for native build). |
| `JAVA_HOME` | Path to JDK 25 installation. |

## 🧪 Testing

```sh
# Run unit tests
mvn test

# Run integration tests (may require credentials)
mvn verify
```

Quality checks:
- `mvn spotless:check`: Verify code formatting.
- `mvn spotless:apply`: Auto-format code.

## 📂 Project Structure

```text
├── bin/                    # Compiled executables and helper scripts
├── docs/                   # Additional documentation (Architecture, MCP, etc.)
├── keys/                   # (Optional) Local storage for service account keys
├── src/
│   ├── main/
│   │   ├── java/           # Layered architecture (Client, Service, DTO, Utils)
│   │   └── resources/      # models.properties and logging config
│   └── test/               # JUnit 5 test suite
├── pom.xml                 # Maven project configuration
└── rebuild.cmd             # Master rebuild script
```

## 🏛️ Architecture Overview

The application follows a **3-tier layered architecture**:

1.  **Presentation Layer (CLI):** `VertexAiMasterMain` - Picocli-based CLI handling user input.
2.  **Service Layer:** `VertexAiService` - Business logic, model resolution, and region management.
3.  **Client Layer:** `VertexAiClient` - Direct API communication (SDK or Chat Completions).
4.  **DTOs:** Data Transfer Objects for clean communication between layers.

## 🤖 Model Context Protocol (MCP)

This tool is designed to work with MCP-compatible clients.
- Current MCP config path: `c:\java\mcp-configs\qoder-mcp.json`
- Planned support for `--mcp-config` flag.

## 📄 License

TODO: Add license information (e.g., Apache 2.0 or MIT).

---
*Last updated: 2025-12-24*
