# ADR-0006 — Favorite timestamp semantics (re-favoriting updates the timestamp)

**Status:** Accepted
**Date:** 2026-07-29
**Deciders:** carlos.hinojosa

## Context

A user can favorite a property, then unfavorite it, then favorite it again. The `favorites` table stores a `favoritedAt` timestamp. Two behaviors are possible on re-favoriting: (a) keep the original timestamp (insert-or-no-op), or (b) update the timestamp to the new moment (upsert). The choice affects how "favorited since" is displayed and whether the favorites list sorts by recency meaningfully.

## Decision

Re-favoriting updates the timestamp to the current moment. `FavoritesDao` uses `@Upsert`, and `FavoritesRepositoryImpl.toggle()` always calls `localDataSource.upsert(id, now)` on the add branch. The timestamp stored is always the *most recent* time the user favorited the property.

## Alternatives considered

- **Insert-or-no-op (keep original timestamp).** Rejected — "favorited since" becomes misleading if the user has unfavorited and re-favorited since then.
- **Throw an error on duplicate insert.** Rejected — no user error should result from tapping a heart icon twice.

## Consequences

- **Positive:** "Favorited since" always reflects the user's actual last intent. No insert conflict to handle.
- **Negative:** A user who favorites on day 1, unfavorites, and re-favorites on day 30 loses the day-1 timestamp.
- **Follow-ups:** None required at challenge scale.

## References

- `FavoritesDao.upsert()` — `@Upsert` annotation (Room 2.5+)
- `ToggleFavoriteUseCase` — passes `Instant.now(clock)` at invocation time
