---
name: adr-writing
description: Read this before writing or referencing an Architecture Decision Record. Contains the format, location, and the initial ADR backlog for this project.
---

# ADR Writing

An **Architecture Decision Record (ADR)** documents a significant technical decision, why it was made, and what alternatives were rejected. They are the reason a future engineer (or the reviewer) doesn't have to ask "why is it done this way?"

## Location

`docs/adr/NNNN-kebab-case-title.md` — sequentially numbered, four digits.

Examples: `docs/adr/0001-modularization-strategy.md`, `docs/adr/0002-static-detail-endpoint-handling.md`.

## When to write one

Write an ADR when:

- Introducing or removing a **library** (Retrofit, MockK, Turbine, Coil, etc.).
- Choosing between two viable **architectural approaches** (StateFlow vs LiveData, one module vs many, Paging 3 vs manual).
- Handling a **contract quirk** (static detail endpoint, price ambiguity).
- Making a **product behavior decision** (re-favoriting updates timestamp).
- Deliberately **not doing** something the reviewer might expect (no Paging 3, no offline-first, no dark mode support).

Do NOT write an ADR for:

- Naming a variable.
- Adding a helper function.
- Fixing a bug.
- Anything reversible in a single small commit.

## Format

```markdown
# ADR-NNNN — <Short imperative title>

**Status:** Accepted | Superseded by ADR-XXXX | Rejected
**Date:** YYYY-MM-DD
**Deciders:** <name(s)>

## Context

What is the situation? What forced this decision? What constraints apply? Keep to 3–8 sentences — anyone reading should understand the problem without extra research.

## Decision

The chosen approach, stated as a decision (imperative or declarative). Not a discussion.

## Alternatives considered

- **<Option A>** — <one sentence>. Rejected because <reason>.
- **<Option B>** — <one sentence>. Rejected because <reason>.

At least one alternative must be listed. If there wasn't one, the decision was not real.

## Consequences

- **Positive:** what we gain.
- **Negative:** what we accept losing.
- **Follow-ups:** what we should do later as a result (link to Jira story if any).

## References

Links to docs, similar projects, or related ADRs.
```

## Length

**One page maximum.** If the ADR needs more, either the decision is too big (split it) or it contains too much implementation detail (that belongs in code comments or KDoc).

## Initial ADR backlog for this project

Write these as you go — do not write them all upfront.

| # | Title | Trigger |
|---|---|---|
| 0001 | Modularization strategy | When creating the modules |
| 0002 | Static detail endpoint handling | When implementing `ObservePropertyDetailUseCase` |
| 0003 | StateFlow over LiveData (with XML exception) | When implementing the first ViewModel |
| 0004 | No Paging 3 | When implementing the listing (deliberate omission) |
| 0005 | XML and Compose interoperability approach | When implementing the detail Fragment |
| 0006 | Favorite timestamp semantics (re-favoriting updates) | When implementing `ToggleFavoriteUseCase` |
| 0007 | Repository implementations live in `:app` (not `:data`) | When implementing the first repository |
| 0008 | AI collaboration harness | When finalizing `docs/ai-usage.md` |

## Example ADR (template you can adapt)

```markdown
# ADR-0002 — Static detail endpoint handling

**Status:** Accepted
**Date:** 2025-11-04
**Deciders:** <your name>

## Context

The challenge detail endpoint (`detail.json`) is documented as always returning the same payload — property `1`. In a real app, this would be a per-property endpoint. The app must not silently misrepresent property `2`'s detail as property `1`'s data.

## Decision

`ObservePropertyDetailUseCase` combines two sources: the property from the listing repository (indexed by `propertyId`) and the detail from the detail endpoint. The detail enriches the base property **only when `detail.adid.toString() == requestedPropertyId`**. Otherwise, the base property from the listing is returned as `PropertyDetail`.

## Alternatives considered

- **Always show the detail endpoint payload.** Rejected — this would show property `1`'s content for property `2`, which is a real bug in an interview context.
- **Do not call the detail endpoint at all for non-`1` properties.** Rejected — wastes the signal that we handled the endpoint intentionally; and if the endpoint is later fixed, we would need to remove a conditional.
- **Load only from the detail endpoint and error out for non-`1` requests.** Rejected — degrades UX for a challenge whose data limitation is not the user's problem.

## Consequences

- **Positive:** No misleading data shown to users. Reviewer sees defensive handling of a real backend quirk. Behavior degrades gracefully.
- **Negative:** A small conditional in the use case that will look strange until the reader reads this ADR.
- **Follow-ups:** Remove the conditional if/when the detail endpoint becomes per-property.

## References

- `.claude/skills/api-contract/SKILL.md` — Quirk 2
```

## Rules

- **Never edit an accepted ADR.** Supersede it with a new ADR that references the old one (`Status: Superseded by ADR-XXXX`).
- **ADRs are committed with the change** they document — same PR, same commit if possible.
- **Link ADRs from code** at the site of the decision (`// See ADR-0002`).
- **Reference ADRs from the README** in the architecture section.
