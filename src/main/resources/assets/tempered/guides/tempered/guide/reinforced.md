---
navigation:
  title: Reinforced
  icon: tempered:reinforced_aspect
  parent: index.md
  position: 2
item_ids:
  - tempered:reinforced_aspect
---
# Reinforced

<ItemImage id="tempered:reinforced_aspect" float="left" />

Increase **maximum durability** on tools, weapons, armor, shields or any other
item with durability. [Find an Aspect](loot.md), then
[apply it at a smithing table](smithing.md).

## Active levels

<tempered:Settings topic="reinforced" />

## How durability is calculated

The first application records the item's durability before Reinforced.
Later Aspect levels use that same baseline, so bonuses do not compound.
A listed bonus is the **total at that level**, not an extra bonus per application.

A [material upgrade](maintenance.md), such as diamond to netherite, keeps the
saved level and uses the new equipment's durability for the bonus. Applying
another Aspect afterward uses that new baseline.

## When balance changes

Disabling Reinforced or lowering its cap keeps your saved levels. The active
bonus uses the current cap; re-enabling the effect or raising the cap makes
retained history available again.

Carried equipment updates its durability while keeping the remaining
percentage. Stored equipment updates when brought into use. Older equipment
with an uncertain original durability keeps its recorded baseline until a
supported material upgrade establishes a new one.

See [server configuration](configuration.md) to tune these rules.
