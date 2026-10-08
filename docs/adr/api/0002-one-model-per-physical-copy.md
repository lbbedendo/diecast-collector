# ADR-0002: One Model per physical copy, with no quantity field

- **Status:** Accepted
- **Date:** 2026-10-08
- **Scope:** `api/` (domain model; the app's forms and list views follow it)

## Context

Collectors often own more than one copy of the same diecast: one kept sealed and one opened, or
a spare to trade. A Model carries per-copy facts: Packaging, Condition, purchase price, date and
source, and a photo. Two copies of the same diecast often differ in these, for example a sealed,
mint copy next to a loose copy with a chipped wing. See `CONTEXT.md` for the terms.

## Decision

A Model is **one physical copy**. Owning the same diecast twice means two Models, even when the
two copies are identical in every field. There is no quantity field.

### Considered options

- **Group identical copies, with a quantity.** This was briefly chosen, then dropped. A quantity
  only fits when every copy shares the same Packaging, Condition and purchase details. That makes
  it a special case that still needs separate Models whenever copies differ.
- **One Model per diecast, describing one representative copy.** Rejected: the record can't show
  that one copy is damaged or cost a different price.
- **One Model per diecast, with a list of copies under it.** Rejected: it adds a second level
  ("copy") to the domain and the schema just to group entries the app can already group when
  displaying them.

## Consequences

- "How many of X do I own?" is answered by counting Models (same name, Brand and Series), not
  by reading a field. List views that want to show "×2" have to group Models themselves.
- Adding a second identical copy means creating a second Model. A "duplicate this Model" action
  in the app would make that quicker.
- Don't add a `quantity` column to fix the counting. Doing so reopens this decision.
