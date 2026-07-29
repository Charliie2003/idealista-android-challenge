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
