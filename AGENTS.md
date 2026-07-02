# AGENTS.md

Repository-specific guidance for `vertex-ai-master`.

Follow the shared AGENTS.md format at <https://agents.md> for the general
model of how agent instructions work. Keep this file focused on the details
that matter for this repository.

## Project Overview

- Java 25 with GraalVM
- Maven 3.x
- Picocli 4.7.7
- Google GenAI SDK 1.60.0
- JUnit Jupiter 6.1.1

This is a CLI for interacting with Google Vertex AI and Gemini models using
either API keys or service account authentication.

Architecture:

- Presentation layer: `VertexAiMasterMain`
- Service layer: `VertexAiService` / `VertexAiServiceImpl`
- Client layer: `VertexAiClient`
- DTOs use builder-based value objects under `domain/dto` and `service/dto`

## Build Commands

```bash
mvn clean compile
mvn clean package
java -jar target/vertex-1.0.1.jar --help
```

Windows and cross-platform helper scripts:

- `.\bin\build-jar.cmd` / `./bin/build-jar.sh`
- `.\bin\build-exe.cmd`
- `.\bin\doctor.cmd` / `./bin/doctor.sh`

## Testing

```bash
mvn test
mvn test "-Drun.integration.tests=true"
mvn test -Dtest=VertexAiMasterMainTest#testVertexAiWithServiceAccountKey
```

Integration tests require a valid service account key. The key path comes from
the `test.project.sa.key.file` property in the test properties file, not from a
hardcoded path.

Relevant test assets:

- `keys\sa_key.json`
- `keys\expired.json`
- `src/test/java/com/jguru/vertexai/VertexAiMasterMainTest.java`

## Formatting

Use Spotless with the Eclipse formatter.

```bash
mvn spotless:apply
mvn spotless:check
```

Run formatting before finishing any code change that touches Java sources.

## Project Conventions

- Use `--release 25` for Java compilation.
- Native builds require `JAVA_HOME` to point at the GraalVM JDK itself.
- Keep the three-tier layering intact when making changes.
- `VertexAiClient` must load explicit service account keys directly and must
  not fall back to ADC when `--sa-key-file` is provided.
- Model aliases live in `src/main/resources/models.properties`.
- Standard Vertex AI SDK routing is for Gemini models without a `.provider`
  property; chat-completions routing is used for MaaS models with one.

## Region Checks

- `--check-all-regions` / `-car` checks a model across a cluster
- `--worldwide` / `-w` checks the model across all worldwide GCP regions
- Cluster values map to `<CLUSTER>_REGIONS` entries in `regions.properties`
- `GLOBAL` is a single pseudo-region for models served only on the global
  endpoint

## Security

- Never commit service account keys or other secrets.
- Test keys under `keys/` are for testing only.
- Redact credentials from logs and error messages.

## Key Files

- Main entry: `src/main/java/com/jguru/vertexai/VertexAiMasterMain.java`
- Service API: `src/main/java/com/jguru/vertexai/service/VertexAiService.java`
- Service impl: `src/main/java/com/jguru/vertexai/service/VertexAiServiceImpl.java`
- Client: `src/main/java/com/jguru/vertexai/client/VertexAiClient.java`
- Model config: `src/main/resources/models.properties`
- Region tests: `src/test/java/com/jguru/vertexai/client/WorldwideAvailabilityClientTest.java`
