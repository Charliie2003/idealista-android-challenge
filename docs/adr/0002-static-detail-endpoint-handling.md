# ADR-0002 — Static detail endpoint handling

**Status:** Accepted  
**Date:** 2026-07-29 (updated 2026-07-31)  
**Deciders:** carlos.hinojosa

## Context

The challenge detail endpoint (`detail.json`) always returns the same payload — property `adid: 1`. In a real app, this would be a per-property endpoint keyed by ID. The app hosts four properties (IDs 1–4); naively displaying the detail endpoint's payload for any property would show property 1's data regardless of which property the user tapped.

Additionally, `getDetail(id)` originally fetched `list.json` on every detail screen open, even though the listing was already downloaded before the user navigated there. Because `PropertiesRepositoryImpl` is `@Singleton`, a session-scoped in-memory cache eliminates this redundant download without requiring persistence.

## Decision

### Enrichment guard
`PropertiesRepositoryImpl.getDetail(id)` fetches the base property and the detail endpoint. It enriches the base property with detail data **only when `detail.adid.toString() == id`**. For any other `id`, the base property is returned as a `PropertyDetail` with `isEnriched = false` and no `moreCharacteristics` or `energyCertification`. This is documented with `// See ADR-0002`.

### Session-scoped in-memory cache
`InMemoryPropertiesCache` stores the last successful listing response, keyed by property ID. The cache is backed by a `@Volatile` immutable map; `replace` is an atomic reference swap so there is no window where a reader observes a partial state.

`getDetail` uses a double-checked locking pattern (`Mutex.withLock` + re-check after acquiring) to guarantee that concurrent cache misses perform exactly one network call. The lock is released before calling `fetchPropertyDetailEnrichment`, which needs no exclusion.

A failed `observeProperties` refresh does **not** clear the cache — the last good data remains available for `getDetail` calls.

Deep links or process recreation hit the network because the cache is in-memory only and does not survive process death. This is acceptable for a challenge scope.

## Alternatives considered

- **Always show the detail endpoint payload.** Rejected — property 2's detail screen would show property 1's data; a real bug visible in the UI.
- **Skip the detail call for non-`1` properties.** Rejected — removes the signal that the quirk was handled intentionally; breaks if the endpoint is ever fixed.
- **Error out for non-`1` requests.** Rejected — degrades UX for a limitation the user cannot control.
- **Pass the full Property via Navigation arguments.** Rejected — Parcelable in navigation arguments is a coupling between list and detail; it bypasses the domain layer and breaks if the detail screen is entry-pointed from a deep link.
- **Always download the listing in `getDetail`.** Rejected — doubles the network traffic on the happy path; the listing never changes within a session.
- **Add Room persistence for all properties.** Rejected — offline support is out of scope for this challenge; the added complexity is not justified.
- **Omit `detail.json` for IDs ≠ 1.** Rejected — the static endpoint should be consumed defensively; a future fix to the endpoint would silently start working.

## Consequences

- **Positive:** No misleading data. No redundant listing request on the normal list → detail path. Reviewer sees defensive handling of the quirk and a concurrency-safe cache.
- **Negative:** The double-check locking pattern adds a small amount of cognitive overhead, documented in code and here.
- **Follow-ups:** Remove the enrichment `id == "1"` guard once the endpoint becomes per-property.

## References

- `.claude/skills/api-contract/SKILL.md` — Quirk 2
