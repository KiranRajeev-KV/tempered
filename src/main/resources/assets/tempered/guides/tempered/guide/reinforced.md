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

The Reinforced Aspect increases **maximum durability**. It accepts any item
with durability, including tools, weapons, armor, shields and compatible
modded equipment. [Find it as loot](loot.md), then [apply it at smithing](smithing.md).

## How the bonus works

The first application records the item's original maximum durability. Every
later level uses that same baseline, so repeated upgrades do not compound the
bonus. Listed bonuses are **totals at each level**, not extra bonuses per application.

Reinforced does not fully repair the item. When server balance changes, carried
equipment is reconciled while preserving its remaining durability fraction.
Stored equipment is reconciled when brought into use.

## Configured levels

<tempered:Settings topic="reinforced" />

Saved levels above a lowered cap are retained; the active bonus uses the cap.
Disabling Reinforced keeps that history. Raising the cap or re-enabling the
mechanic makes it available again.
