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
other stack data. An Aspect cannot be applied past level V.

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
damage, and creative-mode kills do not. A qualifying hit that would leave a
hostile enemy at or below the current threshold becomes lethal.

| Tier | Total hostile kills | Execute threshold |
| --- | ---: | ---: |
| I | 64 | 5% health |
| II | 192 | 10% health |
| III | 384 | 15% health |

Executioner uses the target's current maximum health and accounts for
absorption. It works on vanilla bosses, preserves normal loot and death credit,
and allows Totems of Undying to resolve normally. Weapon eligibility is
controlled by [`tempered:executioner_applicable`](src/main/resources/data/tempered/tags/item/executioner_applicable.json).
Datapacks can exempt special or scripted hostile mobs through the empty-by-default
[`tempered:executioner_immune`](src/main/resources/data/tempered/tags/entity_type/executioner_immune.json)
entity-type tag.

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
```

Development builds include a compact equipment debug overlay above the hotbar.
For Executioner weapons it shows tier progress and execute threshold; aiming at
an entity additionally shows its health, eligibility, and live execute-range
status. The overlay is disabled automatically in production builds.

The source is organized by responsibility:

- `equipment` contains immutable Attribute and Affix data stored on item stacks.
- `service` contains gameplay rules for Attributes and Affixes.
- `recipe/aspect` connects those rules to the smithing-table recipe.
- `event` contains tooltip, mining-speed, and affix combat event handling.
- `registry` owns NeoForge registrations.
- `data/tempered/tags` holds datapack-configurable item and entity eligibility.

## Roadmap

The planned direction is a broader system where Attributes are deliberate
upgrades and more Affixes are earned from equipment use. Aspect acquisition,
additional Attributes and Affixes, and the dedicated Tempering Block are not
implemented yet.

## License

All Rights Reserved.
