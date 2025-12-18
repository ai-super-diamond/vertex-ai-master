# Installation and Setup

<cite>
**Referenced Files in This Document**
- [README.md](file://README.md)
- [pom.xml](file://pom.xml)
- [build-exe.cmd](file://build-exe.cmd)
- [rebuild.cmd](file://rebuild.cmd)
- [vert.cmd](file://vert.cmd)
- [VertexAiMasterMain.java](file://src/main/java/com/jguru/vertexai/VertexAiMasterMain.java)
- [models.properties](file://src/main/resources/models.properties)
- [PropertiesLoader.java](file://src/main/java/com/jguru/vertexai/utils/PropertiesLoader.java)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Environment Setup](#environment-setup)
4. [Installation Methods](#installation-methods)
5. [Configuration](#configuration)
6. [Verification](#verification)
7. [Performance Considerations](#performance-considerations)
8. [Troubleshooting](#troubleshooting)
9. [Advanced Setup](#advanced-setup)

## Introduction

The Vertex AI Master CLI is a powerful command-line interface for interacting with Google's Vertex AI generative models. It provides dual API support for both standard Vertex AI SDK models (Gemini, Llama) and MaaS (Model-as-a-Service) models (DeepSeek, Qwen, etc.) through the Chat Completions API. The application supports multiple deployment options, including Java JAR execution and native executable compilation using GraalVM.

## Prerequisites

Before installing the Vertex AI Master CLI, ensure your system meets all the following requirements:

### Essential Software Requirements

| Requirement | Version | Purpose | Verification Command |
|-------------|---------|---------|---------------------|
| **Java Development Kit (JDK)** | Java 25 | Runtime environment | `java -version` |
| **Apache Maven** | Latest | Build and dependency management | `mvn -v` |
| **Google Cloud SDK** | Latest | Cloud project management | `gcloud --version` |
| **GraalVM (Optional)** | JDK 25 variant | Native executable compilation | `gu --version` |

### Additional Requirements

| Component | Description | Configuration Steps |
|-----------|-------------|-------------------|
| **Google Cloud Project** | Active GCP project with Vertex AI API enabled | [Setup Guide](https://cloud.google.com/vertex-ai/docs/start/cloud-console) |
| **Service Account** | IAM service account with Vertex AI User role | [Create Service Account](https://cloud.google.com/iam/docs/service-accounts-create) |
| **JSON Key File** | Service account credentials in JSON format | Download from IAM console |
| **Model Configuration** | models.properties file for model aliases | Included in project resources |

**Section sources**
- [README.md](file://README.md#L21-L35)
- [pom.xml](file://pom.xml#L10-L17)

## Environment Setup

### Java Development Kit (JDK) 25

The project requires Java 25 for optimal compatibility. Verify your installation:

```bash
# Check Java version
java -version

# Expected output should show Java 25.x.x
# Example: openjdk version "25" 2024-09-17
```

### Apache Maven Configuration

Ensure Maven is properly installed and configured:

```bash
# Verify Maven installation
mvn -v

# Expected output includes Maven version, Java version, and OS details
```

### Google Cloud SDK Setup

Install and configure the Google Cloud SDK for cloud operations:

```bash
# Install Google Cloud SDK (download from official website)
# Configure gcloud CLI
gcloud init

# Authenticate with Google Cloud
gcloud auth login

# Set default project
gcloud config set project YOUR_PROJECT_ID
```

### GraalVM Native Compilation Setup

For building native executables, install GraalVM for JDK 25:

1. **Download GraalVM**: Visit [GraalVM Downloads](https://www.graalvm.org/downloads/)
2. **Install GraalVM**: Extract to your preferred installation directory
3. **Configure Environment Variables**:

```powershell
# Set GRAALVM_HOME environment variable
set GRAALVM_HOME=C:\path\to\graalvm

# Add GraalVM bin directory to PATH
set PATH=%GRAALVM_HOME%\bin;%PATH%

# Verify GraalVM installation
gu --version
```

### Environment Variable Configuration

Create a comprehensive environment setup script:

```batch
@echo off
REM Environment setup for Vertex AI Master CLI

REM Set Java Home
set JAVA_HOME=C:\path\to\jdk-25
set PATH=%JAVA_HOME%\bin;%PATH%

REM Set Maven Home
set MAVEN_HOME=C:\path\to\apache-maven
set PATH=%MAVEN_HOME%\bin;%PATH%

REM Set GraalVM Home (if using native compilation)
set GRAALVM_HOME=C:\path\to\graalvm
set PATH=%GRAALVM_HOME%\bin;%PATH%

REM Set Google Cloud SDK
set CLOUDSDK_CORE_DISABLE_PROMPTS=1
set CLOUDSDK_PYTHON=C:\Python39\python.exe

REM Verify installations
echo Verifying installations...
java -version
mvn -v
gcloud --version
gu --version

echo Environment setup complete.
```

**Section sources**
- [README.md](file://README.md#L25-L35)
- [build-exe.cmd](file://build-exe.cmd#L1-L24)

## Installation Methods

### Method 1: Running as a Java Application

Execute the CLI directly using Maven for development and testing:

```bash
# Clone the repository
git clone https://github.com/your-repository/vertex-ai-master.git
cd vertex-ai-master

# Run using Maven (development mode)
mvn exec:java -Dexec.mainClass="com.jguru.vertexai.VertexAiMasterMain" \
-Dexec.args="--sa-key-file /path/to/your/key.json --model-key gemini.pro"

# Example with service account authentication
mvn exec:java -Dexec.mainClass="com.jguru.vertexai.VertexAiMasterMain" \
-Dexec.args="--project-id your-project-id --location us-central1 --sa-key-file key.json -m gemini.pro 'What is AI?'"
```

### Method 2: Building the Native Executable

Compile the application into a native Windows executable for optimal performance:

```bash
# Navigate to project directory
cd path\to\vertex-ai-master

# Run the build script
.\build-exe.cmd

# Expected output:
# --- Building Native Executable ---
# --- Moving Executable to Project Root ---
# --- Build Complete ---
# Your executable 'vertex.exe' is ready in the project root.
```

The build process performs the following steps:
1. Activates the Maven native profile (`-Pnative`)
2. Compiles the application using GraalVM
3. Creates a native Windows executable (`vertex.exe`)
4. Moves the executable to the project root directory

### Method 3: Building JAR Package

Create a portable JAR file for distribution:

```bash
# Clean and package the application
mvn clean package

# Expected output includes:
# [INFO] Building jar: target/vertex-0.0.1-SNAPSHOT.jar
```

**Section sources**
- [README.md](file://README.md#L109-L129)
- [build-exe.cmd](file://build-exe.cmd#L1-L24)

## Configuration

### Service Account Configuration

Configure authentication using service accounts:

```bash
# Basic service account usage
./vertex.exe --project-id YOUR_PROJECT_ID --location us-central1 \
--sa-key-file "C:\path\to\service-account-key.json" \
--model-name gemini.pro "Your prompt here"

# Short flags version
./vertex.exe --project-id PROJECT --location us-central1 \
--sa-key-file key.json -m gemini.flash "Explain quantum computing"
```

### API Key Authentication

Use Gemini API keys for direct access:

```bash
# API key authentication
./vertex.exe --api-key YOUR_API_KEY --model-name gemini.pro "Write a haiku about AI"
```

### Application Default Credentials

Use Google Cloud's default authentication mechanism:

```bash
# Ensure GOOGLE_APPLICATION_CREDENTIALS is set
set GOOGLE_APPLICATION_CREDENTIALS=C:\path\to\service-account-key.json

# Run with ADC
./vertex.exe --project-id PROJECT --location us-central1 -m gemini.pro "Hello world"
```

### Model Configuration

The application uses a model alias system defined in `models.properties`:

```properties
# Standard Vertex AI model
gemini.pro=gemini-2.5-pro
gemini.pro.region=us-central1

# MaaS model (requires Chat Completions API)
deepseek.r1.0528=deepseek-r1-0528-maas
deepseek.r1.0528.region=us-central1
deepseek.r1.0528.provider=deepseek-ai
```

**Section sources**
- [README.md](file://README.md#L66-L98)
- [models.properties](file://src/main/resources/models.properties#L1-L72)
- [VertexAiMasterMain.java](file://src/main/java/com/jguru/vertexai/VertexAiMasterMain.java#L29-L66)

## Verification

### Installation Verification Commands

Verify your installation using these commands:

```bash
# Check Java version
java -version
# Expected: OpenJDK Runtime Environment (build 25.xxx)

# Check Maven version
mvn -v
# Expected: Apache Maven 3.x.x

# Check GraalVM version (if installed)
gu --version
# Expected: GraalVM Native Image version 25.x.x

# Verify CLI help
./vertex.exe --help
# Expected: Displays CLI usage information

# Test basic functionality
./vertex.exe --api-key YOUR_API_KEY --model-name gemini.pro "Hello"
# Expected: Returns a response from the model
```

### Environment Variable Verification

```batch
REM Check environment variables
echo GRAALVM_HOME=%GRAALVM_HOME%
echo PATH=%PATH%

REM Verify Maven path
where mvn
REM Expected: Full path to mvn.bat

REM Verify Java path
where java
REM Expected: Full path to java.exe
```

### Model Configuration Verification

```bash
# Test model configuration loading
./vertex.exe --model-file ./src/main/resources/models.properties --help

# Verify specific model availability
./vertex.exe --project-id YOUR_PROJECT_ID --location us-central1 \
--sa-key-file key.json --model-name gemini.pro --help
```

**Section sources**
- [README.md](file://README.md#L25-L35)
- [rebuild.cmd](file://rebuild.cmd#L32-L47)

## Performance Considerations

### JAR Execution vs Native Executable

Choose the appropriate deployment method based on your requirements:

| Aspect | JAR Execution | Native Executable |
|--------|---------------|-------------------|
| **Startup Time** | ~2-3 seconds | ~100-200 milliseconds |
| **Memory Usage** | Higher (JVM overhead) | Lower (optimized) |
| **Build Time** | Fast (no compilation) | Slower (native compilation) |
| **Distribution** | Single JAR file | Platform-specific executable |
| **Dependencies** | Bundled JAR | Self-contained binary |

### Performance Comparison

```bash
# Measure JAR execution time
time java -jar target/vertex-0.0.1-SNAPSHOT.jar --help

# Measure native executable time
time vertex.exe --help
```

### Startup Time Optimization

For applications requiring fast startup times:
- Use native executable for production deployments
- Minimize JVM arguments during startup
- Consider lazy initialization patterns

### Memory Optimization

Monitor memory usage patterns:
- JAR execution: ~50-100MB baseline
- Native executable: ~20-40MB baseline
- Large models may require additional heap space

**Section sources**
- [README.md](file://README.md#L118-L129)
- [build-exe.cmd](file://build-exe.cmd#L1-L24)

## Troubleshooting

### Common Setup Issues

#### GraalVM Configuration Problems

**Issue**: Native compilation fails with GraalVM errors
```bash
# Problem: GRAALVM_HOME not set correctly
# Solution: Verify GraalVM installation
echo %GRAALVM_HOME%
where gu

# Problem: GraalVM version mismatch
# Solution: Ensure GraalVM JDK 25 matches project Java version
gu list | findstr java
```

#### Maven Dependency Resolution Errors

**Issue**: Maven fails to resolve dependencies
```bash
# Clear local Maven cache
mvn dependency:purge-local-repository

# Force update dependencies
mvn clean install -U

# Check internet connectivity for repository access
mvn help:system
```

#### Authentication Failures

**Issue**: Service account authentication fails
```bash
# Verify service account key file
type C:\path\to\key.json

# Check service account permissions
gcloud iam service-accounts describe YOUR_SERVICE_ACCOUNT

# Test authentication
gcloud auth activate-service-account --key-file=key.json
```

### Environment Variable Issues

**Issue**: PATH not configured correctly
```batch
REM Check PATH variable
echo %PATH%

REM Temporary PATH addition
set PATH=%PATH%;C:\path\to\graalvm\bin

REM Permanent PATH modification
setx PATH "%PATH%;C:\path\to\graalvm\bin"
```

### Model Configuration Problems

**Issue**: Model aliases not recognized
```bash
# Verify model configuration
./vertex.exe --model-file ./src/main/resources/models.properties --help

# Check model file existence
dir ./src/main/resources/models.properties

# Test with full model names
./vertex.exe --project-id YOUR_PROJECT_ID --location us-central1 \
--sa-key-file key.json --model-name gemini-2.5-pro "Test"
```

### Debug Mode for Troubleshooting

Enable debug logging for detailed error information:

```bash
# Enable debug mode
./vertex.exe --debug --project-id YOUR_PROJECT_ID --location us-central1 \
--sa-key-file key.json --model-name gemini.pro "Test"

# Check logs for detailed error messages
# Look for authentication failures, model routing issues, or network errors
```

**Section sources**
- [README.md](file://README.md#L21-L35)
- [VertexAiMasterMain.java](file://src/main/java/com/jguru/vertexai/VertexAiMasterMain.java#L83-L85)

## Advanced Setup

### Custom Model Configuration

Create custom model configurations for specialized use cases:

```properties
# Custom model configuration
custom.model=my-custom-model
custom.model.region=us-west1
custom.model.provider=custom-provider
custom.model.openai=true
```

### Regional Deployment

Configure models for specific geographic regions:

```bash
# Test model availability across regions
./vertex.exe --project-id YOUR_PROJECT_ID --location us-central1 \
--sa-key-file key.json --check-all-regions --cluster US --model-name gemini.pro "Test"

# Worldwide availability testing
./vertex.exe --project-id YOUR_PROJECT_ID --location us-central1 \
--sa-key-file key.json --worldwide --model-name gemini.pro "Test"
```

### Automated Build Pipeline

Integrate with CI/CD systems:

```yaml
# GitHub Actions example
name: Build Vertex AI Master
on: [push, pull_request]
jobs:
  build:
    runs-on: windows-latest
    steps:
    - uses: actions/checkout@v2
    - name: Setup Java
      uses: actions/setup-java@v2
      with:
        java-version: '25'
    - name: Setup GraalVM
      uses: graalvm/setup-graalvm@v1
      with:
        java-version: '25'
        distribution: 'graalvm'
    - name: Build native executable
      run: ./build-exe.cmd
    - name: Upload artifact
      uses: actions/upload-artifact@v2
      with:
        name: vertex-cli
        path: vertex.exe
```

### Container Deployment

Package the CLI for containerized environments:

```dockerfile
FROM mcr.microsoft.com/openjdk/jdk:25-windowsservercore-ltsc2022

# Install GraalVM
RUN curl -L https://github.com/graalvm/graalvm-community/releases/download/jdk-25.0.2/graalvm-community-jdk-25.0.2_windows-x64.zip -o graalvm.zip && \
    unzip graalvm.zip -d /opt && \
    rm graalvm.zip

ENV GRAALVM_HOME=/opt/graalvm-community-jdk-25.0.2
ENV PATH=$PATH:$GRAALVM_HOME/bin

# Copy application
COPY . /app
WORKDIR /app

# Build native executable
RUN ./build-exe.cmd

# Set entrypoint
ENTRYPOINT ["./vertex.exe"]
```

**Section sources**
- [README.md](file://README.md#L173-L218)
- [PropertiesLoader.java](file://src/main/java/com/jguru/vertexai/utils/PropertiesLoader.java#L1-L86)
- [rebuild.cmd](file://rebuild.cmd#L1-L48)