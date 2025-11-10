# Kilo Code Plugin Configuration for DeepSeek R1

This guide provides step-by-step instructions for configuring the Kilo Code plugin in IntelliJ IDEA to use the DeepSeek R1 model through Google Vertex AI's OpenAI-compatible Chat Completions API.

## Model Information

Based on the model card, DeepSeek R1 (deepseek-r1-0528-maas) is a MaaS (Model as a Service) model available through Google Vertex AI. It uses the OpenAI-compatible Chat Completions API.

## Prerequisites

1. Google Cloud Project with Vertex AI API enabled
2. Service account with Vertex AI User permissions
3. Access token (which you already have)
4. IntelliJ IDEA with Kilo Code plugin installed

## Configuration Steps

### Step 1: Open Kilo Code Settings

1. In IntelliJ IDEA, go to `File` → `Settings` (Windows/Linux) or `IntelliJ IDEA` → `Preferences` (macOS)
2. Navigate to `Tools` → `Kilo Code` → `Settings`

### Step 2: Create New Profile

1. Click the `+` button to add a new profile
2. Name the profile "DeepSeek R1" or any name you prefer

### Step 3: Configure Base Settings

In the profile configuration:

1. **API Type**: Select "OpenAI Compatible"
2. **HTTP Method**: Select "POST"

### Step 4: Configure Endpoint URL

Enter the following endpoint URL, replacing the placeholders with your actual values:

```
https://{your-location}-aiplatform.googleapis.com/v1/projects/{your-project-id}/locations/{your-location}/endpoints/openapi/chat/completions
```

Example with actual values:
```
https://us-central1-aiplatform.googleapis.com/v1/projects/my-project-123/locations/us-central1/endpoints/openapi/chat/completions
```

### Step 5: Configure Headers

Add the following headers:

1. **Authorization**: `Bearer YOUR_ACCESS_TOKEN`
   - Replace `YOUR_ACCESS_TOKEN` with your actual access token
2. **Content-Type**: `application/json`

### Step 6: Configure Model Settings

1. In the "Models" section, click the `+` button to add a new model
2. Set the following values:
   - **Model Name**: `deepseek-ai/deepseek-r1-0528-maas`
   - **Display Name**: `DeepSeek R1`
   - **Context Length**: `131072` (128K tokens)
   - **Input Cost**: `0.0005` per 1K tokens (verify this in your model card)
   - **Output Cost**: `0.0015` per 1K tokens (verify this in your model card)
   - **Supports Streaming**: `true`

### Step 7: Advanced Settings (If Available)

1. **Temperature**: Set to `0.7` (default) or adjust as needed
2. **Top-p**: Set to `0.95` (default) or adjust as needed
3. **Max Tokens**: Set to `4096` or as needed for your use case

### Step 8: Save Configuration

1. Click "Apply" and then "OK" to save the profile
2. Select the newly created "DeepSeek R1" profile from the dropdown in the Kilo Code toolbar

## Troubleshooting 404 Errors

If you're getting a 404 error, check the following:

### 1. Verify Endpoint URL
- Ensure the project ID is correct
- Ensure the location/region is correct
- Verify that the DeepSeek R1 model is available in your selected region

### 2. Check Model Name
- The model name should be exactly: `deepseek-ai/deepseek-r1-0528-maas`
- Some systems might require just `deepseek-r1-0528-maas`

### 3. Verify Access Token
- Ensure your access token is still valid (they expire after 1 hour)
- Generate a new token if needed:
  ```bash
  gcloud auth application-default print-access-token
  ```

### 4. Check Project Permissions
- Ensure your service account has the necessary permissions
- Verify the model is enabled in your project

### 5. Test with cURL
You can test your configuration with cURL:

```bash
curl -X POST \
  https://us-central1-aiplatform.googleapis.com/v1/projects/YOUR_PROJECT/locations/us-central1/endpoints/openapi/chat/completions \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "deepseek-ai/deepseek-r1-0528-maas",
    "messages": [
      {
        "role": "user",
        "content": "Hello, world!"
      }
    ],
    "stream": false
  }'
```

## Common Issues and Solutions

### Issue: 404 (body missing)
This typically indicates one of the following:

1. **Incorrect Endpoint**: Double-check your project ID and location
2. **Model Not Available**: Verify DeepSeek R1 is available in your region
3. **Incorrect Model Name**: Try using just `deepseek-r1-0528-maas` instead of the full provider/model format

### Issue: 401 Unauthorized
1. **Expired Token**: Generate a new access token
2. **Incorrect Token**: Verify you're using the correct access token

### Issue: 403 Forbidden
1. **Insufficient Permissions**: Check your service account permissions
2. **Model Access**: Verify your project has access to the DeepSeek R1 model

## Model Parameters

For DeepSeek R1, you can adjust these parameters:

- **temperature**: 0.0 to 2.0 (controls randomness)
- **top_p**: 0.0 to 1.0 (controls diversity)
- **max_tokens**: Maximum number of tokens to generate
- **stream**: true/false (for streaming responses)

## Pricing Information

Check the model card for the most current pricing, but typical costs are:
- Input: $0.0005 per 1K tokens
- Output: $0.0015 per 1K tokens

## Next Steps

Once configured correctly:
1. Select the "DeepSeek R1" profile in Kilo Code
2. Test with a simple prompt
3. Adjust parameters as needed for your use case

Remember to monitor your usage and costs in the Google Cloud Console.
