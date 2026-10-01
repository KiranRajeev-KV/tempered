package com.auco.tempered.presentation;

import java.util.ArrayList;
import java.util.List;
import com.auco.tempered.config.GameplaySettings;
import com.auco.tempered.registry.ModDataComponents;
import com.auco.tempered.registry.ModItems;
import com.auco.tempered.service.ExecutionerService;
import com.auco.tempered.service.ReinforcementService;
import com.auco.tempered.service.SwiftService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

/** Read-only presentation of stack history, eligibility and a captured settings snapshot. */
public final class EquipmentTooltipContent {
    public static List<Component> build(ItemStack stack, GameplaySettings settings, boolean expanded) {
        List<Component> lines = new ArrayList<>();
        if (stack.isEmpty()) return List.of();
        if (stack.is(ModItems.REINFORCED_ASPECT.get())) {
            aspect(lines, GuideTopic.REINFORCED, settings.reinforced().enabled(), settings.reinforced().bonuses(), expanded);
        } else if (stack.is(ModItems.SWIFT_ASPECT.get())) {
            aspect(lines, GuideTopic.SWIFT, settings.swift().enabled(), settings.swift().bonuses(), expanded);
        } else {
            reinforced(lines, stack, settings, expanded);
            swift(lines, stack, settings, expanded);
            executioner(lines, stack, settings, expanded);
            if (expanded) upgrades(lines, stack, settings);
            if (!lines.isEmpty() || ReinforcementService.isApplicable(stack) || SwiftService.isApplicable(stack)) {
                detailsHint(lines, expanded);
            }
        }
        return List.copyOf(lines);
    }

    private static void aspect(List<Component> lines, GuideTopic topic, boolean enabled, List<Double> bonuses, boolean expanded) {
        lines.add(GuideText.text(topic.id() + ".description").withStyle(ChatFormatting.BLUE));
        lines.add(GuideText.text(topic.id() + ".eligibility").withStyle(ChatFormatting.GRAY));
        if (enabled) lines.add(text("aspect.smithing"));
        else lines.add(GuideText.text("mechanic.disabled", topic.title()).withStyle(ChatFormatting.DARK_GRAY));
        if (expanded) {
            lines.addAll(GuideText.smithingSteps());
            lines.add(GuideText.text("aspect.cap", DisplayValues.level(bonuses.size())));
            lines.add(GuideText.text(topic.id() + ".level", "I", DisplayValues.number(bonuses.getFirst())));
            if (bonuses.size() > 1) lines.add(GuideText.text(topic.id() + ".level",
                    DisplayValues.level(bonuses.size()), DisplayValues.number(bonuses.getLast())));
        }
        detailsHint(lines, expanded);
    }

