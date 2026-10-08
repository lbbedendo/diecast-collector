-- The "collection" table always held a Series (see CONTEXT.md); "Collection" now means everything
-- a Collector owns, so the table is renamed. model.series_name duplicated the same concept as
-- free text and is folded into the series reference before being dropped.

alter table collection rename to series;
alter sequence collection_id_seq rename to series_id_seq;
alter index collection_pkey rename to series_pkey;

alter table model rename column collection_id to series_id;
alter index idx_model_collection_id rename to idx_model_series_id;
alter table model rename constraint model_collection_id_fkey to model_series_id_fkey;

-- Models with a free-text series name but no series reference get a Series row (name only, no
-- year) and are linked to it.
insert into series (name)
select distinct m.series_name
from model m
where m.series_id is null
  and nullif(trim(m.series_name), '') is not null
  and not exists (select 1 from series s where s.name = m.series_name and s.year is null);

update model m
set series_id = (select min(s.id) from series s where s.name = m.series_name and s.year is null)
where m.series_id is null
  and nullif(trim(m.series_name), '') is not null;

alter table model drop column series_name;
