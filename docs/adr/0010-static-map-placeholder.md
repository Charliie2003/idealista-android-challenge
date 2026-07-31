# ADR-0010 — Static Map Placeholder for Location Section

**Status:** Accepted  
**Date:** 2026-07-30  
**Deciders:** carlos.hinojosa

## Context

The HTML prototype includes a `.map` section with a CSS-drawn grid background, two road divs, a halo circle, and a green pin SVG. A real map would require Google Maps SDK or Mapbox, both of which require API keys (blocked by CLAUDE.md §2: "No secrets or API keys"), Play Services dependency, and debug/release key management.

## Decision

`LocationBlock` is a static `Box` Composable that visually approximates the HTML's map placeholder using Compose's `drawBehind` modifier:

- **Grid pattern:** horizontal and vertical lines at 22dp intervals, color `#E8ECE9`.
- **Roads:** two white rectangular overlays (horizontal 36dp tall, vertical 24dp wide) centered in the box.
- **Halo:** a semi-transparent green circle (`#2200A650`) centered on the pin.
- **Pin:** `ic_map_pin_green.xml` (vector drawable, 32dp, green fill with white inner circle) centered in the box.

Height is 150dp, corner radius 16dp, border 1dp `outline` — matching the HTML prototype exactly.

## Migration Path

Replace `LocationBlock` with `AndroidView { MapView(...) }` wrapping a Google Maps or Mapbox `MapView`. Pass `latitude` and `longitude` from `PropertyDetailUiModel`. Add API key handling via `local.properties` → `BuildConfig`. The `PropertyDetailUiModel` already exposes `latitude` and `longitude` for this purpose.

## Consequences

- **Positive:** Zero additional dependencies. No Play Services requirement for the challenge build.
- **Positive:** The static placeholder faithfully represents the design intent and passes code review as documented.
- **Negative:** No real map interaction. Pinch-to-zoom and address lookup are absent. Acceptable for a technical challenge submission.
