# OpenAI-Compatible Gemini Models

This document describes the new OpenAI-compatible Gemini model support in the Vertex AI Master CLI tool.

## Overview

The Vertex AI platform now supports using Gemini models through the OpenAI-compatible Chat Completions API endpoint. This provides compatibility with OpenAI-format requests while leveraging Google's Gemini models.

## Configuration

### Model Properties

The new model is configured in `src/main/resources/models.properties`:

```properties
# OpenAI-Compatible Gemini Models (Region: us-central1)
gemini.flash.openapi=google/gemini-2.0-flash-001
gemini.flash.openapi.region=us-central1
gemini.flash.openapi.provider=google-openai
gemini.flash.openapi.openai=true
```

**Key properties:**
- `gemini.flash.openapi` - The full model identifier (includes `google/` prefix)
- `gemini.flash.openapi.region` - GCP region (us-central1)
- `gemini.flash.openapi.provider` - Custom provider identifier (`google-openai`)
- `gemini.flash.openapi.openai` - Flag to enable OpenAI-compatible routing

### API Endpoint

The model uses the standard Chat Completions endpoint:
```
https://aiplatform.googleapis.com/v1/projects/{project}/locations/{location}/endpoints/openapi/chat/completions
```

## Usage

### Command Line

```bash
# Using the model alias
vertex.exe -m gemini.flash.openapi "Hello, world!"

# Using the full model name (alternative)
vertex.exe -m google/gemini-2.0-flash-001 "Hello, world!"
```

### Java API

```java
// Create client with service account authentication
AuthenticationConfig authConfig = AuthenticationConfig.builder()
    .withType(AuthenticationType.SERVICE_ACCOUNT_EXPLICIT_KEY)
    .withSaKeyFile("path/to/service-account.json")
    .withProjectId("your-project-id")
    .withLocation("us-central1")
    .build();

VertexAiClient client = new VertexAiClient(authConfig);

// Use the model alias
String response = client.callVertexAi("gemini.flash.openapi", "Hello, world!");

// Or use the full model name
String response2 = client.callVertexAi("google/gemini-2.0-flash-001", "Hello, world!");
```

## Technical Implementation

### Routing Logic

The system automatically routes requests based on model properties:

1. **Check for `.provider` property** - If present, route to Chat Completions API with that provider
2. **Check for `.openai=true` flag** - If present, route to Chat Completions API with "openai" provider
3. **Default routing** - Use standard Vertex AI API

### Model Name Resolution

- **Model alias** (e.g., `gemini.flash.openapi`) - User-friendly identifier
- **Full model name** (e.g., `google/gemini-2.0-flash-001`) - Actual API model identifier

The system automatically resolves the alias to the full model name for API calls.

### Provider Handling

Different providers are handled consistently:

- **Standard MaaS models**: `provider/model-name` (e.g., `deepseek-ai/deepseek-r1-0528-maas`)
- **Google OpenAI models**: `google/model-name` (e.g., `google/gemini-2.0-flash-001`)
- **Pure OpenAI models**: `model-name` (e.g., `gpt-4`)

## Testing

The implementation includes comprehensive test coverage in `VertexAiClientTest.java`:

```java
@Test
void shouldRouteToChatCompletionsWhenGoogleOpenAiProviderIsPresent() throws IOException {
    // Test verifies:
    // 1. Correct routing to Chat Completions API
    // 2. Proper provider detection (google-openai)
    // 3. Correct model name resolution
    // 4. No routing to standard Vertex AI API
}
```

## Benefits

1. **OpenAI Compatibility**: Use familiar OpenAI request format
2. **Consistent Interface**: Same command-line interface for all models
3. **Model Aliasing**: Easy-to-remember model names
4. **Automatic Routing**: No manual endpoint selection needed
5. **Extensible**: Easy to add new OpenAI-compatible models

## Comparison

| Feature | Standard Vertex AI | OpenAI-Compatible Gemini |
|---------|-------------------|-------------------------|
| API Endpoint | `aiplatform.googleapis.com` | `aiplatform.googleapis.com/v1/.../openapi/chat/completions` |
| Request Format | Google's native format | OpenAI Chat Completions format |
| Model Names | `gemini-2.0-flash-001` | `google/gemini-2.0-flash-001` |
| CLI Usage | `gemini.flash` | `gemini.flash.openapi` |

## Future Enhancements

Potential future improvements:

1. **Additional models**: Support for more OpenAI-compatible Gemini models
2. **Streaming support**: Real-time response streaming
3. **Function calling**: OpenAI function calling support
4. **Model parameters**: Fine-tuned control over generation parameters
5. **Batch processing**: Support for multiple requests in one call

## Troubleshooting

### Common Issues

1. **Model not found**: Ensure the model is enabled in your GCP project
2. **Authentication errors**: Verify service account permissions
3. **Region errors**: Ensure the model is available in your specified region

### Debugging

Enable detailed logging to see routing decisions:

```java
// The client logs routing decisions:
logger.info("Using Chat Completions API for model: {} with provider: {}", modelName, provider);
```

This helps identify which endpoint and provider are being used for each request.
