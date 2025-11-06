# Agents Guide for Vertex AI Master CLI

This document captures best practices, patterns, and examples for building and operating agents in this repository. It is inspired by community guidance like agents.md and adapted for our Java + Picocli + Google GenAI/Vertex AI stack.


## Goals
- Provide a shared vocabulary (actions, tools, state, plans)
- Offer practical patterns (agent loop, tool-use rubric, retry and safety)
- Show working, minimal Java examples aligned with our codebase
- Define conventions for prompts, logging, and tests


## Overview
- Runtime: Java 25, Picocli CLI, optional GraalVM native
- LLMs: Google GenAI (Gemini) and Vertex AI models
- Tools: Model Context Protocol (MCP) servers available via the README
  - serper (web search)
  - context7 (documentation fetch)
  - exa (advanced RAG/search/analysis)
  - sequential-thinking (orchestrated reasoning for complex tasks)

Note: MCP tools are not yet wired directly in the Java CLI. This doc defines how to plan their integration and usage policy.


## Core Concepts
- Agent: A program that reasons over user input and context, selects tools, executes actions, and produces results.
- Tool: A capability the agent can call (LLM, search, code execution, retrieval, etc.).
- Plan: A sequence of steps (think → act → observe → refine) to reach a goal.
- State: Accumulated messages, tool results, decisions, and artifacts.


## Agent Loop Pattern
The standard agent loop:

1) Think: Parse user goal, constraints, and context. Decide on a next action.
2) Act: Call a tool (LLM, search, retrieval, code runner) with precise inputs.
3) Observe: Capture outputs, errors, and signals.
4) Reflect: Update plan, detect termination criteria.
5) Stop: When the goal is satisfied or a safe stop condition is met (time/step budget).

Guidelines:
- Keep steps idempotent where possible; prefer pure functions.
- Record decisions and evidence (logs) for reproducibility.
- Always set ceilings: max steps, max cost, and guardrails.


## Tool-Use Rubric (MCP)
When deciding which MCP server to use:
- serper (web search): Use for time-sensitive facts, current events, or discovering sources. Return citations.
- context7 (docs): Use to fetch product docs, API references, and RFCs. Prefer when the question is documentation-heavy or version-specific.
- exa (advanced RAG/analysis): Use for deep dives, synthesizing across many sources, or when you need high-quality context windows and summarization.
- sequential-thinking (orchestration): Use for multi-step tasks that require decomposition, iterative reasoning, or tool-chaining.

Selection heuristics:
- If you can answer from local code or cached state, prefer not to call external tools.
- If a single reputable doc suffices, prefer context7 over broad search.
- If ambiguity is high, start with serper to map the space, then pivot to context7/exa.
- If a task spans >2 dependent steps, consider sequential-thinking to manage flow.

Operational tips:
- Test tools individually and cache a rubric of latency, reliability, and cost.
- Include backoff/retry for transient failures.
- Log tool inputs/outputs with PII scrubbing.


## Prompts and System Instructions
Structure prompts with:
- Role and objectives
- Constraints (budgets, allowed tools, privacy)
- Expected output format (JSON schemas when applicable)
- Examples (few-shot) that match our domain

Keep prompts under control with versioned templates in `src/main/resources/prompts/` (create this folder as needed). Use placeholders for model, temperature, and topK/topP.


## Safety and Compliance
- Redact secrets (API keys, SA JSON paths) from logs and error messages.
- Respect rate limits and billing quotas. Implement exponential backoff and circuit breakers.
- Check content policy: avoid unsafe generations; prefer structured outputs and explicit instructions to the LLM.


## Observability
- Log each agent step: decision, tool called, input summary, output summary, duration.
- Surface a `--debug` flag in the CLI to increase verbosity and print planning traces.
- Consider JSONL logs for downstream analysis.


