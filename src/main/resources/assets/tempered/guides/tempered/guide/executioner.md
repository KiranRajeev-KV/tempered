---
navigation:
  title: Executioner
  icon: minecraft:iron_sword
  parent: index.md
  position: 4
---
# Executioner

Earn **Executioner** by killing eligible hostile mobs with a compatible weapon,
usually a sword or axe. Progress belongs to the weapon. There is no
Executioner Aspect or crafting recipe.

Fresh compatible weapons show awakening in their tooltip. Hold **Shift** to
check earned progress and the next milestone.

## Which kills count?

Direct player **melee kills** count, including **sweeping kills and spawner mobs**.
Projectiles, damage over time, environmental and reflected damage do not.
Switching weapons after a kill does not transfer its credit.

The server controls the award chance and amount, whether creative kills count,
and whether progression is paused. Guaranteed one-point awards display
**hostile kills**; other settings display **progress points**.

## Active progression and tiers

<tempered:Settings topic="executioner" />

## What does execution do?

After unlocking a tier, a qualifying melee hit can become lethal if it would
leave an eligible hostile mob alive **at or below that tier's health threshold**.
Each tier has its own execute chance. A zero threshold or chance means execution
is unavailable at that tier. Naturally lethal hits need no execution roll.

The threshold uses the target's current maximum health. Absorption is accounted
for; a fully absorbed hit does not execute. Vanilla bosses are eligible unless
the server's datapacks exempt them.

**Totems can save targets normally.** Totem survival and canceled deaths grant
no progress. Normal loot and death credit are preserved.

## Keeping your progress

[Material upgrades and anvil work](maintenance.md) retain the base weapon's
progress. History continues past the highest tier until the counter's limit.
Disabled effects and incompatible weapons retain it.

Pausing progression keeps already-earned execution effects. Changing the
configured award amount affects future kills and does not rescale earned
history. See [server configuration](configuration.md).
