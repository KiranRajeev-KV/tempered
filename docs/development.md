# Development

Target: Java 21, Minecraft 1.21.1, NeoForge 21.1.248, GuideME 21.1.19.
Versions are declared in `gradle.properties`; use the checked-in Gradle wrapper.

## Commands

| Command | Purpose |
| --- | --- |
| `./gradlew build` | Compile, package and run JUnit plus dedicated-server GameTests |
| `./gradlew test` | Configuration, presentation, guide integration and save compatibility |
| `./gradlew runGameTestServer` | Native combat, menus, loot and equipment transactions |
| `./gradlew runClient` | Client development with GuideME and optional JEI |
| `./gradlew runServer` | Dedicated-server development |
| `./gradlew updateConfigExample` | Regenerate the TOML reference from the registered specification |

Artifacts are in `build/libs`. GameTests and their datapack fixtures load only
in the dedicated test run and are excluded from the mod jar. Prefer a few
complete scenarios covering real regressions over tests of trivial helpers.

The development-only equipment overlay shows held-item progression, mining
speed and aimed-target execute eligibility. Production builds disable it.

The optional treasure chest renderer is controlled by
`rendering.custom_chests` in `config/tempered/client.toml`, defaulting to `false`.
It works in development and production clients. With it enabled, single and
double chests use the exported cuboid geometry and pixel atlas, and the lid
follows vanilla opening animation. The inventory and held item use the same
renderer. Trapped and Ender chests retain their usual appearance. Changes apply
immediately through the Mods configuration screen or a reloaded config file.
Run `./gradlew runClient` and enable the option to test normal chests.
The editable Blockbench project is
`src/main/resources/assets/tempered/models/preview/treasure_chest.bbmodel`;
its atlas is `assets/tempered/textures/entity/treasure_chest.png`. The build
excludes `.bbmodel` source files. Edits to the Blockbench project must also be
exported to the renderer's `models/preview/treasure_chest.json` geometry.

## Responsibilities

| Source | Responsibility |
| --- | --- |
| `equipment` | Immutable persisted history and derived gameplay rules |
| `service` | Eligibility checks and controlled stack mutations |
| `config` | NeoForge specification and immutable active settings |
| `network` | Server-authoritative active snapshot sent on login |
| `recipe/aspect`, `loot` | Smithing and acquisition adapters |
| `event`, `mixin` | Thin native gameplay hooks |
| `presentation` | Read-only tooltip and configured guide content |
| `client/guide` | Client registration, custom tags and command opening |
| `registry`, `tag` | Registrations and datapack compatibility keys |

Capture one settings snapshot per operation or page. Do not read live TOML
values from gameplay code. Changes are pending until restart; clients receive
the active snapshot even if the file already contains pending edits.

## Documentation ownership

README is the project introduction, installation and entry point. Player rules,
how-to instructions, troubleshooting and balance explanations belong in the
[packaged guide](../src/main/resources/assets/tempered/guides/tempered/guide).
`config.toml.example` remains the generated reference for configuration keys.

- [Guide authoring and integration](development/guide.md)
- [Equipment persistence and native integrations](development/equipment.md)

When behavior changes, update its guide page and existing relevant checks.
When config keys, defaults or comments change, regenerate `config.toml.example`;
the build rejects a stale reference. Avoid maintaining duplicate player manuals
in this directory.
