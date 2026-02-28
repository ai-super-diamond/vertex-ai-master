# Vertex AI Master CLI

A powerful command-line interface (CLI) for interacting with Google's Vertex AI generative models. Built with **Java 25**, **Picocli**, and a clean **layered architecture**, it supports both standard Vertex AI models and Model-as-a-Service (MaaS) offerings.

## 🚀 Overview

Vertex AI Master CLI provides a unified interface to experiment with and test various Large Language Models (LLMs) hosted on Google Cloud Vertex AI. 

**Key Features:**
- **Dual API Support:** Seamlessly interact with the standard Vertex AI SDK (Gemini, Llama) and the Chat Completions API (DeepSeek, Qwen via MaaS).
- **Availability Testing:** Verify model availability across 40+ GCP regions, specific clusters (US, EU), or worldwide.
- **Model Alias System:** Use short names for complex model IDs via a flexible `models.properties` configuration.
- **Advanced Authentication:** Supports API Keys, Service Account JSON files, and Application Default Credentials (ADC).
- **High Performance:** Optimized for Windows with optional **GraalVM Native Image** support for near-instant startup.
- **Output Management:** Built-in support for redirecting output and debugging API calls.

## 🛠️ Tech Stack

- **Language:** Java 25
- **CLI Framework:** [Picocli](https://picocli.info/) 4.7.7
- **SDKs:** 
  - Google Cloud GenAI SDK 1.32.0
  - Google Auth Library 1.40.0
- **Build System:** Maven 3.9+
- **Native Image:** GraalVM (for `vertex.exe`)
- **Logging:** SLF4J + Logback
- **Serialization:** GSON 2.13.2

## 📋 Prerequisites

- **Java Development Kit (JDK) 25:** Required for building and running the JAR.
- **Apache Maven 3.9+:** Required for project builds.
- **Google Cloud Project:** An active project with the **Vertex AI API** enabled.
- **GraalVM (Optional):** Required only for building the native Windows executable (`vertex.exe`).
- **Google Cloud SDK (gcloud):** Recommended for managing authentication and project settings.

## 🚀 Setup and Installation

### Building from Source

```powershell
# Clone the repository
git clone <repo-url>
cd vertex-ai-master

# Build the shaded JAR (skipping tests for speed)
mvn clean package -DskipTests
```

The resulting JAR will be located at `target/vertex-0.0.1-SNAPSHOT.jar` (or similar, depending on the version).

### Building the Native Executable (Windows)

Ensure your `JAVA_HOME` points to a GraalVM installation with `native-image` installed.

```powershell
.\bin\build-exe.cmd
```

This script runs the Maven native profile and moves the generated `vertex.exe` to the `bin/` directory.

## 💻 Usage

### Basic Commands

```powershell
# Using the native executable
.\bin\vertex.exe --project-id YOUR_PROJECT --location us-central1 -m gemini.pro "What is the capital of France?"

# Using the Java JAR directly
java -jar target/vertex-0.0.1-SNAPSHOT.jar --project-id YOUR_PROJECT -m gemini.pro "Hello!"
```

### Authentication Modes

1.  **API Key (Direct Gemini API):**
    ```powershell
    .\bin\vertex.exe --api-key YOUR_API_KEY -m gemini.pro "Tell me a joke"
    ```
2.  **Service Account Key:**
    ```powershell
    .\bin\vertex.exe --project-id PROJECT --sa-key-file keys/sa-key.json -m gemini.pro "Analyze this code."
    ```
3.  **Application Default Credentials (ADC):**
    ```powershell
    # Automatically uses credentials from 'gcloud auth application-default login'
    .\bin\vertex.exe --project-id PROJECT -m gemini.pro "Prompt"
    ```

### Availability & Region Checks

**Check a model across a specific cluster (US/EU/Global):**
```powershell
.\bin\vertex.exe --project-id PROJECT --check-all-regions --cluster US -m deepseek.r1 "Test"
```

**Worldwide availability check:**
```powershell
.\bin\vertex.exe --project-id PROJECT --worldwide -m gemini.pro "Connectivity test"
```

**Debug Mode:**
Add `--debug` to see full request/response details and internal routing logic.

## 📜 Scripts

| Script | Path | Description |
| :--- | :--- | :--- |
| `rebuild.cmd` | `/` | Full clean build and basic smoke test. |
| `build-exe.cmd` | `bin/` | Compiles the native Windows executable using GraalVM. |
| `vert.cmd` | `bin/` | Wrapper to run the JAR with a local `models.properties`. |
| `test-all-us.cmd` | `bin/` | Automated availability test for all models in US regions. |
| `test-all-eu.cmd` | `bin/` | Automated availability test for all models in EU regions. |
| `debug-all-us.cmd`| `bin/` | Same as `test-all-us.cmd` but with verbose debug output. |
| `debug-all-eu.cmd`| `bin/` | Same as `test-all-eu.cmd` but with verbose debug output. |
| `run-dry-new-versions.cmd` | `/` | Dry run for dependency updates via OpenRewrite. |
| `run-apply-new-versions.cmd` | `/` | Applies dependency updates via OpenRewrite. |

## ⚙️ Configuration

### `models.properties`

Model routing and aliases are defined in `src/main/resources/models.properties`. You can override this by placing a `models.properties` file in the same directory as the executable or using the `-model-file` flag.

Example configuration:
```properties
# Standard Vertex AI Model
gemini.pro=gemini-1.5-pro-001
gemini.pro.region=us-central1

# Model-as-a-Service (MaaS) Configuration
deepseek.r1=deepseek-r1-maas
deepseek.r1.provider=deepseek-ai
```

## 🌐 Environment Variables

| Variable | Purpose |
| :--- | :--- |
| `GOOGLE_APPLICATION_CREDENTIALS` | Path to your GCP Service Account JSON key. |
| `JAVA_HOME` | Should point to JDK 25 (or GraalVM for native builds). |
| `MAVEN_HOME` | Path to your Maven installation. |

## 🧪 Testing & Quality

```powershell
# Run unit tests
mvn test

# Run integration tests (requires GCP access)
mvn verify

# Code Formatting
mvn spotless:check   # Verify formatting
mvn spotless:apply   # Auto-fix formatting
```

## 📂 Project Structure

```text
├── bin/                    # Executables, helper scripts, and region configs
├── docs/                   # Architecture diagrams and deep-dive documentation
├── keys/                   # Recommended location for local Service Account keys
├── src/
│   ├── main/
│   │   ├── java/           # Clean Architecture implementation
│   │   └── resources/      # Default models.properties and logging setup
│   └── test/               # Unit and Integration test suites
├── pom.xml                 # Maven project definition
└── rebuild.cmd             # Master build script
```

## 🏛️ Architecture

The project follows a **3-tier Layered Architecture**:
1.  **Presentation (CLI):** `VertexAiMasterMain` handles Picocli parsing and user interaction.
2.  **Application/Service Layer:** `VertexAiService` manages business logic and model resolution.
3.  **Infrastructure/Client Layer:** `VertexAiClient` handles low-level API communication with Google Cloud.

## 🤖 Model Context Protocol (MCP)

This tool is compatible with MCP clients. 
- Integrated support for specialized prompts in `src/main/resources/prompts/`.
- Configurable output redirection for agent-based workflows.

## 📄 License

TODO: Specify license (e.g., Apache-2.0).

---
*Last updated: 2026-02-27*
