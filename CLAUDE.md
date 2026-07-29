# CLAUDE.md

> Single source of truth for AI collaborators working on the Idealista Android Challenge.
> Read this file completely before every non-trivial task.
> When in doubt, ask the user — never invent architecture, dependencies, or product decisions.

---

## 1. Mission

Deliver a technical challenge for Idealista that competes on three dimensions:

1. **Architecture rigor** — Clean multi-module structure with strictly enforced dependency boundaries.
2. **Craft** — Idiomatic Kotlin, thoughtful UI on both XML and Compose, tests that catch real bugs.
3. **AI workflow visibility** — This harness (CLAUDE.md + agents + skills) is itself a deliverable. It must be excellent because a senior Android reviewer at Idealista will read it.

**Reviewer profile:** senior Android engineer at a company with millions of MAU. Assume they will read the code, check the tests, and evaluate architecture at a glance. They will not tolerate over-engineering, magic, or unjustified complexity. Optimize for signal, not for showing off.

---

## 2. Non-negotiable rules (blocking)

Violating any of these blocks the change. If a task requires breaking one, stop and ask.

- **Kotlin only.** No Java files.
- **XML views for the listing, Jetpack Compose for the detail.** Non-negotiable — required to demonstrate both.
- **No business logic in Activities, Fragments, or Composables.** Ever. If logic sneaks in, extract it to a ViewModel or a UseCase.
- **Three model layers, never conflated:** `*Dto` (data), `*` domain model (domain), `*UiModel` (ui). Mappers live at the boundary and are covered by tests.
- **No hardcoded user-facing strings.** Everything in `res/values/strings.xml` — this is checked by lint and by the reviewer.
- **Coroutines + Flow.** No RxJava, no `AsyncTask`, no raw threads.
- **StateFlow** exposes ViewModel state, except in the XML listing where **LiveData is allowed** because it plays well with the XML lifecycle. Justify the choice in the ViewModel's KDoc.
- **Hilt for DI everywhere.** No manual `ServiceLocator`. No `object` singletons holding state.
- **Room for persistence.** The favorites table is `favorite(property_id: String PRIMARY KEY, favorited_at: Long NOT NULL)`. Nothing else lives there.
- **No secrets, API keys, or personal data committed.** `.gitignore` blocks `local.properties`, `*.jks`, `google-services.json`, `.idea/`, `.DS_Store`.
- **Every public class in a `core:*` module has a KDoc explaining its purpose.** UI classes need KDoc only when the intent isn't obvious from the name.

---

## 3. Module structure

```
:app                        Wiring only. Application class, MainActivity, Hilt entry points,
                            NavGraph, ClockModule. No repository implementations.
                            Depends on: :core:domain, :core:data, :feature:list, :feature:detail.

:core:domain                Pure Kotlin (no Android). Domain models, repository interfaces,
                            data source port interfaces, use cases, Result wrappers.
                            Depends on: nothing.

:core:data                  Repository implementations, DispatcherProvider, DataModule (@Binds).
                            The data-layer aggregator — the only module that can see both
                            :core:network and :core:database simultaneously.
                            Depends on: :core:domain, :core:network, :core:database.

:core:network               Retrofit + OkHttp + kotlinx.serialization. DTOs, IdealistaApi,
                            RemotePropertiesDataSourceImpl, NetworkModule, NetworkBindingsModule.
                            Depends on: :core:domain.

:core:database              Room database, FavoriteEntity, FavoritesDao,
                            LocalFavoritesDataSourceImpl, DatabaseModule, DatabaseBindingsModule.
                            Depends on: :core:domain.

:feature:list               Listing screen (XML + Fragment + ListAdapter + LiveData ViewModel).
                            Depends on: :core:domain. NEVER on :core:network or :core:database.

:feature:detail             Detail screen (Fragment host + Compose content + StateFlow ViewModel).
                            Depends on: :core:domain. NEVER on :core:network or :core:database.
```

### 3.1 Where do repository implementations live?

Repository **interfaces** live in `:core:domain`. Repository **implementations** live in `:core:data` and are bound via Hilt's `DataModule`. `:app` does not hold any implementation — it is the composition root only.

Rationale: `:app` cannot safely act as the data layer because it makes the DI graph opaque and prevents unit-testing the repository in isolation. `:core:data` is the right aggregation point: it can see both `:core:network` (remote) and `:core:database` (local) without leaking those dependencies to feature modules.

**Data source interfaces** (`RemotePropertiesDataSource`, `LocalFavoritesDataSource`) live in `:core:domain` as domain port interfaces. This is the Dependency Inversion Principle: the domain defines what it needs; the adapters in `:core:network` and `:core:database` implement it. Do NOT move them into the adapter modules — see §8 for why that breaks Hilt/KSP.

### 3.2 Dependency rules (enforced)

- Features never import each other.
- Features never depend on `:core:network` or `:core:database`.
- `:core:domain` depends on nothing (pure Kotlin module — apply the `java-library` plugin, not `com.android.library`).
- `:core:data` is the only module that may depend on both `:core:network` and `:core:database`.
- `:app` depends on `:core:data` (not directly on `:core:network` or `:core:database`).
- Circular dependencies are a build failure. If Gradle doesn't catch it, the reviewer will.

See `.claude/skills/module-boundaries/SKILL.md` for the full matrix.

---

## 4. Tech stack (frozen)

