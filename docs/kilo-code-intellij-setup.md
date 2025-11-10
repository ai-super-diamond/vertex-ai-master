# Kilo Code Plugin Configuration Guide for Google Vertex AI

This guide shows you how to configure the Kilo Code plugin in IntelliJ IDEA to use Google Vertex AI's OpenAI-compatible Chat Completions API.

## Prerequisites

1. **Google Cloud Project**: You need a Google Cloud project with Vertex AI API enabled
2. **Service Account**: Create a service account with Vertex AI User permissions
3. **Service Account Key**: Download the JSON key file for your service account
4. **IntelliJ IDEA**: Install the Kilo Code plugin

## Step 1: Generate Access Token

Run this command to generate an access token:

```bash
gcloud auth application-default print-access-token
```

If prompted, authenticate with your Google account that has access to the project.

**Copy the generated token** - you'll need it for the plugin configuration.

## Step 2: Configure Kilo Code Profile in IntelliJ IDEA

1. **Open IntelliJ IDEA Settings/Preferences**
   - Go to `File > Settings` (Windows/Linux) or `IntelliJ IDEA > Preferences` (macOS)

2. **Navigate to Kilo Code Settings**
   - Search for "Kilo Code" in the search box
   - Or go to `Tools > Kilo Code > Settings`

3. **Create New Profile**
   - Click "Add Profile" or the "+" button
   - Name it "Google Vertex AI" or similar

## Step 3: Configure Endpoint and Authentication

### Base Configuration:
- **Profile Name**: `Google Vertex AI`
- **API Type**: `OpenAI Compatible`
- **HTTP Method**: `POST`

### Endpoint Configuration:
**Base URL**: `https://{your-location}-aiplatform.googleapis.com/v1/projects/{your-project-id}/locations/{your-location}/endpoints/openapi/chat/completions`

**Example**:
```
https://us-central1-aiplatform.googleapis.com/v1/projects/my-project-123/locations/us-central1/endpoints/openapi/chat/completions
```

**Required Headers**:
- `Authorization: Bearer YOUR_ACCESS_TOKEN_HERE`
- `Content-Type: application/json`

## Step 4: Add Custom Model

1. **In the Models section**, add a new model:
   - **Model Name**: `gemini-2.0-flash-exp`
   - **Display Name**: `Gemini 2.0 Flash Experimental`
   - **Context Length**: `131072` (128K tokens)
   - **Input Cost**: `0.00035` per 1K tokens
   - **Output Cost**: `0.00105` per 1K tokens
   - **Supports Streaming**: `true`

2. **Add another model** (optional):
   - **Model Name**: `gemini-1.5-pro`
   - **Display Name**: `Gemini 1.5 Pro`
   - **Context Length**: `2097152` (2M tokens)
   - **Input Cost**: `0.0035` per 1K tokens
   - **Output Cost**: `0.0105` per 1K tokens
   - **Supports Streaming**: `true`

## Step 5: Authentication Setup

Since Kilo Code doesn't directly support Google Cloud service account files, you have two options:

### Option A: Application Default Credentials (Recommended)
1. **Set up ADC on your machine**:
   ```bash
   gcloud auth application-default login
   ```
2. **In the plugin**, use the access token from Step 1
3. **Update token regularly** (tokens expire after ~1 hour)

### Option B: Environment Variable
1. **Set environment variable**:
   ```bash
   export GOOGLE_ACCESS_TOKEN=$(gcloud auth application-default print-access-token)
   ```
2. **In plugin**, reference the environment variable:
   ```
   ${env:GOOGLE_ACCESS_TOKEN}
   ```

## Step 6: Test the Configuration

1. **Save the profile**
2. **Switch to the new profile** in the Kilo Code toolbar
3. **Try a simple request**:
   ```
   Hello! Can you help me with Java programming?
   ```

## Request Format Example

The plugin will send requests in this format to Vertex AI:

```json
{
  "model": "gemini-2.0-flash-exp",
  "messages": [
    {
      "role": "user",
      "content": "Your prompt here"
    }
  ],
  "stream": true,
  "temperature": 0.7,
  "max_tokens": 4096
}
```

## Response Format

Vertex AI will respond with:

```json
{
  "id": "chatcmpl-abc123",
  "object": "chat.completion",
  "created": 1677652288,
  "model": "gemini-2.0-flash-exp",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "Hello! I'd be happy to help you with Java programming."
      },
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 9,
    "completion_tokens": 12,
    "total_tokens": 21
  }
}
```

## Model Pricing Reference

| Model | Input Cost (per 1K tokens) | Output Cost (per 1K tokens) | Context Length |
|-------|----------------------------|------------------------------|----------------|
| gemini-2.0-flash-exp | $0.00035 | $0.00105 | 131,072 |
| gemini-1.5-pro | $0.0035 | $0.0105 | 2,097,152 |
| gemini-1.5-flash | $0.00035 | $0.00105 | 1,048,576 |

## Troubleshooting

### Common Issues:

1. **"401 Unauthorized"**
   - Check that your access token is valid and not expired
   - Regenerate with `gcloud auth application-default print-access-token`

2. **"404 Not Found"**
   - Verify your project ID and location in the endpoint URL
   - Ensure Vertex AI API is enabled in your project

3. **"403 Forbidden"**
   - Check that your service account has Vertex AI User permissions
   - Verify the project and location are correct

### Token Refresh Script

Create a simple script to refresh your token:

```bash
#!/bin/bash
# refresh-token.sh
echo "Generating new access token..."
TOKEN=$(gcloud auth application-default print-access-token)
echo "New token: $TOKEN"
# Copy to clipboard (macOS)
echo "$TOKEN" | pbcopy
echo "Token copied to clipboard!"
```

## Security Notes

- **Never commit** access tokens to version control
- **Regularly rotate** your service account keys
- **Use environment variables** for production setups
- **Set up proper IAM** roles and permissions

## Advanced Configuration

### Custom Headers (Optional):
- `X-User-Agent`: Your application identifier
- `X-Client-Info`: Additional client information

### Model-Specific Parameters:
- **Temperature**: 0.0 to 2.0 (default: 0.7)
- **Top-p**: 0.0 to 1.0 (default: 0.95)
- **Max Tokens**: 1 to 8192 (model dependent)

## Next Steps

Once configured, you can:
1. **Use Kilo Code** for code generation and assistance
2. **Switch between models** based on your needs
3. **Monitor usage** in Google Cloud Console
4. **Set up billing alerts** to control costs

Remember to update your access token regularly as they expire after approximately 1 hour.
