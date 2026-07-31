# Idealista Android Challenge

Technical challenge for Idealista. Demonstrates clean multi-module Android architecture with XML listing UI, Jetpack Compose detail UI, Hilt DI, Jetpack Navigation, and Room persistence.

---

## How to build

**Requirements:** Android Studio Meerkat or later, JDK 17, Android SDK 35.

```bash
# Clone and build
git clone <repo-url>
cd IdealistaChallenge
./gradlew clean assembleDebug

# Run lint
./gradlew lintDebug

# Run unit tests
./gradlew testDebugUnitTest

# Verify module boundaries (must print OK)
./gradlew :feature:list:dependencies | grep -E "core.network|core.database" && echo "VIOLATION" || echo "OK"
./gradlew :feature:detail:dependencies | grep -E "core.network|core.database" && echo "VIOLATION" || echo "OK"
```

---

## SDK versions

| Property | Value | Rationale |
|---|---|---|
| `compileSdk` | 36.1 (Android 16 QPR1) | Highest installed SDK. Uses AGP 9.x extended API: `release(36) { minorApiLevel = 1 }`. |
| `targetSdk` | 36 | Android 16 stable runtime behavior. |
| `minSdk` | 24 (Android 7.0) | Covers ~97% of active Android devices (Google Play distribution, 2025). Keeps the code free of `Build.VERSION` guards for most modern APIs. |
| JVM toolchain | Java 17 | Required by AGP 9.x. LTS release, widely supported by CI environments. |

Toolchain: AGP 9.1.1 / Kotlin 2.2.10 / Gradle 9.3.1.

> **Note on lint suppressed checks:** `GradleDependency`, `AndroidGradlePluginVersion`, and `NewerVersionAvailable` are suppressed. The reason: `core-ktx ≥1.19.0` and `lifecycle ≥2.11.0` require compileSdk 37 (Android 17), which is not installed in this environment. Library versions are pinned to the latest compatible with android-36.1. These suppressions are documented inline in `app/build.gradle.kts`.

---

## Module structure

Seven modules with strictly enforced dependency boundaries.

```
         ┌──────────────────────────────────────────────────────┐
         │                       :app                          │
         │  Application class · MainActivity · NavGraph        │
         │  Hilt entry points (no repository implementations)  │
         └──┬──────────────────────────────────────────────────┘
            │
 ┌──────────▼──────────┐
 │     :core:data      │
 │  Repository impls   │
 │  InMemoryCache      │
 │  DispatcherProvider │
 └──┬──────────────┬───┘
    │              │
┌───▼──────┐  ┌────▼─────┐   ┌──────────────────────┐   ┌────────────────────┐
│:core:    │  │ :core:   │   │   :feature:list       │   │  :feature:detail   │
│network   │  │ database │   │  XML · ViewBinding    │   │  ComposeView       │
│Retrofit  │  │ Room     │   │  ListAdapter · Diff   │   │  StateFlow ViewModel│
│OkHttp    │  │ FavoriteDao  │  LiveData ViewModel   │   └──────────┬─────────┘
│DTOs      │  │          │   └──────────┬────────────┘             │
└────┬─────┘  └────┬─────┘             │                           │
     │              │                  └──────────┬────────────────┘
     └──────┬───────┘                             │
    ┌────────▼──────────────────────┐    ┌────────▼──────────┐
    │        :core:domain           │    │   :core:design    │
    │  (pure Kotlin — java-library) │    │  Color · Type     │
    │  Domain models · UseCases     │    │  IdealistaTheme   │
    │  Repository interfaces        │    └───────────────────┘
    │  DataSource port interfaces   │
    └───────────────────────────────┘
```

**Dependency rules:**
- Feature modules depend only on `:core:domain` and `:core:design`. They are completely unaware of Retrofit or Room.
- `:core:domain` is pure Kotlin (`java-library` plugin). It imports nothing from `androidx.*`.
- `:core:design` is an Android library (`com.android.library`) with no project-level imports — Compose requires the Android runtime but it imports nothing from `:core:domain` or any other project module.
- `:core:data` is the **only** module that depends on both `:core:network` and `:core:database`. It owns all repository implementations, the in-memory properties cache, and `DispatcherProvider`.
- `:app` depends on `:core:data` and both feature modules. It does **not** depend directly on `:core:network` or `:core:database`.

See `docs/adr/0001-modularization-strategy.md` for the rationale and `.claude/skills/module-boundaries/SKILL.md` for the full matrix.

---

## Architecture

- **DI:** Hilt — one `@Module` per module (`NetworkModule`, `NetworkBindingsModule`, `DatabaseModule`, `DatabaseBindingsModule`, `DataModule`, `ClockModule`). Repository implementations live in `:core:data`; `:app` contains only Hilt entry points.
- **Navigation:** Single Activity (`MainActivity`) with `NavHostFragment`. Two destinations: `ListingFragment` (start) → `DetailFragment`. Argument: `propertyId: String`.
- **Listing UI:** XML + ViewBinding + `ListAdapter` + `DiffUtil`. ViewModel exposes LiveData.
- **Detail UI:** `ComposeView` inside a Fragment. ViewModel exposes StateFlow.
- **Persistence:** Room. Favorites table: `favorite(property_id TEXT PRIMARY KEY, favorited_at INTEGER NOT NULL)`.
- **Network:** Retrofit + OkHttp + kotlinx.serialization.

## ADRs

| # | Title | Status |
|---|---|---|
| [0001](docs/adr/0001-modularization-strategy.md) | Modularization strategy — 7 modules, `:core:data` as aggregation layer | Accepted |
| [0002](docs/adr/0002-static-detail-endpoint-handling.md) | Static detail endpoint — `adid=1` always, enrichment guard + in-memory cache | Accepted |
| [0003](docs/adr/0003-material-components.md) | Material Components, `:core:design` module, M3 design system | Accepted |
| [0005](docs/adr/0005-xml-compose-interoperability.md) | XML and Compose interoperability (`CoordinatorLayout` + `ComposeView`) | Accepted |
| [0006](docs/adr/0006-favorite-timestamp-semantics.md) | Favorite timestamp semantics — upsert on re-favorite | Accepted |
| [0009](docs/adr/0009-characteristics-flowrow-chips.md) | Characteristics section as `FlowRow` of `SuggestionChip` | Accepted |
| [0010](docs/adr/0010-static-map-placeholder.md) | Static map placeholder for location section | Accepted |

## Test coverage

| Module | Test class | Tests | What's covered |
|---|---|---|---|
| `:core:data` | `PropertiesRepositoryImplTest` | 13 | Cache hits/misses, enrichment guard (ADR-0002), Mutex stampede protection, failed-refresh resilience |
| `:core:network` | `PropertyDtoMapperTest` | 6 | API contract quirks: `priceInfo` vs top-level `price`, absent `parkingSpace`, optional feature flags |
| `:feature:list` | `ListingViewModelTest` | 11 | Filter states (SALE/RENT/FAVORITES/ALL), retry flow, favorites count, empty vs content state |
| `:feature:list` | `PropertyMapperTest` | 8 | es-ES price formatting, thumbnail fallback chain, operation label, favorite date label |
| `:feature:detail` | `DetailViewModelTest` | 6 | Loading → Content → Error lifecycle, favorite toggle, retry |
| `:feature:detail` | `DetailMapperTest` | 11 | Price format, characteristics list presence/absence, energy letter→index, floor fallback, costs label |
