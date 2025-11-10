# Architecture Documentation - Vertex AI Master CLI

## Overview

Vertex AI Master CLI is a command-line interface tool that provides unified access to Google's Vertex AI generative models and third-party MaaS (Models as a Service) through a clean, layered architecture. The application supports dual API integration, allowing it to work with both Google's native Vertex AI SDK and OpenAI-compatible Chat Completions API for external models.

## Architecture Diagram

```mermaid
graph TB
    A[CLI Layer<br/>VertexAiMasterMain] --> B[Service Layer<br/>VertexAiService]
    B --> C[Client Layer<br/>VertexAiClient]
    B --> D[Client Layer<br/>ChatCompletionsClient]
    B --> E[Client Layer<br/>WorldwideAvailabilityClient]
    C --> F[Google Vertex AI SDK]
    D --> G[HTTP Client<br/>OpenAI API]
    E --> B
    H[Configuration<br/>models.properties] -.-> A
    H -.-> B
    H -.-> C
    H -.-> D
    I[Authentication<br/>Service Account<br/>API Key] -.-> A
    I -.-> B
    I -.-> C
</```

## Layered Architecture

### 1. Presentation Layer (CLI)
**Primary Component**: `VertexAiMasterMain.java`

- Built with Picocli framework for command-line parsing
- Handles user input validation and parameter processing
- Orchestrates the application flow based on CLI arguments
- Manages authentication configuration creation
- Routes execution to appropriate service methods

**Key Responsibilities**:
- Parse and validate CLI arguments
- Create authentication configurations
- Initialize service layer
- Handle application lifecycle and error reporting

### 2. Service Layer
**Primary Components**: `VertexAiService.java`, `VertexAiServiceImpl.java`

- Business logic layer implementing the core application functionality
- Model name resolution and routing logic
- Region availability testing orchestration
- Request/response transformation between layers
- Error handling and retry logic

**Key Responsibilities**:
- Resolve model aliases to actual model identifiers
- Determine appropriate client based on model configuration
- Coordinate region availability testing across clusters
- Manage request lifecycle and response aggregation

### 3. Client Layer
**Primary Components**: `VertexAiClient.java`, `ChatCompletionsClient.java`, `WorldwideAvailabilityClient.java`

- Direct API communication with external services
- Implementation of dual API routing strategy
- Authentication handling and credential management
- HTTP request/response processing
- Specific client implementations for different API types

#### VertexAiClient
- Communicates with Google Vertex AI using native SDK
- Handles service account authentication
- Supports standard Vertex AI models (Gemini, Llama, etc.)
- Implements region-specific endpoint routing

#### ChatCompletionsClient
- Communicates with MaaS providers via OpenAI-compatible API
- Handles API key authentication
- Supports third-party models (DeepSeek, Qwen, MiniMax, OpenAI)
- Implements provider-specific configurations

#### WorldwideAvailabilityClient
- Specialized client for worldwide region testing
- Iterates through all GCP regions across all clusters
- Aggregates test results from multiple region checks
- Provides comprehensive availability reporting

### 4. Data Transfer Objects (DTOs)
Located in `src/main/java/com/jguru/vertexai/service/dto/`

- `AuthenticationConfig`: Encapsulates authentication parameters
- `GenerationRequest`: Standardized request format for content generation
- `GenerationResult`: Standardized response format for content generation
- `RegionCheckRequest`: Configuration for region availability testing
- `RegionCheckResult`: Results from region availability testing

### 5. Utilities
**Primary Component**: `VertexUtils.java`

- Helper methods for common operations
- Credential loading and validation
- Model configuration parsing
- Error message formatting

### 6. Configuration Layer
**Primary Component**: `models.properties`

- Model alias definitions
- Regional deployment configurations
- Provider routing information
- OpenAI compatibility flags
- Worldwide testing configuration flags

## Dual API Integration Strategy

The application implements a dual API integration strategy to support both Google's native Vertex AI models and third-party MaaS models:

1. **Standard Vertex AI Models**: Routed through `VertexAiClient` using Google's native SDK
2. **MaaS Models**: Routed through `ChatCompletionsClient` using OpenAI-compatible API