## Testing Strategy
- Unit tests: deterministic stubs for LLM/tool calls. Validate planning logic and error handling.
- Contract tests: validate request/response schemas to LLM/tool clients.
- Golden tests: store expected outputs for stable prompts (update with review only).
- Offline replay: save tool outputs for reproducible runs.


## Failure Handling
- Classify failures: user input (validation), tool (transient), model (hallucination), system (timeouts).
- Apply retries with jitter for transient errors.
- Fail fast on invalid parameters with helpful remediation tips.
- Provide fallbacks (e.g., degrade from exa to serper if exa is unavailable).


## Minimal Java Patterns

1) Synchronous single-shot generation (Gemini API):

```java
try (Client client = new Client()) {
    GenerateContentResponse response = client.models.generateContent(modelName, prompt, null);
    String text = response.text();
}
```

2) Vertex AI with Service Account (chat example):

```java
GoogleCredentials credentials = GoogleCredentials.fromStream(Files.newInputStream(Path.of(serviceAccountKeyPath)));
try (Client client = Client.builder().project(projectId).location(location).credentials(credentials).build()) {
    Chat chat = client.chats.create(modelName);
    GenerateContentResponse resp = chat.sendMessage(userMessage);
    String text = resp.text();
}
```

3) Agent loop skeleton:

```java
for (int step = 0; step < maxSteps; step++) {
    Decision d = planner.decide(state);
    if (d.shouldStop()) break;
    ToolResult r = tools.call(d.tool(), d.input());
    state = state.withObservation(d, r);
}
```

4) Result shaping with JSON schema:

```java
// Provide a system instruction to output valid JSON matching a schema.
// Validate with a JSON parser and report errors back into the loop.
```


## File/Folder Conventions
- `AGENTS.md` (this file): Ground rules and patterns
- `AGENTS.local.md`: Local or environment-specific addenda (do not commit secrets)
- `src/main/resources/prompts/`: Prompt templates, versioned by use-case
- `docs/`: Longer-form guides; prefer to summarize and link from here


## Integration Plan for MCP
Short-term:
- Keep the MCP policy in this doc; call out when to use which server (see rubric above).

Medium-term:
- Add a thin abstraction `ToolClient` to wrap MCP servers, with common retries/logging.
- Expose `--use serper|context7|exa|sequential-thinking` flag and route calls accordingly.

Long-term:
- Support multi-tool orchestration with a declarative plan (YAML/JSON) and a runtime that executes it.


## Example CLI Flows
- Quick answer with Gemini API key:
  `vertex-ai --api-key $GOOGLE_API_KEY --model-name gemini-1.5-pro-001 "Summarize the SOLID principles in bullets."`

- Vertex AI with SA:
  `vertex-ai --project-id my-proj --location us-central1 --model-name gemini-1.5-flash-001 "Generate a unit test for parsing JSON in Java."`

- Complex task (recommend sequential-thinking MCP):
  - Decompose, search with serper, gather docs via context7, synthesize with exa.


## Coding Guidelines for Agents
- Keep transport dependencies (GenAI vs Vertex) behind `VertexAiClient`/`VertexUtils`.
- Do not hard-code model names; prefer `models.properties` and CLI flags.
- Strict null-safety and explicit error returns; avoid throwing for control flow.
- Prefer small, composable methods with exhaustive logging at decision points.


## Checklist for New Agent Features
- [ ] Define the goal, inputs, outputs, and stop criteria
- [ ] Choose tools per rubric and justify
- [ ] Add prompt templates and tests
- [ ] Implement retries, timeouts, and logging
- [ ] Document usage in README and update AGENTS.md if patterns evolve


## Open TODOs
- Wire MCP tools into the CLI with a small `ToolClient` abstraction
- Add `--debug` flag for agent step tracing
- Provide prompt templates under `src/main/resources/prompts/`
- Expand unit tests for planning logic and error cases

If you see TODOs in code, please create tracking issues and link back here.


## Attribution
This document is informed by industry best practices and community guides (e.g., agents.md). It is adapted for our Java Vertex AI CLI context.