    private static void reinforced(List<Component> lines, ItemStack stack, GameplaySettings settings, boolean expanded) {
        var data = stack.get(ModDataComponents.REINFORCED_DATA.get());
        if (data == null) return;
        if (!data.isValid()) {
            lines.add(text("invalid_data", GuideTopic.REINFORCED.title()));
            return;
        }
        var reinforced = settings.reinforced();
        boolean active = reinforced.enabled() && ReinforcementService.isApplicable(stack);
        lines.add(text("reinforced.title", DisplayValues.level(active ? reinforced.effectiveLevel(data.level()) : data.level()))
                .withStyle(ChatFormatting.AQUA));
        if (active) {
            lines.add(text("reinforced.durability", DisplayValues.number(reinforced.bonusPercent(data.level())))
                    .withStyle(ChatFormatting.BLUE));
            storedLevel(lines, data.level(), reinforced.maxLevel(), expanded);
        } else {
            lines.add(text(reinforced.enabled() ? "inactive_incompatible" : "inactive_disabled").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static void swift(List<Component> lines, ItemStack stack, GameplaySettings settings, boolean expanded) {
        var data = stack.get(ModDataComponents.SWIFT_DATA.get());
        if (data == null) return;
        if (!data.isValid()) {
            lines.add(text("invalid_data", GuideTopic.SWIFT.title()));
            return;
        }
        var swift = settings.swift();
        boolean active = swift.enabled() && SwiftService.isApplicable(stack);
        lines.add(text("swift.title", DisplayValues.level(active ? swift.effectiveLevel(data.level()) : data.level()))
                .withStyle(ChatFormatting.GOLD));
        if (active) {
            lines.add(text("swift.mining_speed", DisplayValues.number(swift.bonusPercent(data.level())))
                    .withStyle(ChatFormatting.BLUE));
            storedLevel(lines, data.level(), swift.maxLevel(), expanded);
        } else {
            lines.add(text(swift.enabled() ? "inactive_incompatible" : "inactive_disabled").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static void storedLevel(List<Component> lines, int saved, int cap, boolean expanded) {
        if (expanded && saved > cap) lines.add(text("stored_level", DisplayValues.level(saved)).withStyle(ChatFormatting.GRAY));
    }

    private static void executioner(List<Component> lines, ItemStack stack, GameplaySettings settings, boolean expanded) {
        var data = stack.get(ModDataComponents.EXECUTIONER_DATA.get());
        boolean eligible = ExecutionerService.isApplicableWeapon(stack);
        if (data != null && !data.isValid()) {
            lines.add(text("invalid_data", GuideTopic.EXECUTIONER.title()));
            return;
        }
        var executioner = settings.executioner();
        if (!eligible || !executioner.enabled()) {
            if (data != null) lines.add(text("executioner.inactive", data.hostileKills()).withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        int progress = data == null ? 0 : data.hostileKills();
        int level = executioner.levelForKills(progress);
        if (level == 0) {
            lines.add(text("executioner.awakening").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            lines.add(text("executioner.title", DisplayValues.level(level)).withStyle(ChatFormatting.DARK_RED));
            if (executioner.healthPercent(progress) > 0 && executioner.executeChance(progress) > 0) {
                lines.add(text("executioner.effect", DisplayValues.number(executioner.healthPercent(progress)))
                        .withStyle(ChatFormatting.RED));
                lines.add(text("executioner.chance", DisplayValues.chance(executioner.executeChance(progress)))
                        .withStyle(ChatFormatting.GRAY));
            } else {
                lines.add(text("executioner.no_execution").withStyle(ChatFormatting.GRAY));
            }
        }
        if (level < executioner.maxLevel()) {
            lines.add(text("executioner.progress_unit", progress, executioner.nextMilestone(progress),
                    DisplayValues.progressUnit(executioner)).withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(text("executioner.max", progress, DisplayValues.progressUnit(executioner)).withStyle(ChatFormatting.GRAY));
        }
        if (!DisplayValues.progressionAvailable(executioner)) {
            lines.add(GuideText.text("executioner.paused").withStyle(ChatFormatting.DARK_GRAY));
        }
        if (expanded) {
            lines.add(text("executioner.melee"));
            lines.add(GuideText.text("executioner.award", DisplayValues.chance(executioner.progressChance()),
                    executioner.progressPerSuccess()));
        }
    }

    private static void upgrades(List<Component> lines, ItemStack stack, GameplaySettings settings) {
        if (ReinforcementService.isApplicable(stack) || stack.has(ModDataComponents.REINFORCED_DATA.get())) {
            var result = ReinforcementService.inspect(stack, settings.reinforced());
            lines.add(upgrade(GuideTopic.REINFORCED, switch (result.status()) {
                case SUCCESS -> "next";
                case MAX_LEVEL -> "max";
                case DISABLED -> "disabled";
                case NOT_DAMAGEABLE -> "incompatible";
                case INVALID_DATA -> "invalid";
            }, result.newLevel()));
        }
        if (SwiftService.isApplicable(stack) || stack.has(ModDataComponents.SWIFT_DATA.get())) {
            var result = SwiftService.inspect(stack, settings.swift());
            lines.add(upgrade(GuideTopic.SWIFT, switch (result.status()) {
                case SUCCESS -> "next";
                case MAX_LEVEL -> "max";
                case DISABLED -> "disabled";
                case NOT_SWIFT_APPLICABLE -> "incompatible";
                case INVALID_DATA -> "invalid";
            }, result.newLevel()));
        }
    }

    private static Component upgrade(GuideTopic topic, String status, int nextLevel) {
        return text("upgrade." + status, topic.title(), DisplayValues.level(nextLevel)).withStyle(ChatFormatting.GRAY);
    }

    private static void detailsHint(List<Component> lines, boolean expanded) {
        lines.add(text(expanded ? "guide" : "hold_shift").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static MutableComponent text(String key, Object... arguments) {
        return Component.translatable("tooltip.tempered." + key, arguments);
    }

    private EquipmentTooltipContent() {}
}
