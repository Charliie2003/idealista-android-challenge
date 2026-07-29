# ADR-0001 — Modularization strategy

**Status:** Accepted
**Date:** 2026-07-28
**Deciders:** Carlos Hinojosa

## Context

The Idealista Android Challenge requires demonstrating clean architecture with both XML and
Compose UI. The app is small enough to ship as a monolith, but module boundaries are
required to show separation of concerns and enforce dependency rules at build time.
The key tension is between simplicity (fewer modules) and encapsulation (stronger boundaries).
A separate `:data` module is common in enterprise projects but adds a module for no
architectural gain at this scale — the app has one network source and one database.

## Decision

Use six modules: `:app`, `:core:domain`, `:core:network`, `:core:database`,
`:feature:list`, `:feature:detail`. Repository **interfaces** live in `:core:domain`.
Repository **implementations** live in `:app` and are provided via Hilt modules.
This keeps features decoupled from the data layer without introducing a `:data`
module whose only job would be wiring — a job `:app` already does as the composition root.

## Alternatives considered

- **Single `:app` module.** Rejected — no build-time enforcement of layer boundaries;
  a ViewModel could accidentally import Room entities.
- **Add a `:data` module** for repository implementations. Rejected — premature at this
  scale; `:app` already has a wiring role and adding `:data` creates a module with no
  domain logic of its own, only glue code. Migration path is documented as a Follow-up.
- **Feature modules own their repository implementations.** Rejected — forces each feature
  to depend on `:core:network` and `:core:database`, breaking the boundary matrix.

## Consequences

- **Positive:** Features are fully decoupled from the data layer. Swapping a repository
  implementation (e.g. adding an offline cache) never touches feature code. Build graph
  is acyclic and auditable.
- **Negative:** `:app`'s `build.gradle.kts` depends on all other modules; it is the
  widest dependency scope in the project. Acceptable — `:app` is the composition root
  by design.
- **Follow-ups:** If the project grows beyond this challenge, extract repository
  implementations to a `:data` module and re-wire Hilt. No feature code changes needed.

## References

- `CLAUDE.md §3.1` — Repository implementation location rationale
- `.claude/skills/module-boundaries/SKILL.md` — Full dependency matrix
