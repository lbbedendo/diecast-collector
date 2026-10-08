-- A Series belongs to exactly one Brand, and a Model's Brand is its Series' Brand (see CONTEXT.md
-- and ADR-0003), so brand moves from model to series. Existing Series rows have no Brand to
-- infer reliably, and only local dev data exists, so Series data is wiped rather than backfilled.

update model set series_id = null;
delete from series;

alter table series add column brand_id bigint not null references brand(id);

-- Brand + name + year identifies a Series; a missing year counts as a value, so two yearless
-- Series with the same Brand and name are duplicates too.
alter table series add constraint uk_series_brand_name_year unique nulls not distinct (brand_id, name, year);

alter table model drop column brand_id;
