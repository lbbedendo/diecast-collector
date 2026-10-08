-- Condition used to mix two questions: whether a Model is still in its packaging (SEALED, LOOSE)
-- and what state the diecast is in (MINT..POOR). Packaging is now its own column; the two
-- packaging values move over and leave condition unknown, since they never said anything about
-- the diecast's state.

alter table model add column packaging varchar(20);

update model
set packaging = condition,
    condition = null
where condition in ('SEALED', 'LOOSE');