| Concern | Choice | Notes |
|---|---|---|
| Language | Kotlin `2.0+` | K2 compiler. |
| Build | Gradle KTS + `libs.versions.toml` | Version catalog is mandatory. |
| DI | Hilt | Modules per `core:*` module. |
| Async | Coroutines + Flow | No RxJava. |
| Network | Retrofit + OkHttp + kotlinx.serialization | Interceptor logs only in `debug`. |
| Persistence | Room | With Flow-returning DAOs. |
| Images | Coil | For both XML (`ImageView.load`) and Compose (`AsyncImage`). |
| Navigation | Jetpack Navigation with a single Activity | Two destinations: list, detail. |
| Listing UI | XML + ViewBinding + `ListAdapter` + `DiffUtil` | LiveData allowed. |
| Detail UI | ComposeView inside a Fragment | StateFlow required. |
| Testing | JUnit 4 + MockK + Turbine + Coroutines Test + Espresso + Compose UI Test | See `.claude/skills/testing-patterns/SKILL.md`. |
| Static analysis | Detekt + Ktlint (via Detekt) + Android Lint | All must pass in CI-equivalent local run. |

**Do not add libraries without justification in an ADR.** Reviewer will scan the version catalog and question every entry.

---

## 5. Subagent delegation

Delegate to specialized subagents when the task falls squarely in their remit. The coordinator (you, reading this) orchestrates but does not do everything.

| Task | Delegate to |
|---|---|
| Architecture decisions, module setup, DI graph, Gradle config | `android-architect` |
| API contract questions, DTOs, mappers, use cases, business rules | `domain-expert` |
| RecyclerView, ListAdapter, DiffUtil, XML layouts, ViewBinding, LiveData ViewModels | `ui-xml-engineer` |
| ComposeView setup, Composables, StateFlow → Compose, Compose theming | `ui-compose-engineer` |
| Any test (unit, integration, UI), test strategy questions | `testing-specialist` |
| Final review before commit, PR-style review, quality gate | `code-reviewer` |

**Delegation protocol:** state the task, hand off the relevant files, and constrain the subagent's scope explicitly. Do not let subagents wander outside their remit.

**Never delegate:** product decisions, changes to this file, changes to the module boundaries. Those come back to the user.

---

## 6. Skills reference

Load the relevant skill before starting a task in that area. Skills are frozen references, not suggestions.

- `module-boundaries` — Exact matrix of what can depend on what.
- `kotlin-conventions` — Naming, formatting, idioms specific to this project.
- `testing-patterns` — Test recipes with copy-pasteable structure.
- `api-contract` — The two endpoints, their quirks, and how we handle them.
- `adr-writing` — Format and location of Architecture Decision Records.

---

## 7. Verification commands

Run these before declaring any task done:

```bash
./gradlew clean
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew detekt          # if configured
```

For UI test tasks, also:

```bash
./gradlew connectedDebugAndroidTest
```

If any command fails, the task is not done. Fix it before returning control.

---

## 8. What NOT to do

- Do not add Paging 3, Hilt Navigation Compose, DataStore, or any other library "just in case." Every dependency is a claim you must defend.
- Do not use `runBlocking` in production code. Only in tests, and only when strictly required.
- Do not swallow exceptions with empty catch blocks. Model errors as `Result` or as `UiState.Error`.
- Do not expose Room entities or Retrofit DTOs to ViewModels or the UI. Cross the boundary via mappers.
- Do not use `!!` on nullable values. If null is impossible here, prove it with an early return or `requireNotNull`.
- Do not reproduce article/text content from any source into this project without paraphrasing and attribution — the property descriptions from the API are user-visible data, not copyrightable content we own.
- Do not commit generated screenshots or GIFs at commit time — put them in `docs/media/` at the end.
- Do not mock what you own (repositories are fine to fake, but prefer real UseCases over mocked UseCases in ViewModel tests when they're simple).
- **Do not mark `internal` any type that participates in Hilt's DI graph.** If a class is used as a constructor parameter in an `@Inject` class, or as the return type of a `@Provides`/`@Binds` method, it must be `public`. Kotlin's `internal` prevents KSP's `InjectProcessingStep` from resolving the type and the build fails with a cryptic error.
- **Do not move data source interfaces out of `:core:domain`.** `RemotePropertiesDataSource` and `LocalFavoritesDataSource` are domain port interfaces. If they live in `:core:network` or `:core:database`, KSP in `:core:data` cannot resolve them when processing `@Inject` constructors — even with `implementation(project(":core:network"))` — because KSP's symbol resolution does not traverse transitive `implementation` scopes the same way `javac` does.

---

## 9. Response protocol for AI collaborators

When taking on a task:

1. **Read this file.** Confirm the task doesn't violate §2.
2. **Read the relevant skill(s).** Do not re-derive conventions.
3. **Consider delegation.** If the task belongs to a subagent, delegate rather than doing it yourself.
4. **State assumptions before coding.** If any assumption is load-bearing, ask.
5. **Run the verification commands.** Not optional.
6. **Report back with:** what you did, why, what you didn't do and why, and any decisions that should become an ADR.

Silence on trade-offs is a red flag. Surface them.

---

## 10. Definition of Done (per story)

A story is done when all apply:

- Code compiles cleanly.
- Unit tests exist for any non-trivial logic added.
- Lint and Detekt pass with no new warnings.
- The change is documented in a commit message following Conventional Commits (`feat:`, `fix:`, `test:`, `docs:`, `refactor:`, `chore:`).
- If the change altered architecture or introduced a library, an ADR was written.
- `docs/ai-usage.md` was updated if AI made a substantive contribution to the story.

---

## 11. Repository layout (top-level)

```
/                           Gradle root, settings.gradle.kts, libs.versions.toml
/app                        Application module
/core/*                     Core modules (see §3)
/feature/*                  Feature modules
/docs                       Human-readable documentation (README, ai-usage, ADRs)
/docs/adr                   Architecture Decision Records
/.claude                    AI harness — coordinator agents and skills
```
