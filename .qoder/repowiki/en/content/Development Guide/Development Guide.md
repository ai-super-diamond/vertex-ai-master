# Development Guide

<cite>
**Referenced Files in This Document**
- [pom.xml](file://pom.xml)
- [README.md](file://README.md)
- [AGENTS.md](file://AGENTS.md)
- [ARCHITECTURE.md](file://ARCHITECTURE.md)
- [docs/FORMATTING.md](file://docs/FORMATTING.md)
- [src/test/java/com/jguru/vertexai/VertexAiMasterMainTest.java](file://src/test/java/com/jguru/vertexai/VertexAiMasterMainTest.java)
- [src/test/java/com/jguru/vertexai/client/WorldwideAvailabilityClientTest.java](file://src/test/java/com/jguru/vertexai/client/WorldwideAvailabilityClientTest.java)
- [src/test/resources/test.properties](file://src/test/resources/test.properties)
- [build-exe.cmd](file://build-exe.cmd)
- [test-all-eu.cmd](file://test-all-eu.cmd)
- [test-all-us.cmd](file://test-all-us.cmd)
- [rebuild.cmd](file://rebuild.cmd)
- [vert.cmd](file://vert.cmd)
- [debug-all-eu.cmd](file://debug-all-eu.cmd)
- [debug-all-us.cmd](file://debug-all-us.cmd)
- [eclipse-formatter.xml](file://eclipse-formatter.xml)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Project Structure](#project-structure)
3. [Testing Strategy](#testing-strategy)
4. [Build and Packaging](#build-and-packaging)
5. [Code Quality and Formatting](#code-quality-and-formatting)
6. [Development Environment Setup](#development-environment-setup)
7. [Branch Management and Pull Requests](#branch-management-and-pull-requests)
8. [Performance Considerations](#performance-considerations)
9. [Debugging and Troubleshooting](#debugging-and-troubleshooting)
10. [Best Practices](#best-practices)

## Introduction

The Vertex AI Master CLI is a Java-based command-line interface for interacting with Google's Vertex AI generative models. This comprehensive development guide covers the complete development workflow, testing strategies, build processes, and contribution guidelines for the project.

The project follows a clean 3-tier layered architecture with strict separation of concerns, comprehensive testing coverage, and automated quality assurance tools. It supports dual API integration for both Google's native Vertex AI models and third-party MaaS (Models as a Service) providers.

## Project Structure

The project is organized into distinct layers with clear responsibilities:

```mermaid
graph TB
subgraph "Presentation Layer"
CLI[VertexAiMasterMain<br/>Picocli CLI Interface]
end
subgraph "Service Layer"
Service[VertexAiService<br/>Business Logic]
ServiceImpl[VertexAiServiceImpl<br/>Implementation]
end
subgraph "Client Layer"
VAClient[VertexAiClient<br/>Google Vertex AI SDK]
CCClient[ChatCompletionsClient<br/>MaaS API Client]
WAClient[WorldwideAvailabilityClient<br/>Region Testing]
end
subgraph "Data Layer"
DTOs[DTO Classes<br/>AuthenticationConfig, GenerationRequest, etc.]
Utils[Utility Classes<br/>VertexUtils]
end
subgraph "Configuration"
Props[models.properties<br/>Model Aliases]
Resources[Logback Configuration<br/>Resources]
end
CLI --> Service
Service --> VAClient
Service --> CCClient
Service --> WAClient
ServiceImpl --> DTOs
VAClient --> Utils
CCClient --> Utils
WAClient --> Utils
Service --> Props
CLI --> Resources
```

**Diagram sources**
- [pom.xml](file://pom.xml#L1-L50)
- [ARCHITECTURE.md](file://ARCHITECTURE.md#L8-L25)

**Section sources**
- [ARCHITECTURE.md](file://ARCHITECTURE.md#L28-L100)
- [README.md](file://README.md#L255-L290)

## Testing Strategy

The project implements a comprehensive testing strategy with multiple layers of validation:

### Unit Tests

Unit tests focus on individual components in isolation using JUnit 5 and Mockito for mocking:

```mermaid
classDiagram
class VertexAiMasterMainTest {
+testVertexAiWithServiceAccountKey()
+shouldRequireLocationInNormalMode()
+shouldNotRequireLocationInRegionCheckMode()
+shouldRejectBothModelNameAndModelFile()
+shouldAcceptModelNameOnly()
+shouldAcceptModelFileOnly()
+shouldUseDefaultModelWhenNeitherSpecified()
+shouldLoadModelFileWhenSpecified()
+testVertexAiWithExpiredServiceAccountKey_ShouldFail()
+shouldAllModelsPass()
+shouldPassOneAtLeastforUS()
+shouldDiscoverDeepseekR1Region()
}
class WorldwideAvailabilityClientTest {
+shouldCreateWorldwideAvailabilityClient()
+shouldHaveCorrectPackage()
}
class TestProperties {
+String test.project.id
+String test.project.location
+String test.project.sa.key.file
+String test.model.name
+String test.prompt
}
VertexAiMasterMainTest --> TestProperties : uses
VertexAiMasterMainTest --> Mocking : uses
WorldwideAvailabilityClientTest --> Assertions : uses
```

**Diagram sources**
- [src/test/java/com/jguru/vertexai/VertexAiMasterMainTest.java](file://src/test/java/com/jguru/vertexai/VertexAiMasterMainTest.java#L25-L100)
- [src/test/java/com/jguru/vertexai/client/WorldwideAvailabilityClientTest.java](file://src/test/java/com/jguru/vertexai/client/WorldwideAvailabilityClientTest.java#L9-L30)

### Integration Tests

Integration tests validate end-to-end functionality with real service account keys:

| Test Type | Purpose | Execution Requirement |
|-----------|---------|----------------------|
| Service Account Authentication | Verify explicit service account key authentication | Must pass 100% |
| ADC Fallback Prevention | Ensure no automatic fallback to Application Default Credentials | Must pass 100% |
| Invalid/Expired Keys | Test proper error handling for invalid credentials | Must pass 100% |
| Model Availability Testing | Validate region availability across all GCP regions | Optional but recommended |

### Pre-commit Checklist

The critical pre-commit checklist enforces 100% test pass rate:

```mermaid
flowchart TD
Start([Pre-commit]) --> RunTests["mvn clean test<br/>• All tests must pass<br/>• 0 failures, 0 errors"]
RunTests --> TestsPassed{"Tests Pass?"}
TestsPassed --> |No| FixIssues["Fix failing tests<br/>• Review test failures<br/>• Debug broken functionality"]
TestsPassed --> |Yes| FormatCode["mvn spotless:apply<br/>• Auto-format Java code<br/>• Apply Eclipse formatter"]
FormatCode --> FormatCheck{"Format Check<br/>Passes?"}
FormatCheck --> |No| ManualFix["Manual code fixes<br/>• Review formatting issues<br/>• Apply corrections"]
FormatCheck --> |Yes| VerifyBuild["Verify build<br/>• Check for compilation errors<br/>• Ensure no warnings"]
VerifyBuild --> BuildOK{"Build OK?"}
BuildOK --> |No| FixBuild["Fix build issues<br/>• Resolve compilation errors<br/>• Address dependency conflicts"]
BuildOK --> |Yes| ReadyToCommit["Ready to commit<br/>• All checks passed<br/>• 100% test pass rate"]
FixIssues --> RunTests
ManualFix --> FormatCheck
FixBuild --> VerifyBuild
```

**Diagram sources**
- [AGENTS.md](file://AGENTS.md#L162-L170)
- [docs/FORMATTING.md](file://docs/FORMATTING.md#L67-L85)

**Section sources**
- [src/test/java/com/jguru/vertexai/VertexAiMasterMainTest.java](file://src/test/java/com/jguru/vertexai/VertexAiMasterMainTest.java#L56-L108)
- [src/test/java/com/jguru/vertexai/client/WorldwideAvailabilityClientTest.java](file://src/test/java/com/jguru/vertexai/client/WorldwideAvailabilityClientTest.java#L11-L29)
- [AGENTS.md](file://AGENTS.md#L162-L170)

## Build and Packaging

### Maven Configuration

The project uses Maven 3.x with comprehensive plugin configuration for build automation:

| Plugin | Version | Purpose | Configuration |
|--------|---------|---------|---------------|
| Maven Compiler | 3.14.1 | Java compilation with Java 25 | Release 25, annotation processing |
| Maven Shade | 3.6.1 | Create executable JAR with dependencies | Main class: VertexAiMasterMain |
| Maven Surefire | 3.5.4 | Test execution with ByteBuddy support | Inline mocking for final classes |
| Spotless | 2.43.0 | Code formatting and POM tidying | Eclipse formatter, Google style |
| Checkstyle | 10.20.2 | Style enforcement | Google checks, warnings only |

### Maven Profiles

The project defines specialized Maven profiles for different build scenarios:

```mermaid
graph LR
subgraph "Base Build"
Base[Clean Compilation<br/>• Java 25<br/>• Annotation Processing<br/>• Resource Filtering]
end
subgraph "Shaded JAR"
Shade[Maven Shade Plugin<br/>• Dependency bundling<br/>• Manifest configuration<br/>• Main class setup]
end
subgraph "Native Compilation"
Native[GraalVM Native Profile<br/>• Native image generation<br/>• Platform-specific binaries<br/>• Fast startup times]
end
subgraph "Quality Assurance"
SpotBugs[SpotBugs Static Analysis<br/>• Defect detection<br/>• Threading issues<br/>• Null pointer analysis]
end
Base --> Shade
Shade --> Native
Base --> SpotBugs
```

**Diagram sources**
- [pom.xml](file://pom.xml#L129-L296)

### Native Compilation Workflow

The native compilation process uses GraalVM for platform-specific executables:

```mermaid
sequenceDiagram
participant Dev as Developer
participant Maven as Maven Build
participant GraalVM as GraalVM Native
participant Output as Executable
Dev->>Maven : mvn -Pnative package
Maven->>GraalVM : Configure native compilation
GraalVM->>GraalVM : Analyze dependencies
GraalVM->>GraalVM : Generate native image
GraalVM->>Output : Create vertex.exe
Output->>Dev : Executable ready in project root
Note over Dev,Output : Windows native executable<br/>• Fast startup<br/>• No JVM required<br/>• Platform optimized
```

**Diagram sources**
- [build-exe.cmd](file://build-exe.cmd#L1-L24)
- [pom.xml](file://pom.xml#L244-L266)

**Section sources**
- [pom.xml](file://pom.xml#L129-L296)
- [build-exe.cmd](file://build-exe.cmd#L1-L24)
- [README.md](file://README.md#L118-L130)

## Code Quality and Formatting

### Automated Formatting Tools

The project enforces consistent code formatting through multiple automated tools:

```mermaid
flowchart TD
CodeChange[Code Changes] --> SpotlessCheck["Spotless:check<br/>• Eclipse formatter<br/>• Remove unused imports<br/>• Trim whitespace<br/>• End with newline"]
SpotlessCheck --> SpotlessApply["Spotless:apply<br/>• Auto-format Java files<br/>• Format pom.xml<br/>• Apply EditorConfig rules"]
SpotlessApply --> Checkstyle["Checkstyle:check<br/>• Google Java Style<br/>• Line length limits<br/>• Import organization<br/>• Comment standards"]
Checkstyle --> QualityReport["Quality Report<br/>• Style violations<br/>• Warning level only<br/>• Non-blocking"]
SpotlessCheck --> FormatOK{"Formatting OK?"}
FormatOK --> |No| AutoFix["Automatic fixes applied"]
FormatOK --> |Yes| Continue["Continue development"]
AutoFix --> SpotlessApply
```

**Diagram sources**
- [docs/FORMATTING.md](file://docs/FORMATTING.md#L7-L55)

### Formatting Configuration

| Tool | Configuration | Rules Enforced |
|------|---------------|----------------|
| Spotless | Eclipse formatter v4.32 | 2-space indentation, 100-char line limit, remove unused imports |
| Checkstyle | Google checks | Google Java Style, warning level only |
| EditorConfig | Cross-editor settings | UTF-8 encoding, LF line endings, consistent indentation |
| POM Tidy | Spotless | Consistent element ordering, proper spacing |

### Running Formatting Commands

Execute formatting before committing:

```bash
# Check formatting compliance
mvn spotless:check

# Apply automatic formatting
mvn spotless:apply

# Run style checks (non-blocking)
mvn checkstyle:check
```

**Section sources**
- [docs/FORMATTING.md](file://docs/FORMATTING.md#L1-L120)
- [eclipse-formatter.xml](file://eclipse-formatter.xml#L1-L16)

## Development Environment Setup

### Prerequisites

Ensure the following tools are installed and configured:

| Tool | Minimum Version | Purpose | Verification Command |
|------|----------------|---------|---------------------|
| Java JDK | Java 25 | Runtime and compilation | `java -version` |
| Apache Maven | 3.x | Build and dependency management | `mvn -v` |
| GraalVM | JDK 25 | Native executable generation | `gu --version` |
| Git | Latest | Version control | `git --version` |

### IDE Configuration

Configure your IDE for optimal development experience:

#### IntelliJ IDEA
- Install "EditorConfig" plugin (pre-installed)
- Settings → Editor → Code Style → Import eclipse-formatter.xml
- Enable annotation processing in compiler settings

#### VS Code
- Install "EditorConfig for VS Code" extension
- Install "Language Support for Java" extension
- Configure Java runtime in settings

#### Eclipse
- Built-in EditorConfig support
- Import eclipse-formatter.xml directly
- Enable annotation processing

### Environment Variables

Set required environment variables for development:

```bash
# GraalVM configuration (for native compilation)
export GRAALVM_HOME=/path/to/graalvm
export PATH=$GRAALVM_HOME/bin:$PATH

# Google Cloud SDK (for authentication)
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json
```

**Section sources**
- [README.md](file://README.md#L21-L36)
- [AGENTS.md](file://AGENTS.md#L205-L230)

## Branch Management and Pull Requests

### Git Workflow

Follow the established Git workflow for contributing to the project:

```mermaid
flowchart TD
CloneRepo["Clone Repository<br/>• git clone URL<br/>• cd vertex-ai-master"]
UpdateMain["Update Main Branch<br/>• git checkout main<br/>• git pull origin main"]
CreateFeature["Create Feature Branch<br/>• git checkout -b feature/description<br/>• Use descriptive names"]
DevelopCode["Develop Code<br/>• Make changes<br/>• Write tests<br/>• Format code"]
PreCommit["Pre-commit Checklist<br/>• mvn clean test<br/>• mvn spotless:apply<br/>• Verify build"]
CommitChanges["Commit Changes<br/>• git add -A<br/>• git commit<br/>• Concise message"]
PushBranch["Push Branch<br/>• git push origin feature/name<br/>• Create pull request"]
CreatePR["Create Pull Request<br/>• Describe changes<br/>• Testing performed<br/>• Breaking changes"]
ReviewProcess["Review Process<br/>• CI checks pass<br/>• Code review<br/>• Address feedback"]
MergePR["Merge Pull Request<br/>• Squash and merge<br/>• Delete feature branch"]
CloneRepo --> UpdateMain
UpdateMain --> CreateFeature
CreateFeature --> DevelopCode
DevelopCode --> PreCommit
PreCommit --> CommitChanges
CommitChanges --> PushBranch
PushBranch --> CreatePR
CreatePR --> ReviewProcess
ReviewProcess --> MergePR
```

**Diagram sources**
- [AGENTS.md](file://AGENTS.md#L70-L160)

### Commit Message Guidelines

Follow conventional commit message format:

| Part | Requirement | Example |
|------|-------------|---------|
| Subject | 50 characters max, imperative mood | `Add region availability testing` |
| Body | Wrap at 72 characters, explain what and why | `Adds comprehensive region testing across all GCP regions` |
| Footer | Reference issues, breaking changes | `Fixes #123, BREAKING CHANGE: removed old API` |

### Pull Request Template

Structure your pull requests with these components:

1. **Description**: Explain what changes were made and why
2. **Testing**: Describe testing performed and results
3. **Breaking Changes**: Document any backward incompatible changes
4. **Additional Notes**: Any other relevant information

**Section sources**
- [AGENTS.md](file://AGENTS.md#L70-L160)

## Performance Considerations

### Test Execution Time

Optimize test execution for efficient development cycles:

| Test Category | Expected Duration | Optimization Strategy |
|---------------|-------------------|----------------------|
| Unit Tests | < 1 minute | Parallel execution, lightweight mocks |
| Integration Tests | 5-10 minutes | Selective execution, service account key caching |
| Full Suite | 10-15 minutes | Profiled execution, dependency optimization |

### Build Optimization

Improve build performance through strategic optimizations:

```mermaid
graph TB
subgraph "Build Optimization Strategies"
Parallel[Parallel Execution<br/>• Multi-module builds<br/>• Concurrent test suites<br/>• Incremental compilation]
Caching[Build Caching<br/>• Dependency caching<br/>• Artifact reuse<br/>• Incremental updates]
Profiling[Performance Profiling<br/>• Build time analysis<br/>• Bottleneck identification<br/>• Optimization prioritization]
end
subgraph "Execution Impact"
Speed[Faster Development<br/>• Reduced iteration time<br/>• Improved developer productivity<br/>• Better CI/CD performance]
end
Parallel --> Speed
Caching --> Speed
Profiling --> Speed
```

### Memory Management

Monitor and optimize memory usage during builds:

| Phase | Memory Usage | Optimization |
|-------|--------------|--------------|
| Compilation | 512MB-1GB | Increase heap size for large projects |
| Testing | 1GB-2GB | Parallel test execution with memory limits |
| Packaging | 256MB-512MB | Optimize dependency tree |

**Section sources**
- [pom.xml](file://pom.xml#L170-L185)

## Debugging and Troubleshooting

### Common Build Failures

Address frequent build and test issues systematically:

```mermaid
flowchart TD
BuildFailure[Build Failure] --> CheckLogs["Check Build Logs<br/>• Read error messages<br/>• Identify root cause<br/>• Check dependencies"]
CheckLogs --> DependencyIssue{"Dependency Issue?"}
DependencyIssue --> |Yes| UpdateDeps["Update Dependencies<br/>• Check for version conflicts<br/>• Update to compatible versions<br/>• Clear local cache"]
DependencyIssue --> |No| ConfigIssue{"Configuration Issue?"}
ConfigIssue --> |Yes| FixConfig["Fix Configuration<br/>• Check Maven settings<br/>• Verify environment variables<br/>• Review plugin configurations"]
ConfigIssue --> |No| CodeIssue{"Code Issue?"}
CodeIssue --> |Yes| FixCode["Fix Code Issues<br/>• Address compilation errors<br/>• Resolve test failures<br/>• Apply formatting"]
CodeIssue --> |No| EnvIssue{"Environment Issue?"}
EnvIssue --> |Yes| FixEnv["Fix Environment<br/>• Check Java version<br/>• Verify GraalVM setup<br/>• Review PATH settings"]
EnvIssue --> |No| UnknownIssue["Unknown Cause<br/>• Consult team<br/>• Search documentation<br/>• Create issue report"]
UpdateDeps --> RetryBuild["Retry Build"]
FixConfig --> RetryBuild
FixCode --> RetryBuild
FixEnv --> RetryBuild
UnknownIssue --> RetryBuild
```

### Debugging Test Failures

Systematic approach to debugging test failures:

| Symptom | Likely Cause | Debugging Steps |
|---------|--------------|-----------------|
| Test timeouts | Network latency, slow API calls | Add timeout configuration, use mock clients |
| Authentication failures | Invalid service account keys | Verify key validity, check project permissions |
| Model not found | Incorrect model configuration | Validate model aliases, check regional availability |
| Unexpected responses | API changes, version mismatches | Update model configurations, check SDK versions |

### Debug Commands

Use specialized debug commands for troubleshooting:

```bash
# Debug integration tests with verbose output
mvn test -Drun.integration.tests=true -Dorg.slf4j.simpleLogger.defaultLogLevel=debug

# Debug native compilation issues
.\build-exe.cmd -X

# Debug model availability testing
.\debug-all-us.cmd
.\debug-all-eu.cmd
```

**Section sources**
- [src/test/java/com/jguru/vertexai/VertexAiMasterMainTest.java](file://src/test/java/com/jguru/vertexai/VertexAiMasterMainTest.java#L275-L328)
- [debug-all-eu.cmd](file://debug-all-eu.cmd#L1-L32)
- [debug-all-us.cmd](file://debug-all-us.cmd#L1-L32)

## Best Practices

### Code Contribution Guidelines

Follow established best practices for code contributions:

| Practice | Rationale | Implementation |
|----------|-----------|----------------|
| Atomic Commits | Logical changes, easier review | Group related changes into single commits |
| Comprehensive Tests | Reliable code, prevent regressions | Write unit and integration tests for all changes |
| Clear Documentation | Maintainable codebase | Document complex logic, update README when needed |
| Performance Awareness | Efficient development cycle | Profile builds and tests, optimize bottlenecks |

### Security Considerations

Maintain security best practices:

- Never commit service account keys or API credentials
- Use environment variables for sensitive configuration
- Validate all user inputs and API responses
- Implement proper error handling without exposing sensitive data

### Continuous Improvement

Contribute to project improvement:

- Monitor build performance and suggest optimizations
- Identify and address code quality issues
- Contribute to documentation improvements
- Participate in code reviews and knowledge sharing

**Section sources**
- [AGENTS.md](file://AGENTS.md#L162-L205)
- [README.md](file://README.md#L37-L46)