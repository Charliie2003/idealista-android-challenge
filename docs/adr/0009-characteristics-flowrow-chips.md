# ADR-0009 — Characteristics Section as FlowRow of AssistChips

**Status:** Accepted  
**Date:** 2026-07-30  
**Deciders:** carlos.hinojosa

## Context

The HTML prototype (`detalle_definitivo.html`) renders property characteristics as a 2-column CSS grid with `.cell` items. A user requirement for the Compose implementation explicitly asked for a `FlowRow` of `AssistChip` components instead.

## Decision

`CharacteristicsFlow` uses `@OptIn(ExperimentalLayoutApi::class) FlowRow` with `AssistChip` for each characteristic (leading icon + label + value). Chips are set to `enabled = false` (informational, non-interactive). `horizontalArrangement = Arrangement.spacedBy(8.dp)`, `verticalArrangement = Arrangement.spacedBy(8.dp)`.

## Alternatives Considered

- **2-column `LazyVerticalGrid`**: matches the HTML exactly but `LazyVerticalGrid` inside a `LazyColumn` requires a fixed height workaround (measuring all items in advance). `FlowRow` in a regular `Column` inside `verticalScroll` avoids nested scrolling constraints entirely.
- **Fixed 2-column `Row` pairs**: less readable code, brittle on narrow screens and on large font scales.

## Consequences

- **Positive:** Chips reflow naturally on narrow devices and with large font scales (accessibility).
- **Positive:** `AssistChip` uses the Material3 shape system and aligns with the existing design system.
- **Negative:** `@ExperimentalLayoutApi` opt-in is required at the file level in `CharacteristicsFlow.kt`. As of Compose BOM 2024.09.00 (`foundation-layout 1.7.x`), `FlowRow` is functionally stable; the annotation is cosmetic and will be removed in a future BOM.
- **Design deviation:** The HTML uses a 2-column grid; the Android implementation uses a wrapping chip row. This deviation is intentional and documented here.
