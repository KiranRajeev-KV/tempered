# In-game guide

GuideME is a required, separately installed dependency on clients and servers.
Development runs download version 21.1.19 from Maven Central automatically.
Tempered uses only the published API; it does not reference GuideME internals.
The integration follows GuideME's [guide builder](https://guideme.appliedenergistics.org/integration/)
and [custom tag](https://guideme.appliedenergistics.org/integration/custom-tag/)
APIs, with a separate [NeoForge client entrypoint](https://docs.neoforged.net/docs/1.21.1/concepts/sides/).

## Access

- `/tempered guide` opens the overview. The command is local and needs no cheats or permissions.
- `/tempered guide <topic>` opens `overview`, `smithing`, `reinforced`, `swift`, `executioner` or `loot`.
- Hold GuideME's Open Guide key (G by default) over either Aspect to open its page.
- The sidebar, search, cross-links and scrolling replace chat pagination. Numeric page arguments were removed.

Tooltips remain concise. Shift details explain eligibility, application caps,
active bonuses, progress and retained history without changing equipment.

## Structure and active configuration

The guide ID is `tempered:guide`. Markdown resources live in
`assets/tempered/guides/tempered/guide`; pages use GuideME frontmatter for
navigation and Aspect item links. Static prose is Markdown so GuideME can index
it for full-text search. Resource packs can override pages and provide translated
versions using GuideME's `_<language>` subdirectories.

`GuideSettingsContent` produces translated rows from an explicit immutable
`GameplaySettings` snapshot. It contains all configured levels, Executioner
tiers and loot sources, with no arbitrary display limits. It explains disabled
mechanics, paused progression, creative progression, unavailable loot and custom
progress-point units. `GuideText` holds translations shared with tooltips.

The client-only `SettingsTagCompiler` adapts `<tempered:Settings topic="..." />`
to GuideME paragraphs. Supported topics are `reinforced`, `swift`, `executioner`
and `loot`; invalid tags show an authoring error. Page compiler state captures
one active snapshot for all settings tags on a page. Each page visit compiles
fresh rows; there is no cross-world cache of configured values. After a server
settings sync, reopening/navigating to a page reflects the current snapshot.
An already-open page represents its captured snapshot until revisited.

`TemperedGuide` is a separate client `@Mod` entrypoint. Register the guide in its
constructor, before the first resource reload begins preparing pages; client
setup events run too late for that initial load. The common mod entrypoint and
gameplay code never load GuideME's client layout classes on a dedicated server.

Dynamic numbers and source IDs are not in GuideME's static search index. Search
finds the explanatory topic text; configured values appear when viewing a page.
Configuration remains server-authoritative and restart-only. No new gameplay
configuration keys, packets, recipes or items are introduced by this port.

## Maintenance

Keep rules in gameplay services and typed settings; presentation only describes
them. Update Markdown when mechanics change, and update translated configured
rows when settings change. Add future topics through `GuideTopic`, a Markdown
page and a settings section only if needed. Keep Aspect IDs in their pages'
`item_ids` metadata so the shortcut and item links keep working.

When configuration keys, defaults or explanations change, run
`./gradlew updateConfigExample` and commit `config.toml.example`. This port does
not change the specification; its reference remains checked by the build.

The focused presentation tests exercise equipment eligibility and non-mutation,
configured guide output with large lists, and GuideME page/resource integration.
The build also starts a dedicated GameTest server, catching accidental client
class loading in common gameplay code.

## Client verification

Check the GUI command with cheats disabled, sidebar and cross-links, search,
scrolling, back/forward, and both Aspect shortcuts. Verify non-default server
bonuses, disabled mechanics and loot, and progress-point units. Reconnect to a
server with different active settings and revisit pages to check their values.
Also check a resource reload and a smaller window / larger GUI scale.
