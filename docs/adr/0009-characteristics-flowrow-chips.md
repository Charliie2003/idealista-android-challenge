# ADR-0009 — Characteristics Section as FlowRow of SuggestionChips

**Status:** Accepted  
**Date:** 2026-07-30  
**Deciders:** carlos.hinojosa

## Context

The HTML prototype (`detalle_definitivo.html`) renders property characteristics as a 2-column CSS grid with `.cell` items. A user requirement for the Compose implementation explicitly asked for a `FlowRow` of chip components instead.

`AssistChip(enabled = false)` was initially considered but is prohibited by `CLAUDE.md §10`:
a disabled `AssistChip` renders at reduced opacity implying "feature unavailable", which
misrepresents read-only informational content. `SuggestionChip` is the correct Material 3
chip for non-interactive labels.

## Decision

`CharacteristicsFlow` uses `@OptIn(ExperimentalLayoutApi::class) FlowRow` with `SuggestionChip`
for each characteristic (icon + label + value text). `SuggestionChip` is always non-interactive
— no `enabled` flag needed. Note: `SuggestionChip` uses the `icon` parameter, not `leadingIcon`.
`horizontalArrangement = Arrangement.spacedBy(8.dp)`, `verticalArrangement = Arrangement.spacedBy(8.dp)`.

## Alternatives Considered

- **`AssistChip(enabled = false)`**: Rejected — `CLAUDE.md §10` prohibits it for informational
  content because the disabled state implies "unavailable feature", not "read-only label".
  Semantics matter for accessibility (TalkBack announces disabled state).
- **2-column `LazyVerticalGrid`**: matches the HTML exactly but `LazyVerticalGrid` inside a
  `LazyColumn` requires a fixed height workaround (measuring all items in advance). `FlowRow`
  in a regular `Column` inside `verticalScroll` avoids nested scrolling constraints entirely.
- **Fixed 2-column `Row` pairs**: less readable code, brittle on narrow screens and on large
  font scales.

## Consequences

- **Positive:** Chips reflow naturally on narrow devices and with large font scales (accessibility).
- **Positive:** `SuggestionChip` is the semantically correct Material 3 chip for non-interactive
  informational labels — correct opacity, correct role in the accessibility tree.
- **Negative:** `@ExperimentalLayoutApi` opt-in is required at the file level in
  `CharacteristicsFlow.kt`. As of Compose BOM 2024.09.00 (`foundation-layout 1.7.x`),
  `FlowRow` is functionally stable; the annotation is cosmetic and will be removed in a future BOM.
- **Design deviation:** The HTML uses a 2-column grid; the Android implementation uses a wrapping
  chip row. This deviation is intentional and documented here.
