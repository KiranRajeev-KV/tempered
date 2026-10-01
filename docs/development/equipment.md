# Equipment persistence and integrations

Player-facing rules belong in the guide's maintenance and configuration pages.
This document records contracts needed when changing gameplay code.

## Saved component contract

Immutable item-stack components use NeoForge's
[persistent codecs](https://docs.neoforged.net/docs/1.21.1/items/datacomponents/),
which also supply their default network encoding. Their registry IDs are permanent.

| Component | Representation |
| --- | --- |
| `tempered:reinforced_data` | `level`, `base_max_damage`, `format_version`, optional `baseline_item` |
| `tempered:swift_data` | Positive integer level |
| `tempered:executioner_data` | Positive integer accumulated progress |

History is valid through the integer limit independently of active caps and
milestones. Swift and Executioner retain their scalar formats. Executioner's
`hostileKills` accessor represents progress points under non-unit award settings;
changing award amount never rescales saved history. Counters saturate safely.

Reinforced reads pre-versioned records as version 1 and writes version 2.
`baseline_item` names the item whose unreinforced durability supplied the baseline.
It is a resource ID, not a required live registry lookup. Legacy records retain
an unknown owner, including when re-encoded; a supported material upgrade
establishes a new baseline and owner. Never infer old durability from the current
item ID: earlier upgrades or other mods may have customized it.

Malformed present fields and unsupported versions fail decoding; optional fields
are not lenient. Minecraft may reject a containing stack with invalid serialized
components. This contract handles compatible old saves, not arbitrary corruption
or downgrade recovery. When a structure changes, add an explicit old-version
reader and literal compatibility fixtures rather than a reset-to-default fallback.

The literal fixture in `src/test/resources/equipment/legacy-diamond-axe.snbt`
checks registered item loading, balance changes, persistence and network transfer.
The active-settings payload uses protocol 3; bump it if its wire layout changes.

## Native adapters

`SmithingTransformRecipeMixin` adjusts the assembled output through MixinExtras
[`ModifyReturnValue`](https://github.com/LlamaLad7/MixinExtras/wiki/ModifyReturnValue).
[NeoForge already supplies MixinExtras](https://github.com/LlamaLad7/MixinExtras#setup).
Keep hooks thin; calculation belongs in `EquipmentUpgradeRules`, component
mutation in `EquipmentUpgradeService`. Never mutate ingredients in assembly.

For material changes, use the destination recipe template's maximum durability,
including explicit components, not the assembled stack's inherited source
`MAX_DAMAGE`. Preserve saved progression regardless of enabled effects, apply
the current bonus once, and clamp carried wear below the new maximum. A
destination without durability keeps dormant history without inherited durability
components. Same-item transforms and Aspect application keep their existing rules.

Custom recipe classes that bypass native assembly and other machines need an
explicit adapter. Respect unrelated components and let native menus own pickup,
consumption and inventory synchronization. Reconciliation must retain the newly
recorded baseline instead of undoing the upgrade.

Anvil and trim paths copy base components; grindstone copies the selected/top
input. No donor-history merge is implemented. `RepairItemRecipeMixin` guards
shared input validation so matching and direct assembly both reject history loss.
Use component presence, not active effects or eligibility, for this guard.

Executioner death awards commit at the server tick's end after cancellation and
Totem resolution. They retain the captured weapon, deduplicate targets for that
tick, and announce only the highest tier crossed. Preserve those contracts when
changing event priorities or attribution.

## Verification

The two equipment GameTest scenarios cover native smithing preview/pickup,
consumption, disabled/capped history, custom destination durability, wear clamping,
anvil work, armor trim, disenchanting and crafting-repair rejection. Use these
scenarios for related changes rather than duplicating each helper in unit tests.

For manual verification, prepare worn, named and enchanted progressed equipment:

1. Preview a netherite upgrade repeatedly, remove/reinsert inputs, then take it.
   Check history, wear and consumption once; apply another Aspect afterward.
2. Try anvil rename, material repair, enchanted books and equipment with distinct
   histories in each slot. Check the left item's ownership.
3. Try grindstone disenchanting and a trim on progressed armor.
4. Try crafting repair with history in either position; compare ordinary items.
5. Restart with disabled effects/lower caps, then restore them. Repeat smithing
   and repair checks; history must remain.
6. Change wear policy, restart, and compare the guide with a remote client's
   differing local settings. Save/reopen inventory and stored equipment.
