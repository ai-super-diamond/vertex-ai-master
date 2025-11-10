# Vertex AI Master CLI

A command-line interface (CLI) for interacting with Google's Vertex AI generative models, built with Java, Picocli, and a clean layered architecture.

## Architecture Overview

The application follows a **3-tier layered architecture** for maintainability and testability:

1. **Presentation Layer (CLI):** `VertexAiMasterMain` - Picocli-based command-line interface handling user input
2. **Service Layer:** `VertexAiService` / `VertexAiServiceImpl` - Business logic, model resolution, region management
3. **Client Layer:** `VertexAiClient` - Direct API communication with Google Cloud (Vertex AI SDK & Chat Completions API)
4. **Data Transfer Objects (DTOs):** Request/response objects with Builder pattern for flexible construction

**Key Features:**
- Dual API support: Standard Vertex AI SDK for Gemini/Llama models, Chat Completions API for MaaS models (DeepSeek, Qwen, etc.)
- Automatic model routing based on configuration
- Region availability testing across 42 global GCP regions
- Model alias system via `models.properties`
- Three authentication modes: API Key, Service Account with explicit key, Application Default Credentials

## Prerequisites

Before you begin, ensure you have the following installed and configured:

1.  **Java Development Kit (JDK) 25:** This project requires Java 25. You can verify your installation by running `java -version`.
2.  **Apache Maven:** Used for project build and dependency management. Verify your installation with `mvn -v`.
3.  **Google Cloud SDK (gcloud):** While not strictly required to run the final executable, it is essential for managing your Google Cloud project and authentication. [Installation Guide](https://cloud.google.com/sdk/docs/install).
4.  **Google Cloud Project:** You need a Google Cloud project with the Vertex AI API enabled.
5.  **Service Account Key:**
    *   Create a service account in your Google Cloud project with the "Vertex AI User" role.
    *   Download the JSON key file for this service account.
6.  **GraalVM (for native executable):** To build the native Windows executable, you need GraalVM for JDK 25.
    *   [Download GraalVM](https://www.graalvm.org/downloads/).
    *   Set the `GRAALVM_HOME` environment variable to your GraalVM installation directory.
    *   Ensure the GraalVM `bin` directory is in your system's `PATH`.

## Quality Checklist

Run the following steps before sending code for review or publishing a release:

1. `mvn spotless:apply` – auto-format Java sources and tidy the `pom.xml`.
2. `mvn verify` – compile the project and execute the full JUnit suite.
3. `mvn -Pspotbugs verify` – optional static analysis run. Requires a SpotBugs-compatible JDK (21 or 22); with newer preview JDKs SpotBugs may fail to parse standard library classes.

The CLI also emits DEBUG-level diagnostics for model routing and credential usage when `logger` configuration enables the debug level, which helps triage misconfiguration quickly.

## Model Context Protocol (MCP)

### Local MCP configuration
- Current MCP config file path: `c:\java\mcp-configs\qoder-mcp.json`
- Once MCP is wired into the CLI, point the client to this file using a future `--mcp-config` flag or an environment variable like `MCP_CONFIG`.

Examples (planned):
- Using a CLI flag: `vertex-ai --mcp-config c:\\java\\mcp-configs\\qoder-mcp.json "Your prompt here"`
- Using an env var: `set MCP_CONFIG=c:\\java\\mcp-configs\\qoder-mcp.json && vertex-ai "Your prompt here"`

* when you need up-to-date information use MCP: **serper**
* when you need documentation use MCP: **context7**
* for sophisticated answers use MCP: **exa**
* for complex tasks use MCP: **sequential-thinking**

IMPORTANT: Test the tools of MCP servers and create your own rubric how and when to use and which one to use.

## Configuration

### Service Account

The application requires credentials to authenticate with the Google Cloud API. You will need to pass the path to your service account JSON key file using the `--sa-key-file` option when running the application.

**Important:** When `--sa-key-file` is explicitly provided, the application will **NOT** fall back to Application Default Credentials (ADC) if the key file is invalid or malformed. The application will fail immediately with a clear error message. This ensures explicit credential validation and prevents unintended authentication via ADC.

### Authentication Types

The application supports three authentication modes (defined in `AuthenticationType` enum):

1. **API_KEY:** Direct Gemini API access using `--api-key`
2. **SERVICE_ACCOUNT_EXPLICIT_KEY:** Vertex AI access with explicit JSON key file using `--sa-key-file`
3. **SERVICE_ACCOUNT_ADC:** Vertex AI access using Application Default Credentials (fallback when no explicit key provided)

### Model Configuration

Available models are configured in `src/main/resources/models.properties`. The file contains:

- **Model aliases:** Short names that resolve to full model IDs (e.g., `gemini.pro=gemini-2.5-pro`)
- **Regional configuration:** Each model has a `.region` property specifying deployment location
- **Provider prefixes:** MaaS models have a `.provider` property for Chat Completions API routing

**Example entries:**
```properties
# Standard Vertex AI model
gemini.pro=gemini-2.5-pro
gemini.pro.region=us-central1

# MaaS model (requires Chat Completions API)
deepseek.r1.0528=deepseek-r1-0528-maas
deepseek.r1.0528.region=us-central1
deepseek.r1.0528.provider=deepseek-ai
```

**Supported models (as of latest):**
- Gemini: 2.5 Pro, 2.5 Flash, 2.0 Flash Lite
- Llama: 3.1 (405B, 70B), 3.3 70B, 4 Maverick, 4 Scout
- DeepSeek: R1
- Qwen: Qwen3 235B, Qwen3 Coder 480B
- OpenAI: GPT OSS 120B

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

Once you have the `vertex.exe` executable (or JAR file), you can run it from your command line.

### Basic Content Generation

**Using Service Account with explicit key:**
```sh
# Basic usage with model alias
./vertex.exe --project-id vertex-ai-project-skorec --location us-central1 --sa-key-file "C:\path\to\key.json" --model-name gemini.pro "What is the capital of France?"

# Short flags
./vertex.exe --project-id PROJECT --location us-central1 --sa-key-file key.json -m gemini.flash "Explain quantum computing"
```

**Using API Key (Gemini API):**
```sh
./vertex.exe --api-key YOUR_API_KEY --model-name gemini.pro "Write a haiku about AI"
```

**Using Application Default Credentials:**
```sh
# Ensure GOOGLE_APPLICATION_CREDENTIALS is set in environment
./vertex.exe --project-id PROJECT --location us-central1 -m gemini.pro "Hello world"
```

### Model Selection

Use model aliases from `models.properties` or full model names:

```sh
# Using alias
./vertex.exe --sa-key-file key.json --project-id PROJECT --location us-central1 -m gemini.flash "Your prompt"

# Using full model name
./vertex.exe --sa-key-file key.json --project-id PROJECT --location us-central1 -m gemini-2.5-flash "Your prompt"

# MaaS models (auto-routed to Chat Completions API)
./vertex.exe --sa-key-file key.json --project-id PROJECT --location us-central1 -m deepseek.r1.0528 "200+200*99=?"
./vertex.exe --sa-key-file key.json --project-id PROJECT --location us-south1 -m qwen3.coder.480b.a35b "Write quicksort in Python"
```

### Region Availability Check

Test model availability across all regions in a geographic cluster:

```sh
# Check DeepSeek R1 in all US regions
./vertex.exe --project-id PROJECT --location us-central1 --sa-key-file key.json --check-all-regions --cluster US --model-name deepseek.r1.0528 "Test prompt"

# Short flags - check Qwen in EU regions
./vertex.exe --project-id PROJECT --location eu --sa-key-file key.json -car -c EU -m qwen3.coder.480b.a35b "Test"

# Available clusters: US, EU, ASIA, MIDDLE_EAST, AFRICA, CANADA, SOUTH_AMERICA
```

### Worldwide Region Availability Check

Test model availability across all worldwide regions (42 GCP regions):

```sh
# Check Gemini Pro availability worldwide
./vertex.exe --project-id PROJECT --location us-central1 --sa-key-file key.json --worldwide --model-name gemini.pro "Test prompt"

# Short flags
./vertex.exe --project-id PROJECT --location us-central1 --sa-key-file key.json -w -m gemini.flash "Test"
```

**Worldwide check output:**
```
=== Worldwide Region Availability Check ===
Model: gemini.pro
Test prompt: 200+200*99=?

Testing...

=== Results ===
✓ us-central1: SUCCESS
✓ us-east1: SUCCESS
✗ us-west1: 404 Not Found
...

=== Summary ===
Total: 42
Success: 15
Failed: 27
```

## Command-Line Options

### Authentication (mutually exclusive groups)

**API Key authentication:**
- `--api-key KEY` - Your Gemini API key

**Service Account authentication:**
- `--project-id PROJECT` - Google Cloud project ID (required)
- `--location REGION` - GCP region (e.g., us-central1) (required)
- `--sa-key-file PATH` - Path to service account JSON key file (optional, uses ADC if omitted)

### Model Selection

- `--model-name MODEL`, `-m MODEL` - Model alias or full name (default: gemini-1.5-pro-001)

### Prompt Input

- `TEXT` - Positional argument for prompt text
- `--text TEXT`, `-t TEXT` - Named option for prompt text

### Region Check Mode

- `--check-all-regions`, `-car` - Enable region availability testing
- `--cluster NAME`, `-c NAME` - Geographic cluster to test (US, EU, ASIA, MIDDLE_EAST, AFRICA, CANADA, SOUTH_AMERICA)

### Worldwide Region Check Mode

- `--worldwide`, `-w` - Enable worldwide region availability testing across all 42 GCP regions

### General Options

- `--help`, `-h` - Show help message
- `--version`, `-V` - Show version information

## Development

### Project Structure

```
src/main/java/com/jguru/vertexai/
├── VertexAiMasterMain.java          # CLI entry point
├── client/
│   ├── VertexAiClient.java          # API client with routing logic
│   ├── ChatCompletionsClient.java   # MaaS Chat Completions API client
│   └── WorldwideAvailabilityClient.java # Worldwide region testing client
├── service/
│   ├── VertexAiService.java         # Service interface
│   ├── VertexAiServiceImpl.java     # Business logic implementation
│   └── dto/
│       ├── AuthenticationConfig.java
│       ├── AuthenticationType.java
│       ├── GenerationRequest.java
│       ├── GenerationResult.java
│       ├── RegionCheckRequest.java
│       └── RegionCheckResult.java
└── utils/
    └── VertexUtils.java             # Utility methods

src/main/resources/
└── models.properties                # Model configuration

src/test/java/com/jguru/vertexai/
├── VertexAiMasterMainTest.java     # Integration tests
└── client/
    └── WorldwideAvailabilityClientTest.java # Unit tests for worldwide client
```

### Running Tests

```sh
# Run all unit tests
d:\java\maven\bin\mvn.cmd test

# Run integration tests (requires service account key)
d:\java\maven\bin\mvn.cmd test "-Drun.integration.tests=true"
```

### Building from Source

```sh
# Compile
d:\java\maven\bin\mvn.cmd clean compile

# Package JAR
d:\java\maven\bin\mvn.cmd clean package

# Run JAR directly
java -jar target/demo-0.0.1-SNAPSHOT.jar --help
```
