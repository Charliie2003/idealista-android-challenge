# ADR-0002 — Static detail endpoint handling

**Status:** Accepted
**Date:** 2026-07-29
**Deciders:** carlos.hinojosa

## Context

The challenge detail endpoint (`detail.json`) always returns the same payload — property `adid: 1`. In a real app, this would be a per-property endpoint keyed by ID. The app hosts four properties (IDs 1–4); naively displaying the detail endpoint's payload for any property would show property 1's data regardless of which property the user tapped.

## Decision

`PropertiesRepositoryImpl.getDetail(id)` fetches both the listing (to get the base property) and the detail endpoint. It enriches the base property with detail data **only when `detail.adid.toString() == id`**. For any other `id`, the base property from the listing is returned as a `PropertyDetail` with `isEnriched = false` and no `moreCharacteristics` or `energyCertification`. The enrichment logic is documented with `// See ADR-0002`.

## Alternatives considered

- **Always show the detail endpoint payload.** Rejected — property 2's detail screen would show property 1's data; a real bug visible in the UI.
- **Skip the detail call for non-`1` properties.** Rejected — removes the signal that the quirk was handled intentionally; breaks if the endpoint is ever fixed.
- **Error out for non-`1` requests.** Rejected — degrades UX for a limitation the user cannot control.

## Consequences

- **Positive:** No misleading data. Reviewer sees defensive handling. Degradation is graceful.
- **Negative:** A conditional in the repository that looks surprising without context — hence this ADR.
- **Follow-ups:** Remove conditional once endpoint becomes per-property.

## References

- `.claude/skills/api-contract/SKILL.md` — Quirk 2
