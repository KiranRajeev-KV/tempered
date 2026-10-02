---
navigation:
  title: Server configuration
  icon: minecraft:comparator
  parent: index.md
  position: 7
---
# Configure your world or server

The guide's configured sections show the **server's active balance**.
Players on a remote server cannot override it with a local configuration.
Server owners can tune bonuses, levels, milestones, probabilities and loot
independently in one TOML file.

## Change settings

1. Start a world/server once to generate `config/tempered/config.toml`.
2. Stop the world/server and edit that file.
3. Restart to activate the changes.
4. Reopen the relevant guide page to check the active values.

An existing `<world>/serverconfig/tempered/config.toml` overrides the normal
file. Use a world override only when that world should have its own balance.
Edits while running stay pending until restart, including for players joining
after the edit. On a local world, NeoForge's Mods configuration screen can edit
settings too; the same restart rule applies.

The supplied `config.toml.example` documents every key, range and unit. The
sections below explain how the settings affect play.

## Aspect bonuses and caps

`aspects.reinforced` and `aspects.swift` have independent `enabled` switches.
Their bonus lists use **total percentages at each level**, not extra percentages
added to the previous level. The list length sets the application cap.

For example, `[5.0, 17.5]` gives two levels: a total 5% bonus at level I and
17.5% at level II. Lists accept 1–100 entries, with each bonus from 0–10000%.
Use `durability_bonus_percent_by_level` for Reinforced and
`mining_speed_bonus_percent_by_level` for Swift. Aspect application is deterministic.

View current [Reinforced levels](reinforced.md) or [Swift levels](swift.md).

## Executioner milestones and chances

Within `affixes.executioner`, `kills_required_by_tier` gives total progress
milestones. Entries must be strictly increasing positive integers.
`execute_health_percent_by_tier` gives thresholds from 0–100% and
`execute_chance_by_tier` gives the probability at each tier. All three lists
must contain the same number of tiers, from 1–100.

`progress_chance` rolls once per qualifying hostile melee death.
`progress_per_success` sets the positive integer award on success.
Changing the award amount affects future kills and does not rescale saved history.
Guaranteed one-point awards display hostile kills; other settings display
progress points. History continues past the highest tier until the counter's limit.

Set `progression_enabled = false` to stop new progress while keeping earned
effects. `enabled = false` disables both. `allow_creative_progression` controls
creative kills; `announce_tier_up` controls tier messages.

**Probabilities use 0–1:** `0.0` never, `0.25` means 25%, and `1.0` always.
Only qualifying deaths and executable hits roll. View the current
[Executioner progression and tiers](executioner.md).

## Aspect loot

`acquisition.reinforced` and `acquisition.swift` independently control `enabled`,
`chance`, `min_count`, `max_count` and `loot_tables`.
Counts are inclusive integers from 1–16; each count within the range is equally
likely. The two Aspects roll independently.

Source IDs are exact loot-table names, for example `minecraft:chests/simple_dungeon`
or a custom `othermod:chests/ruins`. Use container tables to keep acquisition
exploration-based. An empty source list disables all sources; duplicate IDs
count once and unknown IDs do nothing. Up to 256 source IDs are allowed.

Disabling acquisition alone leaves found Aspects usable. Disabling the Aspect
mechanic also suppresses its loot. Existing generated loot is unchanged; see
[Finding Aspects](loot.md) for current sources and unopened-container behavior.

## Upgrade wear

`equipment.upgrades.damage_policy` accepts `remaining_fraction` or
`damage_points`. It controls material smithing for equipment with Tempered
history, not Aspect application or durability changes caused by balance edits.
See [Upgrades and repairs](maintenance.md) for the active policy and examples.

## Retained history and invalid settings

Disabling effects or lowering caps does not erase levels or earned progress.
The active effect uses the new balance; restore the setting to use retained
history again. Reinforced adjusts carried equipment's durability while keeping
its remaining percentage. Stored equipment adjusts when brought into use.

Individual invalid values revert to defaults. Mismatched Executioner tier lists
use the complete default Executioner section. An inverted loot count range uses
default counts while preserving the other acquisition settings. Check the
server log if the active guide values differ from what you intended.

## Datapack compatibility

Datapacks control eligible Swift tools with `tempered:swift_applicable`,
Executioner weapons with `tempered:executioner_applicable`, and exempt hostile
mobs with `tempered:executioner_immune`. Eligibility and immunity use tags;
gameplay balance and acquisition source lists use the TOML file.

## Custom chest appearance

Your local `config/tempered/client.toml` contains a separate visual option:

```toml
[rendering]
custom_chests = false
```

Set it to `true` to use the treasure chest model for ordinary single and double
chests, including inventory and held items. Set it to `false` to restore the
usual appearance. The default is off. Trapped and Ender chests are unaffected.
You can also change it through **Mods → Tempered → Config → client.toml**;
changes apply immediately. This is a personal preference and does not change
the server's settings or other players' chest appearance.
