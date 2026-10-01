---
navigation:
  title: Finding Aspects
  icon: minecraft:chest
  parent: index.md
  position: 5
---
# Finding Aspects

Find <ItemLink id="tempered:reinforced_aspect" /> and
<ItemLink id="tempered:swift_aspect" /> as **exploration loot**.
Aspects have **no crafting recipes**. Use found Aspects at a
[smithing table](smithing.md).

## Independent rolls

Reinforced and Swift roll independently when an eligible loot table generates.
A container can contain **either, both or neither**. The server's configured
chances, quantities and source tables appear below.

## Existing worlds and containers

Unopened containers with pending loot can receive Aspects, including in existing
worlds. **Already-generated loot is unchanged.** Reopening or emptying a container
never rerolls it, and ordinary storage containers receive nothing.

Loot is added alongside existing rewards. Container space, other mods and
datapacks can affect the final contents. Configured custom sources only work
when their loot-table IDs exist and are used by the world.

## Configured acquisition

<tempered:Settings topic="loot" />

Disabling acquisition leaves existing Aspects usable. Disabling the Aspect
mechanic also suppresses its loot and prevents new applications; saved upgrades
are retained.
