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

## Module diagram

```
                    ┌─────────────────────────────────────────┐
                    │                  :app                   │
                    │  Application class · MainActivity        │
                    │  NavGraph · Hilt modules · Repo impls   │
                    └────┬──────────┬───────┬──────────┬──────┘
                         │          │       │          │
              ┌──────────▼──┐  ┌────▼───┐  │    ┌─────▼──────┐
              │ :core:network│  │:core:  │  │    │:core:      │
              │  Retrofit    │  │database│  │    │model       │
              │  DTOs        │  │Room    │  │    │(pure Kotlin)│
              │  RemoteSrc   │  │Entities│  │    │Interfaces  │
              └──────┬───────┘  └───┬────┘  │    │UseCases    │
                     │              │       │    └─────────────┘
                     └──────────────┴───────┘          ▲
                                                        │
                    ┌───────────────────────────────────┤
                    │                                   │
              ┌─────▼──────┐                    ┌───────▼─────┐
              │:feature:   │                    │:feature:    │
              │list        │                    │detail       │
              │XML + LvData│                    │Compose+Flow │
              └────────────┘                    └─────────────┘
```

**Dependency rules:**
- Feature modules depend **only** on `:core:domain`. They are completely unaware of Retrofit or Room.
- `:core:domain` is pure Kotlin (`java-library` plugin). It imports nothing from `androidx.*`.
- `:app` is the composition root — it depends on everything and wires DI.

See `docs/adr/0001-modularization-strategy.md` for the rationale and `.claude/skills/module-boundaries/SKILL.md` for the full matrix.

---

## Architecture

- **DI:** Hilt — one `@Module` per concern (`NetworkModule`, `DatabaseModule`, `RepositoryModule`, `DispatchersModule`).
- **Navigation:** Single Activity (`MainActivity`) with `NavHostFragment`. Two destinations: `ListingFragment` (start) → `DetailFragment`. Argument: `propertyId: String`.
- **Listing UI:** XML + ViewBinding + `ListAdapter` + `DiffUtil`. ViewModel exposes LiveData.
- **Detail UI:** `ComposeView` inside a Fragment. ViewModel exposes StateFlow.
- **Persistence:** Room. Favorites table: `favorite(property_id TEXT PRIMARY KEY, favorited_at INTEGER NOT NULL)`.
- **Network:** Retrofit + OkHttp + kotlinx.serialization.

## ADRs

| # | Title |
|---|---|
| [0001](docs/adr/0001-modularization-strategy.md) | Modularization strategy |
