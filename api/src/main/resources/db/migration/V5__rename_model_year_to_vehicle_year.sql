-- "model year" didn't say whose year it was. It's the year of the real vehicle the Model depicts
-- (see Vehicle year in CONTEXT.md); the release year comes from the Model's Series.

alter table model rename column model_year to vehicle_year;
