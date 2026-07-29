---
name: module-boundaries
description: Read this before adding any inter-module dependency, moving code between modules, or when Gradle throws a circular-dependency error. Contains the frozen dependency matrix and the rules for what can import what.
---

# Module Boundaries

Multi-module discipline is what separates a codebase that scales from one that becomes a tangled mess. These rules are enforced at review time.

## The matrix

Rows are modules; columns are what they may depend on. An `x` means the dependency is allowed.

|                    | :core:domain | :core:network | :core:database | :core:data | :feature:list | :feature:detail | :app |
|--------------------|:-----------:|:-------------:|:--------------:|:----------:|:-------------:|:---------------:|:----:|
| **:core:domain**    |      —      |               |                |            |               |                 |      |
| **:core:network**  |      x      |       —       |                |            |               |                 |      |
| **:core:database** |      x      |               |       —        |            |               |                 |      |
| **:core:data**     |      x      |       x       |       x        |     —      |               |                 |      |
| **:feature:list**  |      x      |               |                |            |       —       |                 |      |
| **:feature:detail**|      x      |               |                |            |               |        —        |      |
| **:app**           |      x      |               |                |     x      |       x       |        x        |  —   |

Anything not marked `x` is forbidden and will be rejected in review.

## The rules in prose

1. **`:core:domain` depends on nothing.** It is pure Kotlin. Apply the `java-library` plugin and `kotlin("jvm")`. Never `com.android.library`.
2. **`:core:network` depends only on `:core:domain`.** It needs the domain port interfaces to implement them; that's all.
3. **`:core:database` depends only on `:core:domain`.** Same reasoning.
4. **`:core:data` depends on `:core:domain`, `:core:network`, and `:core:database`.** It is the only module allowed to combine all three. Repository implementations and `DispatcherProvider` live here.
5. **Feature modules depend only on `:core:domain`.** They must not know Retrofit exists. They must not know Room exists.
6. **`:app` is the composition root.** It depends on `:core:domain`, `:core:data`, `:feature:list`, `:feature:detail`. It does NOT directly depend on `:core:network` or `:core:database` — that coupling lives in `:core:data`.
7. **Features never import each other.** If `:feature:detail` needs something from `:feature:list`, that something belongs in `:core:domain`.

## Practical consequences

### Where does a repository interface go?
`:core:domain`. It's a domain contract.

### Where do data source interfaces go?
`:core:domain`. They are **domain port interfaces** — the domain defines what it needs from the outside world (remote API, local DB), and the adapters in `:core:network`/`:core:database` implement them. Do NOT put them in the adapter modules. If `RemotePropertiesDataSource` lives in `:core:network`, KSP's `InjectProcessingStep` in `:core:data` cannot resolve it when processing `@Inject` constructors, even with `implementation(project(":core:network"))`.

### Where does a repository implementation go?
`:core:data`. It needs both `:core:network` (RemoteDataSource) and `:core:database` (LocalDataSource), which `:core:data` is the only module allowed to combine.

### Where do use cases go?
`:core:domain`. Pure Kotlin, injected via Hilt from `:app`.

### Where do mappers go?
- `Dto → Domain` — in `:core:network`, in a file like `PropertyDtoMapper.kt`. Rationale: the DTO belongs to network, so the mapping belongs there too. The domain doesn't know a DTO exists.
- `Entity → Domain` — in `:core:database`, same rationale.
- `Domain → UiModel` — in the feature module that owns the UiModel.

### Where do DTOs and Entities go?
DTOs live in `:core:network` and Entities in `:core:database`. Mark them `internal` to prevent leakage — **but only if they do not appear as parameters or return types in Hilt `@Provides`/`@Binds` methods or `@Inject` constructors**. If a type participates in the DI graph it must be `public`; KSP's `InjectProcessingStep` cannot resolve `internal` types and the build fails with an opaque error.

### Where does `UiText` (sealed class for i18n) go?
`:core:domain`, because features need it to expose errors from ViewModels without a `Context`.

### Where do design system components go?
For this challenge, keep them in `:app/res/values/themes.xml` and `:app/res/drawable/`. If a `:core:designsystem` module is created later, it's a `com.android.library` that depends on nothing except AndroidX UI libraries — never on `:core:domain`.

## What to do when this feels restrictive

The restriction is the point. If you feel forced to break a rule, either:

1. **The rule is right and your design is wrong** — usually true. Rethink where the code lives.
2. **You've discovered a case the rules don't cover** — stop, write it up, and update this document before proceeding.

Never break a rule to unblock yourself and "fix it later." Later never comes and the reviewer will find it.

## Gradle enforcement (nice-to-have)

If time allows, add a Gradle convention plugin or a simple task that fails the build when a forbidden dependency appears. This is a strong signal in the review. Even a shell script is enough:

```bash
# scripts/check-module-boundaries.sh
if grep -rn "core.network\|core.database" feature/; then
  echo "Feature module illegally references network or database"
  exit 1
fi
```
