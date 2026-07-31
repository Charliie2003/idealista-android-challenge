# ADR-0005 — XML and Compose Interoperability

**Status:** Accepted  
**Date:** 2026-07-30  
**Deciders:** carlos.hinojosa

## Context

The detail screen is the first screen in the project to mix an XML Fragment host with Jetpack Compose content. The challenge requirements explicitly mandate demonstrating both XML and Compose in the same project.

## Decision

`DetailFragment` inflates an XML layout (`fragment_detail.xml`) containing a `CoordinatorLayout` + `AppBarLayout` + transparent `MaterialToolbar` + `ComposeView`. The toolbar handles status bar coverage and system back navigation via `setNavigationOnClickListener`. All property content (gallery, property details, characteristics, map placeholder) is rendered inside the `ComposeView` using `IdealistaTheme`.

Lifecycle-aware state collection uses `collectAsStateWithLifecycle()` (from `lifecycle-runtime-compose`). The composition is disposed via `ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed` to prevent leaks when the Fragment view is destroyed while the Fragment itself is retained in the back stack.

## Alternatives Considered

- **Full Compose with `ComposeActivity`**: rejected — the challenge explicitly requires Fragment + XML for the listing screen, so mixing paradigms is a first-class requirement.
- **`AbstractComposeFragment`**: rejected — unnecessary abstraction for a single screen.
- **Edge-to-edge with `WindowCompat.setDecorFitsSystemWindows(false)`**: rejected — touching `MainActivity` risked breaking the listing screen's safe area handling; the transparent toolbar approach achieves the same result with zero cross-screen risk.

## Consequences

- **Positive:** Demonstrates both paradigms clearly and side-by-side for the reviewer.
- **Positive:** `ComposeView` inside Fragment is the recommended AndroidX bridge pattern and requires no experimental APIs.
- **Negative:** The back navigation icon is duplicated — the XML toolbar icon and the Compose `GalleryTopBar` overlay both call `navigateUp()`. This is intentional: the overlay is the primary UX affordance; the toolbar icon is a structural fallback for accessibility.
- **Follow-up:** If future screens are all Compose, migrate to `@Composable` destinations inside a `NavHost` directly. `DetailFragment` can be the last Fragment in the codebase.
