---
navigation:
  title: Upgrades and repairs
  icon: minecraft:anvil
  parent: index.md
  position: 6
---
# Upgrade and repair equipment

Keep your Aspect levels and [Executioner progress](executioner.md) when upgrading
materials, repairing, renaming or changing enchantments. For adding an Aspect,
follow [Applying Aspects](smithing.md).

## Upgrade to netherite

1. Put a netherite upgrade template in the **left** smithing slot.
2. Put your diamond equipment in the **middle** slot.
3. Put a netherite ingot in the **right** slot.
4. Check the result, then take it.

<Row>
  <ItemImage id="minecraft:netherite_upgrade_smithing_template" />
  <ItemImage id="minecraft:diamond_axe" />
  <ItemImage id="minecraft:netherite_ingot" />
</Row>

The result keeps enchantments, names, Aspect levels and earned progress.
[Reinforced](reinforced.md) uses the new material's durability for its bonus.
Previewing or moving ingredients does not change them; taking the result
consumes them once.

## How much durability remains?

<tempered:Settings topic="maintenance" />

Percentage-based wear keeps a half-worn item roughly half-worn after upgrading.
Keeping damage points instead carries the same number of used durability points.
Rounding can change wear slightly; damage is always clamped below the new maximum.
Material upgrades do not fully repair worn equipment.

These rules also cover compatible material upgrades at a smithing table.
Other mods' custom recipes or machines may handle history differently.

## Repair or rename in an anvil

Put the equipment whose history you want to keep in the **left slot**. Use a
repair material, another matching item or an enchanted book in the right slot
as usual. Renaming and enchantment changes also keep the left item's history.

When combining two equipment items, the right item's levels and progress are
**not added** to the result. Its history is sacrificed with that item.

## Disenchant or add a trim

A grindstone removes normal enchantments while retaining Tempered history.
If combining two items, put the item whose history you want to keep in the
**top slot**; the bottom item's history is sacrificed.

Armor trims keep the base armor's history and wear.

## Why does crafting repair have no result?

Two-item crafting repair is blocked if **either input has Tempered history**,
because it would erase that history. **Use an anvil instead**, with the item
you want to keep in the left slot. This protection also applies to disabled effects.

## When server balance changes

Disabled effects and levels above a lowered cap retain their history through
upgrades. Re-enabling an effect or raising its cap makes that history available
again. See [server configuration](configuration.md) for tuning and restart rules.
