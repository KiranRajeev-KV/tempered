# Tempered

Tempered is a Minecraft equipment-progression mod for **Minecraft 1.21.1**
with **NeoForge**. It adds player-chosen **Attributes** through consumable
**Aspects**. The current implementation focuses on a small, understandable
foundation that can grow into a larger tempering system later.

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

The source is organized by responsibility:

- `equipment/attribute` contains the data stored on upgraded item stacks.
- `service` contains gameplay rules for validating and applying each Aspect.
- `recipe/aspect` connects those rules to the smithing-table recipe.
- `event` contains client tooltip and mining-speed event handling.
- `registry` owns NeoForge registrations.
- `data/tempered/tags` holds datapack-configurable item eligibility.

## Roadmap

The planned direction is a broader system where Attributes are deliberate
upgrades and Affixes are earned from equipment use. Affixes, Aspect acquisition,
additional Attributes, and the dedicated Tempering Block are not implemented
yet.

## License

All Rights Reserved.
