# Guide authoring and integration

## Writing for players

Begin each page with what the player can do. Give instructions in play order,
use short paragraphs and descriptive headings, and put troubleshooting near
the task it addresses. Link to background or another task rather than inserting
every detail into the first-use instructions. This follows
[Diátaxis's task-oriented guidance](https://diataxis.fr/how-to-guides/).

Keep basic help available in tooltips and let players reopen the full guide at
any time. Introduce related concepts where they are useful rather than front-loading
the overview; [Game Accessibility Guidelines recommends contextual help](https://gameaccessibilityguidelines.com/include-contextual-in-game-helpguidancetips/).
Use familiar terms and direct sentences, following its
[plain-language guidance](https://gameaccessibilityguidelines.com/use-simple-clear-language/).

Use item images with text labels, numbered steps for tasks, and linked item names
for identification. Do not convey a rule only through an icon or color. Avoid
long tables on narrow game screens. The sidebar and related-page links should
make each task reachable without reading the whole guide.

Keep Java classes, codec versions and test procedures out of player pages.
Include a technical identifier only where it helps a server owner configure
the mod or explains a compatibility decision. Describe effective balance from
the active server settings, not duplicated default numbers.

## Resources and navigation

The guide ID is `tempered:guide`. Pages live in
`assets/tempered/guides/tempered/guide`. Follow GuideME's
[authoring API](https://guideme.appliedenergistics.org/authoring/): lowercase
filenames, one initial H1, navigation frontmatter, and relative Markdown links.
Use `ItemImage`, `ItemLink` and `SubPages` for item identification and navigation.

`GuideTopic` declares topic IDs, command routes and which topics have configured
sections. Add a page to that catalog and give it a unique sidebar position under
`index.md`. Keep both Aspects' `item_ids` on their primary pages for item links
and the Open Guide shortcut. Commands need no cheats or permissions.

Static prose belongs in Markdown and is searchable. Resource packs can override
pages; translated pages use GuideME's `_<language>` subdirectories. Translated
rows shared with tooltips live in `en_us.json`. Dynamic values are not part of
GuideME's static search index, so put relevant searchable terms in the prose.

## Client integration

`TemperedGuide` registers a public-API GuideME builder and
`SettingsTagCompiler` in its client mod constructor, **before the initial resource
reload prepares pages**. Registering in client setup is too late. Common gameplay
code must not load client layout classes on a dedicated server.

`GuideCommandHandler` queues opening with `Minecraft.tell`: ChatScreen closes
after command execution, so opening inline would close the guide immediately.
Use `GuidesCommon.openGuide` and `PageAnchor`, not GuideME internals.

`<tempered:Settings topic="..." />` renders configured topics from
`GuideSettingsContent`. Compiler state captures one immutable active snapshot
per page, even with multiple tags. There is no cross-visit compiled-value cache.
Revisiting a page after sync reflects new settings; an already-open page keeps
its captured snapshot. Invalid/static topic names produce an authoring error.

## Verification

Extend the existing GuideME integration scenario when navigation or tags change.
It parses all catalog pages, checks internal links and item targets, validates
authored settings tags, and exercises recompilation after an active-settings
change. Presentation tests cover configured values and tooltip non-mutation.

For client checks, verify command opening with cheats disabled, sidebar, search,
cross-links, scrolling, back/forward and both Aspect shortcuts. Check non-default
server values, disabled mechanics, reconnect, resource reload, smaller windows
and larger GUI scales. Check that the task remains understandable before its
optional details and configured tables are read.
