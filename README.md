# Vertex AI Master CLI

A powerful command-line interface (CLI) for interacting with Google's Vertex AI generative models. Built with **Java 25**, **Picocli**, and a clean **layered architecture**, it supports both standard Vertex AI models and Model-as-a-Service (MaaS) offerings.

## 🚀 Overview

Vertex AI Master CLI provides a unified interface to experiment with and test various Large Language Models (LLMs) hosted on Google Cloud Vertex AI. 

**Key Features:**
- **Multi-Provider Support:** Interact with Vertex AI models (Gemini), Anthropic models (Claude), and MaaS offerings via Chat Completions API.
- **Availability Testing:** Verify model availability across 40+ GCP regions, specific clusters (US, EU, Asia, Africa, Canada, South America, or Global), or worldwide.
- **Model Alias System:** Use short names for complex model IDs via a flexible `models.properties` configuration.
- **Advanced Authentication:** Supports API Keys, Service Account JSON files, and Application Default Credentials (ADC).
- **High Performance:** Optimized for Windows with optional **GraalVM Native Image** support for near-instant startup.
- **Output Management:** Built-in support for redirecting output and debugging API calls.

## 🛠️ Tech Stack

- **Language:** Java 25
- **CLI Framework:** [Picocli](https://picocli.info/) 4.7.7
- **SDKs:** 
  - Google Cloud GenAI SDK 1.60.0
  - Google Auth Library 1.48.0
- **Build System:** Maven 3.9+
- **Native Image:** GraalVM (for `vertex.exe`)
- **Logging:** SLF4J + Logback
- **Serialization:** GSON 2.14.0

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

The resulting JAR will be located at `target/vertex-1.0.1.jar`.

### Building the Native Executable (Windows)

Ensure your `JAVA_HOME` points to a GraalVM installation with `native-image` installed.

```powershell
.\bin\build-exe.cmd
```

This script runs the Maven native profile and moves the generated `vertex.exe` to the `bin/` directory.

## 💻 Usage

### Basic Commands

```powershell
# Using the Java JAR directly
java -jar target/vertex-1.0.1.jar --sa-key-file keys/sa_key.json --location us-central1 -m gemini.pro "What is the capital of France?"

# Or using the native executable (if built)
.\bin\vertex.exe --sa-key-file keys\sa_key.json --location us-central1 -m gemini.pro "What is the capital of France?"
```

### Authentication Modes

1.  **API Key (Direct Gemini API):**
    ```powershell
    .\bin\vertex.exe --api-key YOUR_API_KEY -m gemini.pro "Tell me a joke"
    ```
2.  **Service Account Key:**
    ```powershell
    java -jar target/vertex-1.0.1.jar --sa-key-file keys/sa_key.json -m gemini.pro "Analyze this code."
    ```
3.  **Application Default Credentials (ADC):**
    ```powershell
    # Automatically uses credentials from 'gcloud auth application-default login'
    .\bin\vertex.exe --location us-central1 -m gemini.pro "Prompt"
    ```

### Availability & Region Checks

**Check a model across a specific cluster (US/EU/ASIA/MIDDLE_EAST/AFRICA/CANADA/SOUTH_AMERICA/GLOBAL):**
```powershell
java -jar target/vertex-1.0.1.jar --sa-key-file keys/sa_key.json --check-all-regions --cluster US -m gemini.pro "Test"
```

**Worldwide availability check (all 42+ regions):**
```powershell
java -jar target/vertex-1.0.1.jar --sa-key-file keys/sa_key.json --worldwide -m gemini.pro "Connectivity test"
```

**Global endpoint check (models served only on the global endpoint):**
```powershell
java -jar target/vertex-1.0.1.jar --sa-key-file keys/sa_key.json --check-all-regions --cluster GLOBAL -m anthropic.sonnet5 "Test"
```

**Debug Mode:**
Add `--debug` to see full request/response details and internal routing logic.

## 📜 Scripts

| Script | Path | Description |
| :--- | :--- | :--- |
| `build-jar.cmd` / `build-jar.sh` | `bin/` | Build the shaded JAR and stage it as `bin/vertex-latest.jar`. |
| `build-exe.cmd` | `bin/` | Compiles the native Windows executable using GraalVM. |
| `doctor.cmd` / `doctor.sh` | `bin/` | Sanity-check the local toolchain (Maven, Java, jar, properties, key). |
| `test-all-us.cmd` / `test-all-us.sh` | `bin/` | Automated availability test for all models in US regions. |
| `test-all-eu.cmd` / `test-all-eu.sh` | `bin/` | Automated availability test for all models in EU regions. |
| `test-global.cmd` / `test-global.sh` | `bin/` | Automated availability test for all models on the global endpoint. |
| `test-worldwide.cmd` / `test-worldwide.sh` | `bin/` | Automated worldwide availability test (all 42+ regions). |
| `debug-all-us.cmd` / `debug-all-us.sh` | `bin/` | US region test with verbose debug output. |
| `debug-all-eu.cmd` / `debug-all-eu.sh` | `bin/` | EU region test with verbose debug output. |

## ⚙️ Configuration

### `models.properties`

Model routing and aliases are defined in `src/main/resources/models.properties`. You can override this by placing a `models.properties` file in the same directory as the executable or using the `-model-file` flag.

Example configuration:
```properties
# Google Gemini Models (Vertex AI SDK)
gemini.pro=gemini-3.1-pro-preview
gemini.flash=gemini-3.5-flash

# Anthropic Models (Chat Completions API)
anthropic.sonnet5=claude-sonnet-5@default
anthropic.sonnet5.provider=anthropic

anthropic.opus48=claude-opus-4-8@default
anthropic.opus48.provider=anthropic

# Model-as-a-Service (MaaS) Configuration (example; others commented out)
openai.gpt.oss.120b=gpt-oss-120b-maas
openai.gpt.oss.120b.provider=openai
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
*Last updated: 2026-07-02*
