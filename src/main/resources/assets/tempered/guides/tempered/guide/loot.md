---
navigation:
  title: Finding Aspects
  icon: minecraft:chest
  parent: index.md
  position: 5
---
# Find Aspects

Explore the sources listed below to find
<ItemLink id="tempered:reinforced_aspect" /> and
<ItemLink id="tempered:swift_aspect" />.
Aspects have **no crafting recipes**; apply found ones at a
[smithing table](smithing.md).

Default sources include dungeon chests, abandoned mineshaft chest minecarts,
desert pyramids and jungle temples. Your server can change these sources.

## Active sources and rewards

<tempered:Settings topic="loot" />

## Can both Aspects appear?

Yes. Reinforced and Swift roll independently when eligible loot generates.
A container can contain **either, both or neither**. Counts are chosen from
the configured inclusive range.

## Why does an old chest have none?

Unopened containers with pending loot can receive Aspects, including in existing
worlds. **Already-generated loot is unchanged.** Reopening or emptying a
container never rerolls it. Ordinary storage containers receive nothing.

Rewards are added alongside existing loot. Container space, other mods and
datapacks can affect the final contents. Custom sources only work if their
loot-table IDs exist and the world uses them.

## What if acquisition is disabled?

Disabling acquisition alone leaves found Aspects usable. Disabling the Aspect
effect also suppresses its loot and prevents new applications; saved upgrades
remain. See [server configuration](configuration.md) for tuning.
