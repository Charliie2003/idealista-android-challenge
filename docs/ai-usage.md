# AI collaboration — how this project was actually built

---

## The harness

`CLAUDE.md` is the single source of truth for the entire development workflow. It defines the mission, enforces non-negotiable rules (Kotlin only, no business logic in UI, three model layers, Hilt everywhere), and names six subagents — each with a locked remit — so that no agent wanders outside its domain. Five frozen skills (`api-contract`, `module-boundaries`, `kotlin-conventions`, `testing-patterns`, `adr-writing`) are loaded before each task in their area rather than re-derived from scratch. ADRs are an append-only record: once accepted, they are never edited. The coordinator (Claude Code CLI, model `claude-sonnet-4-6`) orchestrates the agents; it does not write feature code itself. No agent commits, stages, or pushes — git operations are always explicitly requested by the human.

---

## Concrete examples of AI contributions

### 1. Multi-module scaffold (IAC-01/02/03)

`android-architect` generated all nine `build.gradle.kts` files, `libs.versions.toml`, `settings.gradle.kts`, all Hilt module skeletons, stub Fragments, and stub ViewModels from the spec in `CLAUDE.md §3`. Choices like KSP over KAPT and Java 17 toolchain were escalated and confirmed before code was written. The `ListingNavigator` interface pattern (to avoid a circular dependency between `:feature:list` and `:app`) was proposed and accepted in the same session. ADR-0001 was authored in that same commit.

### 2. `api-contract` skill front-loaded defensive handling

The three API quirks — top-level `price` vs `priceInfo.price`, static `adid=1` detail endpoint, optional `parkingSpace` field — were documented in `.claude/skills/api-contract/SKILL.md` before any mapper was written. The first `PropertyDtoMapper` draft therefore already used `priceInfo.price` as the source of truth and handled `parkingSpace` absence. This is the point of a frozen skill: constraints are articulated once and respected everywhere, not discovered per-class.

### 3. Repository extraction refactor (commit `075f78d`)

`android-architect` proposed moving repository implementations from `:app` into a new `:core:data` module. The justification: `:app` as the data layer makes the DI graph opaque and prevents unit-testing the repository in isolation. The refactor also added `InMemoryPropertiesCache` with a `@Volatile` snapshot and `Mutex` for stampede protection (concurrent cache misses trigger exactly one network call). `testing-specialist` covered both with 13 new tests in `PropertiesRepositoryImplTest`. ADR-0001 was updated to reflect the final module count.

### 4. Detail screen implementation (IAC-30/31)

`ui-compose-engineer` authored 12 Composables for the detail screen — `PropertyGallery` (HorizontalPager + auto-scroll), `GalleryTopBar` (back/share/favorite overlays), `HighlightsRow`, `DescriptionBlock` with `animateContentSize`, `CharacteristicsFlow` as a `FlowRow` of `SuggestionChip` (ADR-0009), and `EnergyCertificationCard`. Three `CLAUDE.md §10` rules were applied directly during this session: `settledPage` not `currentPage` as the `LaunchedEffect` key to prevent mid-animation coroutine cancellation; `SuggestionChip` not `AssistChip(enabled = false)` for informational content; and `Modifier.background(color)` not `painterResource()` for the map placeholder.

### 5. `testing-specialist` caught real regressions

`DetailViewModelTest` and `PropertiesRepositoryImplTest` were written using the Turbine + `UnconfinedTestDispatcher` pattern from `testing-patterns` skill. The mapper tests caught a real gap: the initial `PropertyDtoMapper` did not handle the optional `parkingSpace` field — a `PropertyDtoMapperTest` fixture exposed the null-pointer path and the production code was fixed. `code-reviewer` caught two uses of `collectAsState()` instead of `collectAsStateWithLifecycle()` in `DetailFragment` before they were committed — a lifecycle correctness issue, not a style nit.

---

## What I did NOT delegate

All product decisions were made by the human: favorite re-tap semantics (upsert, not no-op), error copy in `strings.xml`, the XML-for-listing / Compose-for-detail split, and the challenge-only static endpoint fallback strategy. All library introductions required human confirmation before an ADR was written — no dependency was added speculatively. Every commit was reviewed by the human before landing; no agent staged or pushed. The architecture rules in `CLAUDE.md §2` and `§3` were authored by the human and treated as immutable constraints by every agent.

---

## Honest limitations observed

**1. Stated artifact vs. committed artifact gap.** `ai-usage.md` originally claimed that `docs/adr/0010-static-map-placeholder.md` was authored in IAC-33. The file was never committed and the decision was ultimately removed from the implementation. The gap between stated intent and actual artifact is real — the original entry has been removed rather than preserved as a broken reference.

**2. Mapper test as regression net.** The initial `PropertyDtoMapper` draft missed the optional `parkingSpace` field. The AI did not catch this during generation — it was caught by a test fixture written afterward. Tests are not a formality: they exposed a bug the generation pass missed.

**3. Module boundary enforcement is manual.** Nothing in Gradle prevents a developer from adding `implementation(project(":core:data"))` to `:feature:list`. The boundaries are enforced by the `code-reviewer` checklist and the human reviewing diffs — not by a build-graph constraint. The Gradle script in `.claude/skills/module-boundaries/SKILL.md` is a nice-to-have that was not implemented.

---

## Reproducing this workflow

Paste `CLAUDE.md` as the project's single source of truth: mission, non-negotiables, module map, agent remit table. Define skills as frozen references loaded before each task (not derived on-the-fly). Run `code-reviewer` last, against the full diff, before any commit. The key discipline is that the human owns all product and architecture decisions; the agents own execution within those decisions. The harness works on any Android project where the architecture is pre-decided and documented.
