# AI Usage Log

This file documents where AI made substantive contributions to the project, per the Definition of Done in `CLAUDE.md §10`.

---

## IAC-01 / IAC-02 / IAC-03 — Bootstrap, Multi-module, DI & Navigation

**Date:** 2026-07-28
**Model:** Claude Sonnet 4.6 (claude-sonnet-4-6) via Claude Code CLI

**Contributions:**
- Designed the full multi-module Gradle structure from the Android Studio skeleton.
- Authored `libs.versions.toml`, all six `build.gradle.kts` files, and `settings.gradle.kts`.
- Designed and implemented the cross-module navigation pattern (`ListingNavigator` interface) to avoid a circular dependency between `:feature:list` and `:app`.
- Authored all Hilt module skeletons, stub Fragments, and ViewModels.
- Authored `docs/adr/0001-modularization-strategy.md`.
- Authored the top-level `README.md` and per-module `README.md` files.

**Decisions escalated to the human:**
- Package name (`com.carloshinojosa.idealistachallenge`).
- SDK versions (compileSdk 35, minSdk 24, targetSdk 35).
- JVM target (Java 17).
- Toolchain versions (AGP 9.1.1 / Kotlin 2.2.10 / Gradle 9.3.1 — kept from Android Studio scaffold).
- KSP over KAPT.
- Detekt deferred to IAC-44.

**What AI did NOT decide:**
- Architecture rules (defined in `CLAUDE.md` by the human).
- Module structure (defined in `CLAUDE.md §3` by the human).
- Tech stack choices (defined in `CLAUDE.md §4` by the human).

---

## IAC-30 / IAC-31 / IAC-32 / IAC-33 — Property Detail Screen (Epic 4)

**Date:** 2026-07-31
**Model:** Claude Sonnet 4.6 (claude-sonnet-4-6) via Claude Code CLI

**Contributions:**

*IAC-30 — Navigation & ViewModel wiring*
- Modified `IsFavoriteUseCase` return type from `Flow<Boolean>` to `Flow<Favorite?>` to expose the favorite timestamp for date display.
- Authored `DetailUiState`, `PropertyDetailUiModel`, `ImageUiModel`, `CharacteristicUiModel`, `EnergyUiModel` UI models.
- Authored `DetailMapper` (`@Singleton` + `@ApplicationContext`) mapping `PropertyDetail` → `PropertyDetailUiModel`, including price formatting (es-ES `NumberFormat`), floor string, energy letter→index, and `formatFavoriteDate` using `java.time` via core library desugaring.
- Rewrote `DetailViewModel` with SavedStateHandle injection, `flatMapLatest + combine` pattern for retry + live favorites, and `stateIn(WhileSubscribed(5_000))`.
- Added `isCoreLibraryDesugaringEnabled = true` and `coreLibraryDesugaring(libs.desugar.jdk.libs)` to `feature/detail/build.gradle.kts` to allow `java.time` usage without `@RequiresApi`.
- Added `lifecycle-runtime-compose` and `coil-compose` library aliases to `gradle/libs.versions.toml`.

*IAC-31 — Detail UI (Compose)*
- Rewrote `fragment_detail.xml` to `CoordinatorLayout` + `AppBarLayout` + transparent `MaterialToolbar` + `ComposeView` with `appbar_scrolling_view_behavior`.
- Rewrote `DetailFragment` using `ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed` and `collectAsStateWithLifecycle()`.
- Authored 12 Composables: `DetailScreen`, `PropertyGallery` (HorizontalPager + dots + room tag), `GalleryTopBar` (back/share/favorite overlay buttons), `PropertyHeader` (chips + price + location), `HighlightsRow` (4-cell bordered row with VerticalDivider), `DescriptionBlock` (5-line clamp + animateContentSize expand), `CharacteristicsFlow` (FlowRow of AssistChip), `EnergyCertificationCard`, `CommunityCostsCard`, `LocationBlock` (static canvas map placeholder), `LoadingState`, `ErrorState`.
- Created 13 drawable vector assets (arrow back, share, heart outline/filled, pin, ruler, bed, bath, floor, elevator, house, green map pin, property placeholder).
- Added 35 strings to `feature/detail/res/values/strings.xml`.

*IAC-32 — Favorite sync*
- Covered by IAC-30 ViewModel wiring. Room is the single source of truth; no additional files needed.

*IAC-33 — ADRs*
- Authored `docs/adr/0005-xml-compose-interoperability.md` — XML Fragment + ComposeView bridge, ViewCompositionStrategy, back nav mirroring.
- Authored `docs/adr/0009-characteristics-flowrow-chips.md` — FlowRow of AssistChips vs 2-column grid, nested scrolling justification.
- Authored `docs/adr/0010-static-map-placeholder.md` — static canvas map vs Google Maps SDK (API keys blocked), migration path.

**Decisions escalated to the human:**
- None — all decisions were within the scope of the implementation plan authored by the human.

**What AI did NOT decide:**
- The implementation plan itself (authored by the human as the Epic 4 specification document).
- Module structure and dependency rules (defined in `CLAUDE.md §3`).
- Visual design (traced from `detalle_definitivo.html`, the human-provided source of truth).
