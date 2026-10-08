# ADR-0003: A Model's Brand comes from its Series

- **Status:** Accepted
- **Date:** 2026-10-08
- **Scope:** `api/` (domain model; the app's forms and list views follow it)

## Context

A Series belongs to exactly one Brand (see `CONTEXT.md`). Before this decision, a Model stored its
own Brand **and** a Series. With Series now scoped to a Brand, the two could disagree: a Model
could say Matchbox while pointing at a Hot Wheels Series.

## Decision

Remove Brand from the Model. A Model's Brand is the Brand of its Series. Series stays optional, so
**a Model with no Series has no known Brand.**

A Series is unique by Brand + name + year. Year is optional, and a missing year counts as a value
for uniqueness (`unique nulls not distinct`).

### Considered options

- **Keep Brand on the Model and reject mismatches (400).** Rejected: it stores the same fact
  twice and pushes the job of keeping them in sync onto every client.
- **Keep Brand on the Model, copied from the Series when one is set.** Rejected: the column
  would still be a second copy that can drift if a Series' Brand is later edited.
- **Make Series required so every Model has a Brand.** Rejected: collectors sometimes own pieces
  they can't place in a Series (an unidentified loose diecast, a standalone release). Losing
  their Brand was judged better than forcing a made-up Series.

## Consequences

- Filtering or grouping Models by Brand means going through `series.brand`. Models with no Series
  are left out of any per-Brand view.
- Changing a Series' Brand changes the Brand of every Model in it. That's intended: it fixes a
  wrong Brand in one place.
- Existing Series data was wiped by `V3__scope_series_to_brand.sql` instead of backfilled,
  because only local dev data existed and the old Series had no Brand to infer from.
- Don't add a `brand_id` back to `model` as a shortcut for Series-less Models; doing so reopens
  this decision.
