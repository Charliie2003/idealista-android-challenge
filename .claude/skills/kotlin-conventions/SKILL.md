---
name: kotlin-conventions
description: Read this before writing any Kotlin file. Contains naming, formatting, idioms, and structural conventions specific to this project. Deviations require justification in review.
---

# Kotlin Conventions

Follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html) except where this document overrides them.

## Naming

| Kind | Convention | Example |
|---|---|---|
| Package | lowercase, no underscores | `com.idealista.challenge.feature.list` |
| Class | UpperCamelCase | `PropertyDetail` |
| Function | lowerCamelCase | `mapToDomain()` |
| Property | lowerCamelCase | `propertyId` |
| Constant | SCREAMING_SNAKE_CASE | `MAX_IMAGE_COUNT` |
| Test | backticks, describe behavior | `` `emits error when network fails` `` |
| DTOs | suffix with `Dto` | `PropertyDto` |
| Entities | suffix with `Entity` | `FavoriteEntity` |
| UI models | suffix with `UiModel` | `PropertyCardUiModel` |
| UseCases | verb + `UseCase` | `ToggleFavoriteUseCase` |
| ViewModels | screen + `ViewModel` | `ListingViewModel` |
| State | `<Screen>UiState` (sealed) | `ListingUiState` |

## File organization

- **One top-level class per file** when the class is > 50 lines.
- **Related small types can share a file** (`ListingUiState.kt` contains the sealed interface and its data class variants).
- **Extension functions** live in a file named after the type they extend (`InstantExt.kt`), not `Utils.kt`.

## Imports

- **No wildcard imports.** IDE setting: `Editor → Code Style → Kotlin → Imports → Use single name import`.
- **Imports sorted alphabetically** (Ktlint enforces this).

## Nullability

- **Never `!!`** on nullable values in production code. If you're sure it's non-null, prove it with `requireNotNull(x)` or an early return.
- **Prefer `?:` (Elvis) for defaults** over `if (x == null) default else x`.
- **`checkNotNull()` for programmer errors, `requireNotNull()` for input validation.**

## Coroutines

- **Suspending functions** are called from a coroutine scope, never wrapped in `runBlocking` in production code.
- **Dispatchers are injected** via a wrapper interface, never referenced directly:

```kotlin
interface DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}
```

Reason: makes tests inject `UnconfinedTestDispatcher` cleanly. Provided by Hilt in `DispatchersModule`.

- **`Dispatchers.Main.immediate` for UI-driven flows** where an unnecessary post to the message queue would be visible.
- **`flowOn(dispatchers.io)` for I/O flows,** at the source, not the collector.
- **`viewModelScope.launch { ... }` for one-shot actions from the ViewModel.**
- **`stateIn(viewModelScope, WhileSubscribed(5_000), initial)` for state flows.**

## Data classes

- **Use `data class`** for anything that is a value carrier (models, states).
- **Do not use `data class` for classes with behavior** — those are regular classes.
- **Copy semantics via `copy()`** — do not mutate.

## Sealed hierarchies for states

```kotlin
sealed interface ListingUiState {
    data object Loading : ListingUiState
    data class Content(val properties: List<PropertyCardUiModel>) : ListingUiState
    data object Empty : ListingUiState
    data class Error(val message: UiText) : ListingUiState
}
```

- **`sealed interface` preferred over `sealed class`** — allows implementations in different files if needed.
- **`data object`** for stateless variants (Kotlin 1.9+).
- **Never a generic `success: Boolean` flag** — model the states explicitly.

## Result and errors

Repository operations return either `Flow<T>` (for observations that don't error) or `Result<T>` (for one-shots that can fail):

```kotlin
suspend fun getDetail(id: String): Result<PropertyDetail>
fun observeProperties(): Flow<List<Property>>
```

For domain errors, prefer a custom sealed type:

```kotlin
sealed class DomainError : Throwable() {
    data object Network : DomainError()
    data class Http(val code: Int) : DomainError()
    data object Parse : DomainError()
    data class Unknown(val cause: Throwable) : DomainError()
}
```

Catch technical exceptions at the repository boundary and map them.

## Extensions vs member functions

Use an **extension function** when:
- The function operates on a type you don't own.
- The function is a pure transformation (`.toDomain()`, `.formatAsPrice()`).

Use a **member function** when:
- The function depends on other members or state.
- The function is polymorphic (a subtype might override).

## Visibility

- **Prefer `internal` over `public`** unless a class must cross a module boundary.
- **`private` for helpers used within a single file or class.**
- **DTOs and Entities are `internal`.** Nothing outside `:core:network` should reference a DTO by name.

## Anti-idioms to avoid

- `object` used as a mutable singleton holder (`object AppState { var user: User? = null }`) — never. Use Hilt scoping.
- `companion object` full of unrelated constants — extract to a top-level object or split.
- `apply` used to configure a call — use named arguments or a builder instead.
- Chains of `let/run/apply/also` that make the flow unreadable — break into named `val`s.
- `String.format("%s - %s", a, b)` — use string templates `"$a - $b"`.
- `for (i in 0 until list.size)` — use `list.indices` or `forEachIndexed`.

## Formatting

Ktlint's defaults, with two overrides:

- **Max line length: 120.**
- **Trailing commas: required** for multi-line parameter lists and collections (Kotlin 1.4+ style).

## KDoc

- **Public API in `:core:domain`** — always documented.
- **Public API in `:core:network`, `:core:database`** — documented if the intent isn't obvious.
- **UI classes** — documented only when non-obvious.
- **`@param`, `@return`, `@throws`** used correctly. Don't add `@param name The name` when the parameter is `name: String` and self-explanatory — that's noise.
