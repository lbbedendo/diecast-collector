# Domain Docs

How the engineering skills should consume this repo's domain documentation when exploring the codebase.

## Before exploring, read these

- **`CONTEXT.md`** at the repo root.
- **`docs/adr/`**: ADRs are split per module — `docs/adr/api/` (Spring Boot API) and
  `docs/adr/app/` (KMP app), each numbered independently, indexed in `docs/adr/README.md`.
  Read the ADRs for the module you're about to work in. New ADRs go in the matching
  module folder and get a row in the README index.

If any of these files don't exist, **proceed silently**. Don't flag their absence; don't suggest creating them upfront. The `/domain-modeling` skill (reached via `/grill-with-docs` and `/improve-codebase-architecture`) creates them lazily when terms or decisions actually get resolved.

## File structure

Single-context repo, with ADRs split per module:

```
/
├── CONTEXT.md
├── docs/adr/
│   ├── README.md                ← index of all ADRs
│   ├── api/
│   │   └── 0001-migrate-to-spring-boot-4-and-java-25.md
│   └── app/
├── api/
└── app/
```

## Use the glossary's vocabulary

When your output names a domain concept (in an issue title, a refactor proposal, a hypothesis, a test name), use the term as defined in `CONTEXT.md`. Don't drift to synonyms the glossary explicitly avoids.

If the concept you need isn't in the glossary yet, that's a signal: either you're inventing language the project doesn't use (reconsider) or there's a real gap (note it for `/domain-modeling`).

## Flag ADR conflicts

If your output contradicts an existing ADR, surface it explicitly rather than silently overriding:

> _Contradicts api/ADR-0001 (Spring Boot 4 / Java 25), but worth reopening because…_
