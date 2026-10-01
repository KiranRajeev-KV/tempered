# Tempered

Tempered adds equipment progression to **Minecraft 1.21.1 on NeoForge**.
Find Aspects as exploration loot, apply chosen upgrades at a smithing table,
and earn Affixes by using your equipment.

- **Reinforced:** more durability.
- **Swift:** faster mining.
- **Executioner:** hostile melee kills unlock execution effects.
- Progress stays with your equipment through supported upgrades and repairs.

## Installation

Install Tempered and [GuideME](https://www.curseforge.com/minecraft/mc-mods/guideme)
in the `mods` folder on both clients and servers. Use **NeoForge 21.1.248+** and
**GuideME 21.1.19+ within the 21.1 release line**, for Minecraft 1.21.1.

## In-game guide

Run **`/tempered guide`** for getting started, mechanics, equipment maintenance
and server configuration. It works with cheats disabled and shows the server's
active balance. Hold **Shift** over equipment for details, or GuideME's
**Open Guide** key (**G** by default) over an Aspect to open its page.

## Configuration

Gameplay settings use **`config/tempered/config.toml`**, generated when a
world/server starts. Changes require a **world/server restart**. The in-game
configuration page explains tuning; [config.toml.example](config.toml.example)
documents every key, default and allowed value.

## Development

Use Java 21 and the included Gradle wrapper:

```sh
./gradlew build
./gradlew runClient
```

See [developer documentation](docs/development.md) for architecture, verification
and guide maintenance. Development runs download GuideME automatically.

## License

All Rights Reserved. The NeoForge template's license is retained in
[TEMPLATE_LICENSE.txt](TEMPLATE_LICENSE.txt).
