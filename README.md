# Tempered

Tempered is a Minecraft equipment-progression mod for **Minecraft 1.21.1**
with **NeoForge**. It adds player-chosen **Attributes** through consumable
**Aspects** and **Affixes** earned by using equipment. The current implementation
focuses on a small, understandable foundation that can grow into a larger
tempering system later.

## Current features

### Applying Aspects

Apply an Aspect at a smithing table:

1. Put the target item in the **base** slot (the middle slot).
2. Put an Aspect in the **addition** slot (the right slot).
3. Leave the template slot empty.
4. Take the result to apply the next level and consume one Aspect.

The result keeps the original item's enchantments, custom name, damage, and
other stack data. By default, an Aspect cannot be applied past level V.
The configuration can change each Aspect's bonuses and level cap independently.

### Reinforced Aspect

Reinforced can be applied to any damageable item, including tools, weapons,
armor, shields, and compatible modded equipment.

| Level | Maximum durability bonus |
| --- | ---: |
| I | +10% |
| II | +20% |
| III | +30% |
| IV | +40% |
| V | +50% |

The first application records the item's original maximum durability. Every
later level is calculated from that same baseline, so the bonus never compounds
unexpectedly.

### Swift Aspect

Swift can be applied to pickaxes, axes, shovels, and hoes. It multiplies the
player's final mining speed, so it composes with effects such as Efficiency and
Haste.

| Level | Mining speed bonus |
| --- | ---: |
| I | +8% |
| II | +16% |
| III | +24% |
| IV | +32% |
| V | +40% |

Swift eligibility is controlled by the datapack tag
[`tempered:swift_applicable`](src/main/resources/data/tempered/tags/item/swift_applicable.json).
By default it includes the vanilla `minecraft:pickaxes`, `minecraft:axes`,
`minecraft:shovels`, and `minecraft:hoes` tags. Modpacks and datapacks can
extend this tag without changing Java code.

### Executioner Affix

Executioner is earned by killing hostile enemies with a sword or axe. Only
direct player melee kills count; projectiles, damage over time, environmental
damage, reflected damage, and creative-mode kills do not by default. Sweeping
kills and spawner mobs count. A qualifying hit that would leave a
hostile enemy at or below the current threshold becomes lethal.

| Tier | Total hostile kills | Execute threshold |
| --- | ---: | ---: |
| I | 2 | 5% health |
| II | 5 | 10% health |
| III | 10 | 15% health |

Executioner uses the target's current maximum health and accounts for
absorption. It works on vanilla bosses, preserves normal loot and death credit,
and allows Totems of Undying to resolve normally. Weapon eligibility is
controlled by [`tempered:executioner_applicable`](src/main/resources/data/tempered/tags/item/executioner_applicable.json).
Datapacks can exempt special or scripted hostile mobs through the empty-by-default
[`tempered:executioner_immune`](src/main/resources/data/tempered/tags/entity_type/executioner_immune.json)
entity-type tag.

Progress is awarded to the weapon captured at the death event, at the end of
the server tick after all death listeners have finished. Canceled deaths and
Totem survival grant no progress; duplicate notifications for a target within
one tick produce at most one award. Switching weapons before the award does
not transfer credit. Each successful award uses the configured progress amount
and announces only the highest tier reached when it crosses multiple tiers.

## Configuration

All gameplay settings live in **`config/tempered/config.toml`**, generated when
a world/server first starts. Use [`config.toml.example`](config.toml.example)
as the reference: it documents every key, default, valid range, and unit.
The tables above show defaults; each Aspect and Affix can be tuned independently.

Settings are server-authoritative and require a **world/server restart**.
Connected clients receive the server's active settings, including players joining
after the file was edited with changes pending. An existing file at
`<world>/serverconfig/tempered/config.toml` overrides the normal file; only use
an override if you deliberately want world-specific balance.

Aspect bonus lists contain the total bonus at each level, and their length sets
the level cap. Executioner's three tier lists must have matching lengths, and
its milestones must increase. Chances are probabilities from `0.0` (never) to
`1.0` (always); `0.25` means 25%. Aspect application remains deterministic.
Only qualifying melee deaths/hits roll for Executioner progress/execution.
When progress per success is greater than one, the saved counter represents
progress points rather than literal kills. Executioner continues recording
history beyond the highest configured tier, saturating at the integer limit.

Disabling a mechanic or lowering its cap preserves saved levels and progress.
Re-enabling it or raising the cap makes that history available again.
Existing Reinforced equipment recalculates when carried or applied in smithing,
preserving its remaining durability fraction. Unloaded storage is reconciled
when brought into use. Individual invalid values revert to defaults through
NeoForge; mismatched Executioner tier-list lengths use the complete default
Executioner section and log an explanation.

Eligibility tags and acquisition recipes remain datapack-controlled.

## Development

Requirements:

- Java 21
- Minecraft 1.21.1
- NeoForge 21.1.248

Useful Gradle commands:

```sh
./gradlew runClient
./gradlew runServer
./gradlew build
./gradlew test
./gradlew runGameTestServer
./gradlew updateConfigExample
```

Development builds include a compact equipment debug overlay above the hotbar.
For Executioner weapons it shows tier progress and execute threshold; aiming at
an entity additionally shows its health, eligibility, and live execute-range
status. The overlay is disabled automatically in production builds.

The source is organized by responsibility:

- `equipment` contains immutable Attribute and Affix data stored on item stacks.
- `equipment` also contains rules that derive effective levels and bonuses from settings.
- `config` declares typed sections and the immutable active gameplay snapshot.
- `service` validates and applies changes to equipment.
- `network` synchronizes active settings when players join.
- `recipe/aspect` connects those rules to the smithing-table recipe.
- `event` contains tooltip, mining-speed, and affix combat event handling.
- `registry` owns NeoForge registrations.
- `data/tempered/tags` holds datapack-configurable item and entity eligibility.

Whenever config keys, defaults, or explanations change, run
`./gradlew updateConfigExample` and commit the updated reference. The test suite
checks the reference against the registered specification, so stale examples
fail the build.

`build` runs both verification suites. `test` checks configuration and registered
equipment data. `runGameTestServer`
runs four focused gameplay scenarios in a real Minecraft world, covering combat
attribution, cancellation, execution, smithing consumption, and saved equipment.
These tests and their immunity-tag fixture live in `src/gameTest` and load only
in that run; they are excluded from the distributed mod and normal play runs.

## Roadmap

The planned direction is a broader system where Attributes are deliberate
upgrades and more Affixes are earned from equipment use. Aspect acquisition,
additional Attributes and Affixes, and the dedicated Tempering Block are not
implemented yet.

## License

All Rights Reserved.