The routing decision is made based on model configuration in `models.properties`:
- Models with a `.provider` property are routed to Chat Completions API
- Models without a `.provider` property are routed to Vertex AI SDK

## Authentication Architecture

The application supports three authentication modes through the `AuthenticationType` enum:

1. **API_KEY**: Direct Gemini API access using API keys
2. **SERVICE_ACCOUNT_EXPLICIT_KEY**: Vertex AI access with explicit service account JSON key file
3. **SERVICE_ACCOUNT_ADC**: Vertex AI access using Application Default Credentials (fallback mode)

## Region Availability Testing

The application provides comprehensive region availability testing capabilities:

1. **Cluster-based Testing**: Test models across regions within a specific geographic cluster
2. **Worldwide Testing**: Test models across all 42 GCP regions globally
3. **Result Aggregation**: Collect and summarize results from multiple region tests
4. **Detailed Reporting**: Provide per-region success/failure status with error details
5. **Region Catalogue**: `RegionCatalog` maintains the canonical mapping of clusters to regions so that clients, services, and utilities share a single source of truth.

## Design Principles

1. **Separation of Concerns**: Clear boundaries between presentation, service, and client layers
2. **Single Responsibility**: Each component has a well-defined, focused purpose
3. **Open/Closed Principle**: Extensible design that allows adding new providers without modifying existing code
4. **Dependency Inversion**: High-level modules depend on abstractions, not concrete implementations
5. **Configuration-Driven**: Behavior controlled through external configuration files
6. **Fail-Fast**: Immediate failure when explicit credentials are invalid (no ADC fallback when key provided)

## Technology Stack

- **Language**: Java 21
- **Build Tool**: Apache Maven
- **CLI Framework**: Picocli 4.7.7
- **Cloud SDK**: Google GenAI SDK 1.26.0
- **HTTP Client**: OkHttp (transitive dependency)
- **JSON Processing**: Gson (transitive dependency)
- **Testing**: JUnit Jupiter 5.12.1, Mockito 5.14.2
- **Logging**: SLF4J 2.0.16, Logback 1.5.12
- **Code Quality**: Spotless, Checkstyle

## Testing Architecture

The application follows a comprehensive testing strategy:

1. **Unit Tests**: Test individual components in isolation
2. **Integration Tests**: Validate end-to-end functionality with real services
3. **Authentication Tests**: Verify proper credential handling and ADC fallback prevention
4. **Client Tests**: Validate both Vertex AI and Chat Completions API integrations
5. **Region Testing**: Validate region availability checking functionality

## Extensibility Points

1. **New Model Providers**: Add new MaaS providers by implementing additional routing logic
2. **Authentication Methods**: Extend authentication support through the AuthenticationType enum
3. **Region Clusters**: Add new geographic clusters for region testing
4. **Output Formats**: Extend response formatting and serialization options
5. **CLI Options**: Add new command-line parameters for additional functionality

## Performance Considerations

1. **Connection Reuse**: HTTP clients maintain connection pools for efficiency
2. **Credential Caching**: Service account credentials are cached to avoid repeated file reads
3. **Configuration Caching**: Model properties are cached to avoid repeated file parsing
4. **Parallel Processing**: Region availability testing can be parallelized for faster results
5. **Memory Management**: Efficient object creation and cleanup to minimize memory footprint

## Security Architecture

1. **Credential Isolation**: Explicit service account keys never fall back to ADC
2. **Input Validation**: All user inputs are validated and sanitized
3. **Error Redaction**: Sensitive information is redacted from error messages
4. **Secure Storage**: Service account keys are excluded from version control
5. **Least Privilege**: Application requests only necessary permissions

## Deployment Options

1. **Java Application**: Run directly using `java -jar`
2. **Native Executable**: Platform-specific native executable for faster startup
3. **Container Deployment**: Docker container for consistent deployment across environments
4. **Cloud Deployment**: Direct execution in Google Cloud environments

This architecture provides a solid foundation for a robust, maintainable, and extensible CLI tool that can evolve with changing requirements while maintaining high performance and security standards.