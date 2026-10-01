package com.auco.tempered.presentation;

import static com.auco.tempered.presentation.GuideText.text;

import java.util.ArrayList;
import java.util.List;
import com.auco.tempered.config.GameplaySettings;
import com.auco.tempered.config.acquisition.AspectLootSettings;
import net.minecraft.network.chat.Component;

/** All configured rows from one captured snapshot; static prose belongs in Markdown resources. */
public final class GuideSettingsContent {
    public static List<Component> build(GuideTopic topic, GameplaySettings settings) {
        List<Component> lines = new ArrayList<>();
        switch (topic) {
            case REINFORCED -> addAspect(lines, topic, settings.reinforced().enabled(), settings.reinforced().bonuses());
            case SWIFT -> addAspect(lines, topic, settings.swift().enabled(), settings.swift().bonuses());
            case EXECUTIONER -> {
                var executioner = settings.executioner();
                if (!executioner.enabled()) lines.add(text("mechanic.disabled", topic.title()));
                if (!DisplayValues.progressionAvailable(executioner)) lines.add(text("executioner.paused"));
                lines.add(text("executioner.creative." + (executioner.allowCreativeProgression() ? "yes" : "no")));
                lines.add(text("executioner.award", DisplayValues.chance(executioner.progressChance()), executioner.progressPerSuccess()));
                for (int i = 0; i < executioner.maxLevel(); i++) {
                    lines.add(text("executioner.tier", DisplayValues.level(i + 1), executioner.killsRequired().get(i),
                            DisplayValues.progressUnit(executioner), DisplayValues.number(executioner.healthPercents().get(i)),
                            DisplayValues.chance(executioner.executeChances().get(i))));
                }
            }
            case LOOT -> {
                addLoot(lines, GuideTopic.REINFORCED, settings.reinforced().enabled(), settings.acquisition().reinforced());
                addLoot(lines, GuideTopic.SWIFT, settings.swift().enabled(), settings.acquisition().swift());
            }
            case OVERVIEW, SMITHING -> throw new IllegalArgumentException("Topic has no configured section: " + topic);
        }
        return List.copyOf(lines);
    }

    private static void addAspect(List<Component> lines, GuideTopic topic, boolean enabled, List<Double> bonuses) {
        if (!enabled) lines.add(text("mechanic.disabled", topic.title()));
        lines.add(text("aspect.cap", DisplayValues.level(bonuses.size())));
        for (int i = 0; i < bonuses.size(); i++) {
            lines.add(text(topic.id() + ".level", DisplayValues.level(i + 1), DisplayValues.number(bonuses.get(i))));
        }
    }

    private static void addLoot(List<Component> lines, GuideTopic topic, boolean mechanicEnabled, AspectLootSettings loot) {
        lines.add(topic.title());
        if (!mechanicEnabled) {
            lines.add(text("mechanic.disabled", topic.title()));
        } else if (!loot.enabled() || loot.chance() == 0 || loot.lootTables().isEmpty()) {
            lines.add(text("loot.unavailable"));
        } else {
            lines.add(text("loot.roll", DisplayValues.chance(loot.chance()), loot.minCount(), loot.maxCount()));
        }
        // Configured IDs do not prove that a loot table exists in the current datapack stack.
        if (!loot.lootTables().isEmpty()) {
            lines.add(text("loot.sources"));
            loot.lootTables().stream().sorted().forEach(id -> lines.add(LootSourceNames.name(id)));
        }
    }

    private GuideSettingsContent() {}
}
