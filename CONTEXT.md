# Diecast Collector

A personal catalogue for diecast collectors to track the scale models they own, across any diecast
brand.

## Language

### Ownership

**Collector**:
A person who uses the app to catalogue the diecast models they own.
_Avoid_: User, owner, account

**Collection**:
Everything one Collector owns: the full set of their Models.
_Avoid_: Garage, inventory

**Model**:
One physical diecast model a Collector owns. Owning the same diecast twice means two Models,
each with its own condition and purchase details. A Model's Brand is the Brand of its Series; a
Model with no Series has no known Brand.
_Avoid_: Piece, item, car, copy, casting

**Vehicle year**:
The year of the real-world vehicle a Model depicts (e.g. 2004 for a Ferrari F2004). Absent when
the vehicle is fictional. Not the release year, which belongs to the Series.
_Avoid_: Model year, year

### State of a Model

**Packaging**:
Whether a Model is still in its original packaging: sealed, opened (packaging kept), or loose
(no packaging).
_Avoid_: Carded, boxed, in-package

**Condition**:
The physical state of the diecast itself, independent of its Packaging: mint, good, fair, or poor.
_Avoid_: Grade, quality

### Who made it

**Brand**:
The company that produces the diecast model (e.g. Hot Wheels, Matchbox, California Collectibles).
_Avoid_: Manufacturer, maker

**Automaker**:
The real-world company that built the vehicle a Model depicts (e.g. Ferrari, Honda). A Model of
a fictional vehicle has none.
_Avoid_: Manufacturer, make

### How it was released

**Series**:
A named group of models one Brand releases together, usually in a given year (e.g. Hot Wheels
"HW Starting Grid" 2026). The same name in another year, or from another Brand, is a different
Series. A Series' year, when known, is the release year of its Models.
_Avoid_: Collection, line, wave, set

**Series number**:
A model's position within its Series as printed by the Brand, kept as written (e.g. "10/10").
Optional: not every Brand numbers its Series.
_Avoid_: Catalog number, collector number

**Chase**:
A rare variant a Brand mixes into a Series in small numbers (e.g. a Hot Wheels Treasure Hunt, a
Matchbox or CK chase). A Model either is a Chase or isn't; brand-specific tiers are not
distinguished. Premium releases are not Chases; they belong to their own Series.
_Avoid_: Treasure Hunt, rare, premium, variant
