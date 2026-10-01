---
navigation:
  title: Executioner
  icon: minecraft:iron_sword
  parent: index.md
  position: 4
---
# Executioner

Executioner is an **earned Affix**. Use a compatible weapon, usually a sword
or axe, to kill eligible hostile mobs. Progress belongs to that weapon.
There is no Executioner Aspect or crafting recipe.

## Earning progress

Direct player **melee kills** on eligible hostile mobs grant progress.
**Sweeping kills and spawner mobs count.** Projectiles, damage over time,
environmental and reflected damage do not count. The server's datapacks can
change weapon compatibility and exempt hostile mobs.

The configured section below tells you whether creative-mode kills count,
whether progression is paused, and the chance and amount of each progress award.
With guaranteed one-point awards, progress is measured in **hostile kills**.
Other configurations use **progress points**.

## Executing a target

After unlocking a tier, a qualifying melee hit can become lethal if it would
leave an eligible hostile mob alive **at or below the tier's health threshold**.
Each tier has its own execute chance. A zero threshold or zero execute chance
means execution is unavailable at that tier.

The threshold uses the target's current maximum health. Absorption is accounted
for; a fully absorbed hit does not execute. Vanilla bosses are eligible unless
exempted by datapacks.

**Totems can save the target normally.** Totem survival and canceled deaths
grant no progress. Normal loot and death credit are preserved.

## Configured progression and tiers

<tempered:Settings topic="executioner" />

## Keeping your history

Saved progress is retained when balance changes. History keeps growing past
the highest tier, up to the counter's limit. Disabled effects do not erase it.
Pausing progression does not disable an already-earned execution effect.
