# Vertex AI URL Construction Guide (Non-Gemini Models)

This guide explains how URLs are constructed for Model-as-a-Service (MaaS) and other non-Gemini family models on Google Cloud Vertex AI.

## 1. Overview of API Types

Vertex AI provides different API surfaces depending on the model provider and the desired compatibility:

1.  **Chat Completions API**: An OpenAI-compatible endpoint used for models like Llama, Qwen, DeepSeek, etc.

---

## 2. Base Host Logic

The base host depends on the specified **Location (Region)**:

-   **Global**: `aiplatform.googleapis.com`
-   **Regional**: `{region}-aiplatform.googleapis.com` (e.g., `us-central1-aiplatform.googleapis.com`)

---

## 3. Chat Completions API (OpenAI-Compatible)

Used for most MaaS models. This endpoint follows the OpenAI specification but is hosted on Vertex AI.

### URL Pattern
`https://{host}/v1/projects/{projectId}/locations/{location}/endpoints/openapi/chat/completions`

### Components
-   **`{host}`**: See "Base Host Logic" above.
-   **`{projectId}`**: Your Google Cloud Project ID.
-   **`{location}`**: The region where the model is deployed (e.g., `us-central1`, `us-east5`, or `global`).

### Example
For a project `my-project` in `us-central1`:
`https://us-central1-aiplatform.googleapis.com/v1/projects/my-project/locations/us-central1/endpoints/openapi/chat/completions`

---

## 4. Summary Table

| Model Family | API Type | Endpoint Suffix |
| :--- | :--- | :--- |
| **Llama (Meta)** | Chat Completions | `/endpoints/openapi/chat/completions` |
| **Qwen (Alibaba)** | Chat Completions | `/endpoints/openapi/chat/completions` |
| **DeepSeek** | Chat Completions | `/endpoints/openapi/chat/completions` |
| **Moonshot (Kimi)** | Chat Completions | `/endpoints/openapi/chat/completions` |

---

## 5. Implementation Notes

-   **Model Selection**: For Chat Completions, the model name is passed inside the JSON request body (`"model": "..."`).
-   **Authentication**: All requests require a `Bearer` token in the `Authorization` header, obtained via Google Cloud IAM (typically `Vertex AI User` role).
-   **Content-Type**: Must be `application/json`.
