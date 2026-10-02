# Planned design

This preserves the original mod direction from README commit `f5cfeaf` and
distinguishes it from the implemented foundation. These are future design
requirements, not claims about current gameplay or a finalized implementation
plan. Player instructions for implemented features belong in the in-game guide.

## Core direction

Attributes are chosen by the player using Aspects. Affixes are earned through
an item's experiences. Greater power comes with greater risk.

## Remaining original ideas

| Idea | Original direction | Current status |
| --- | --- | --- |
| Tempering Block | One main workstation for equipment management | Not implemented |
| Attribute limit | At most five different Attributes on one item | Not enforced; only Reinforced and Swift exist |
| Adding Attributes | Adding another Attribute increases permanent item-destruction risk | Not implemented; smithing Aspect application is deterministic |
| Upgrading Attributes | Safer than adding a new Attribute, but still carries destruction risk | Not implemented |
| Risk ceiling | Destruction chance never reaches 100% | Future constraint; no destruction rolls exist |
| Rerolling and removal | The workstation supports rerolling, removing and managing Attributes and Affixes | Not implemented; exact operations remain undecided |
| More Attributes and Affixes | Expand beyond the existing equipment effects | Two Attributes and one Affix exist |
| More Affix activities | Earn different Affixes through different activities, including while wearing equipment | Only Executioner hostile melee progression exists |
| Affix count | No global maximum number of Affixes per item | Original direction; multiple-Affix behavior remains to be designed |

The five-Attribute limit concerns distinct Attributes, not their upgrade levels.
The Affix count direction does not prevent individual Affixes having tier caps.

## Foundation already implemented

- Reinforced and Swift Aspects with configurable levels and bonuses.
- Executioner earned through equipment use, with configurable progression and effects.
- Aspect acquisition from selected structure loot tables.
- Central gameplay configuration and a maintained TOML reference.
- Saved equipment history and preservation through supported upgrades and repairs.
- Tooltips and the GuideME in-game guide.

The original design allowed crafted or looted Aspects. The later decision is
**loot acquisition only**, so Aspect crafting is not missing work.

## Decisions before implementation

Define the workstation's first operation, survival acquisition, inputs, costs,
output and inventory behavior. Specify the risk formula for adding versus
upgrading Attributes, how it remains below 100%, and what a failed attempt consumes.
Decide how existing deterministic smithing fits alongside the new mechanic.

Rerolling and removal need explicit rules for eligible data, resulting levels,
retained history and costs. Additional Attributes and Affixes need their own
effects, activities and compatibility rules. The original description did not
specify these details; plan them as separate implementable slices.
