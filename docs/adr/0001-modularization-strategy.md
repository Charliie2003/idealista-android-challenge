# ADR-0001 — Modularization strategy

**Status:** Accepted  
**Date:** 2026-07-28  
**Updated:** 2026-07-31  
**Deciders:** Carlos Hinojosa

## Context

The Idealista Android Challenge requires demonstrating clean architecture with both XML and
Compose UI. The app is small enough to ship as a monolith, but module boundaries are
required to show separation of concerns and enforce dependency rules at build time.
The key tension is between simplicity (fewer modules) and encapsulation (stronger boundaries).
A critical constraint is that feature modules must never import Room or Retrofit — this must
be enforced by the build graph, not by convention.

## Decision

Use seven modules: `:app`, `:core:domain`, `:core:data`, `:core:network`, `:core:database`,
`:core:design`, `:feature:list`, `:feature:detail`.

- Repository **interfaces** and DataSource **port interfaces** live in `:core:domain`.
- Repository **implementations** live in `:core:data` — the only module that can see both
  `:core:network` and `:core:database` simultaneously. This makes the Dependency Inversion
  Principle explicit at the build level.
- `:app` is the composition root only: `Application` class, `MainActivity`, `NavGraph`, and
  the `ClockModule` Hilt entry point. It holds no repository implementations.
- `:core:design` is a standalone Android library owning all Compose design tokens
  (`Color`, `Type`, `IdealistaTheme`). Feature modules import it without pulling in any
  business logic.

## Alternatives considered

- **Single `:app` module.** Rejected — no build-time enforcement of layer boundaries;
  a ViewModel could accidentally import Room entities.
- **Repository implementations in `:app`.** Rejected — makes `:app` depend directly on
  `:core:network` and `:core:database`, widening the composition root's scope unnecessarily
  and making the DI graph opaque to reviewers.
- **Feature modules own their repository implementations.** Rejected — forces each feature
  to depend on `:core:network` and `:core:database`, breaking the boundary matrix.

## Consequences

- **Positive:** Features are fully decoupled from the data layer. Swapping a repository
  implementation (e.g. replacing the in-memory cache with a database cache) never touches
  feature code. Build graph is acyclic and auditable.
- **Positive:** `:core:data` is the single module whose `build.gradle.kts` depends on both
  `:core:network` and `:core:database`. This boundary is visible and testable in isolation
  (`PropertiesRepositoryImplTest` runs without touching `:app`).
- **Negative:** `:core:data`'s `build.gradle.kts` has the widest dependency scope in the
  project (`:core:domain` + `:core:network` + `:core:database`). Acceptable — it is the
  aggregation layer by design.

## References

- `CLAUDE.md §3.1` — Repository implementation location rationale
- `.claude/skills/module-boundaries/SKILL.md` — Full dependency matrix
