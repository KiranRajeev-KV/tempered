package com.auco.tempered.event;

import com.auco.tempered.Tempered;
import com.auco.tempered.equipment.affix.executioner.ExecutionerData;
import com.auco.tempered.equipment.affix.executioner.ExecutionerRules;
import com.auco.tempered.equipment.attribute.reinforced.ReinforcedData;
import com.auco.tempered.equipment.attribute.reinforced.ReinforcedRules;
import com.auco.tempered.equipment.attribute.swift.SwiftData;
import com.auco.tempered.equipment.attribute.swift.SwiftRules;
import com.auco.tempered.service.ExecutionerService;
import com.auco.tempered.service.SwiftService;
import com.auco.tempered.service.ReinforcementService;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Adds client-only Aspect information to vanilla item tooltips.
 *
 * <p>The component is synchronized with the ItemStack, so the client can read
 * it directly without sending a custom network packet.</p>
 */
@EventBusSubscriber(
        modid = Tempered.MODID,
        value = Dist.CLIENT
)
public final class ItemTooltipHandler {

    /**
     * The item name is always the first tooltip line. Inserting directly after
     * it gives an Aspect the same prominence as other stack-specific state,
     * while leaving vanilla enchantment, attribute, and durability sections
     * in their normal order.
     */
    private static final int REINFORCEMENT_TOOLTIP_INDEX = 1;

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        int insertIndex = REINFORCEMENT_TOOLTIP_INDEX;

        ReinforcedData reinforcedData = ReinforcementService.getActiveData(stack);
        if (reinforcedData != null && reinforcedData.isValid()) {
            insertIndex = addReinforcedTooltip(event, reinforcedData, insertIndex);
        }

        SwiftData swiftData = SwiftService.getActiveData(stack);
        if (swiftData != null) {
            insertIndex = addSwiftTooltip(event, swiftData, insertIndex);
        }

        ExecutionerData executionerData = ExecutionerService.getProgressData(stack);
        if (executionerData != null) {
            addExecutionerTooltip(event, executionerData, insertIndex);
        }
    }

    private static int addReinforcedTooltip(
            ItemTooltipEvent event,
            ReinforcedData reinforcedData,
            int insertIndex
    ) {
        event.getToolTip().add(
                safeInsertionIndex(event, insertIndex),
                Component.translatable(
                        "tooltip.tempered.reinforced.title",
                        toRomanNumeral(ReinforcedRules.level(reinforcedData))
                )
                        .withStyle(ChatFormatting.AQUA)
        );

        event.getToolTip().add(
                safeInsertionIndex(event, insertIndex + 1),
                Component.translatable(
                        "tooltip.tempered.reinforced.durability",
                        ReinforcedRules.bonusPercent(reinforcedData)
                ).withStyle(ChatFormatting.BLUE)
        );

        return insertIndex + 2;
    }

    private static int addSwiftTooltip(
            ItemTooltipEvent event,
            SwiftData swiftData,
            int insertIndex
    ) {
        event.getToolTip().add(
                safeInsertionIndex(event, insertIndex),
                Component.translatable(
                        "tooltip.tempered.swift.title",
                        toRomanNumeral(SwiftRules.level(swiftData))
                ).withStyle(ChatFormatting.GOLD)
        );

        event.getToolTip().add(
                safeInsertionIndex(event, insertIndex + 1),
                Component.translatable(
                        "tooltip.tempered.swift.mining_speed",
                        SwiftRules.bonusPercent(swiftData)
                ).withStyle(ChatFormatting.BLUE)
        );

        return insertIndex + 2;
    }

    private static void addExecutionerTooltip(
            ItemTooltipEvent event,
            ExecutionerData executionerData,
            int insertIndex
    ) {
        if (!ExecutionerRules.unlocked(executionerData)) {
            event.getToolTip().add(
                    safeInsertionIndex(event, insertIndex),
                    Component.translatable("tooltip.tempered.executioner.awakening")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            event.getToolTip().add(
                    safeInsertionIndex(event, insertIndex + 1),
                    Component.translatable(
                            "tooltip.tempered.executioner.progress",
                            executionerData.hostileKills(),
                            ExecutionerRules.nextMilestone(executionerData)
                    ).withStyle(ChatFormatting.GRAY)
            );
            return;
        }

        event.getToolTip().add(
                safeInsertionIndex(event, insertIndex),
                Component.translatable(
                        "tooltip.tempered.executioner.title",
                        toRomanNumeral(ExecutionerRules.level(executionerData))
                ).withStyle(ChatFormatting.DARK_RED)
        );
        event.getToolTip().add(
                safeInsertionIndex(event, insertIndex + 1),
                Component.translatable(
                        "tooltip.tempered.executioner.effect",
                        ExecutionerRules.healthPercent(executionerData)
                ).withStyle(ChatFormatting.RED)
        );

        double chance = ExecutionerRules.executeChance(executionerData);
        if (chance < 1) {
            event.getToolTip().add(
                    safeInsertionIndex(event, insertIndex + 2),
                    Component.translatable("tooltip.tempered.executioner.chance", chance * 100)
                            .withStyle(ChatFormatting.GRAY)
            );
            insertIndex++;
        }

        if (ExecutionerRules.level(executionerData) < ExecutionerRules.maxLevel()) {
            event.getToolTip().add(
                    safeInsertionIndex(event, insertIndex + 2),
                    Component.translatable(
                            "tooltip.tempered.executioner.progress",
                            executionerData.hostileKills(),
                            ExecutionerRules.nextMilestone(executionerData)
                    ).withStyle(ChatFormatting.GRAY)
            );
        }
    }

    private static int safeInsertionIndex(ItemTooltipEvent event, int requestedIndex) {
        return Math.min(requestedIndex, event.getToolTip().size());
    }

    /**
     * Minecraft represents tiered effects with Roman numerals (for example,
     * enchantments and potion effects), so using them keeps this tooltip
     * familiar to players.
     */
    private static String toRomanNumeral(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(level);
        };
    }

    private ItemTooltipHandler() {
    }
}
