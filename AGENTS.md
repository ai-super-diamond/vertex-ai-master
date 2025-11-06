# AGENTS.md - Vertex AI Master CLI

Instructions for AI coding agents working on this Java CLI project for Google Vertex AI and Gemini API.

## Project Overview

**Tech Stack:**
- Java 21 (use `--release 21` flag, not `-source/-target`)
- Maven 3.x build tool (located at `d:\java\maven\bin\mvn.cmd`)
- Picocli 4.7.7 for CLI framework
- Google GenAI SDK 1.26.0 for Vertex AI integration
- JUnit Jupiter 5.12.1 for testing
- GraalVM (optional) for native compilation

**Purpose:** Command-line tool to interact with Google's Vertex AI generative models using either API keys or service account authentication.

## Build Commands

```bash
# Clean build
d:\java\maven\bin\mvn.cmd clean compile

# Package shaded JAR
d:\java\maven\bin\mvn.cmd clean package

# Run JAR directly
java -jar target/demo-0.0.1-SNAPSHOT.jar --help

# Build native executable (requires GraalVM + Visual Studio 2022)
.\build-exe.cmd
```

## Testing Instructions

**Run all tests:**
```bash
d:\java\maven\bin\mvn.cmd test
```

**Run integration tests (requires valid service account key):**
```bash
d:\java\maven\bin\mvn.cmd test "-Drun.integration.tests=true"
```

**Service account key location for integration tests:**
`c:\java\backup\GCP\Vertex\skorec.json`

**Run specific test:**
```bash
d:\java\maven\bin\mvn.cmd test -Dtest=VertexAiMasterMainTest#testVertexAiWithServiceAccountKey
```

**Test files:**
- Working SA key: `keys\skorec.json` (from backup location)
- Expired SA key: `keys\expired.json`
- Main test class: `src/test/java/com/example/demo/VertexAiMasterMainTest.java`

**Important:** All tests must pass before committing. Integration tests validate:
1. Service account authentication works
2. Application does NOT fall back to ADC when explicit `--sa-key-file` is provided
3. Invalid/expired keys fail with proper error messages

## Code Style Guidelines

**Formatting:** This project uses automated formatting tools.

```bash
# Auto-format all code (do this before committing)
d:\java\maven\bin\mvn.cmd spotless:apply

# Check if code is formatted
d:\java\maven\bin\mvn.cmd spotless:check

# Run style checks (non-blocking)
d:\java\maven\bin\mvn.cmd checkstyle:check
```

**Style rules:**
- 2-space indentation (enforced by Eclipse formatter)
- 100-character line limit
- Remove unused imports
- Trim trailing whitespace
- End files with newline
- Google Java Style checks (warnings only)

**Before committing:**
1. Run `mvn spotless:apply` to format code
2. Run `mvn clean test` to ensure all tests pass
3. Verify no compilation errors

## Development Environment Tips

**Key files:**
- Main entry: `src/main/java/com/jguru/vertexai/VertexAiMasterMain.java`
- Client logic: `src/main/java/com/jguru/vertexai/client/VertexAiClient.java`
- Utilities: `src/main/java/com/jguru/vertexai/utils/VertexUtils.java`
- Model aliases: `src/main/resources/models.properties`
- Tests: `src/test/java/com/example/demo/VertexAiMasterMainTest.java`

**Model configuration:**
- Models are aliased in `models.properties` (e.g., `gemini.pro=gemini-2.5-pro`)
- Use `--model-name` flag to specify model alias or full name
- Default model resolves through alias system

**Authentication modes:**
1. API Key: `--api-key YOUR_KEY` (Gemini API)
2. Service Account: `--sa-key-file path/to/key.json` (Vertex AI)
3. ADC: Default when no explicit auth provided

**CRITICAL: ADC Fallback Prevention**
When `--sa-key-file` is explicitly provided, the application MUST:
- Load credentials via `GoogleCredentials.fromStream(new FileInputStream(path))`
- Use `.credentials(credentials)` in client builder
- Fail immediately if key is invalid (no ADC fallback)
- This is enforced in `VertexAiClient.java` lines 93-113

## Security Considerations

- Never commit service account keys to the repository
- Keys are in `.gitignore` and stored in `c:\java\backup\GCP\Vertex\`
- Test keys in `keys/` directory are for testing only
- Expired keys are used to verify proper error handling
- Redact credentials from logs and error messages


## Agent Development Patterns

### MCP Tool Integration (Planned)

**Available MCP servers** (configured in `c:\java\mcp-configs\qoder-mcp.json`):
- **serper**: Web search for current information
- **context7**: Library documentation lookup
- **exa**: Advanced search and context retrieval
- **sequential-thinking**: Complex task orchestration

**Tool selection rubric:**
- Use `serper` for time-sensitive facts, current events
- Use `context7` for API docs, library references
- Use `exa` for deep research, synthesizing multiple sources
- Use `sequential-thinking` for multi-step reasoning tasks

**Integration status:** MCP tools are not yet wired into the CLI. Planned implementation:
1. Add `ToolClient` abstraction for MCP server communication
2. Add `--use-mcp serper|context7|exa` CLI flag
3. Implement retry/backoff for tool calls
4. Add structured logging for tool inputs/outputs
