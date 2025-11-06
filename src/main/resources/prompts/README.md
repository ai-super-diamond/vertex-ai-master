Prompt templates live here. Keep templates versioned and parameterized.

Conventions:
- File types: .md for freeform prompts, .json for schema-enforced outputs.
- Use placeholders like {{MODEL}}, {{TEMPERATURE}}, {{TOP_K}}, {{TOP_P}}, {{TASK}}, {{CONTEXT}}, {{CONSTRAINTS}}.
- Keep prompts under ~1-2k tokens and prefer explicit structure.

Suggested structure:
- system.md: System role and high-level rules
- task_answer.md: A generic question-answer prompt
- agent_plan.md: A planning prompt that yields a JSON plan
- schema/*.json: JSON Schemas for outputs where possible
