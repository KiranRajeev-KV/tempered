package com.auco.tempered.event;

import com.auco.tempered.Tempered;
import com.auco.tempered.equipment.attribute.reinforced.ReinforcedData;
import com.auco.tempered.registry.ModDataComponents;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Adds client-only information to the vanilla tooltip for reinforced stacks.
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

        // Most stacks never receive this component, so exit before doing any
        // tooltip allocation work.
        if (!stack.has(ModDataComponents.REINFORCED_DATA.get())) {
            return;
        }

        ReinforcedData reinforcedData = stack.get(
                ModDataComponents.REINFORCED_DATA.get()
        );

        if (reinforcedData == null) {
            return;
        }

        if (!reinforcedData.isValid()) {
            // Do not display misleading progress for edited/corrupt data.
            return;
        }

        // ItemTooltipEvent is fired after vanilla has assembled the tooltip.
        // Insert our compact two-line section beneath the item name instead of
        // appending it after vanilla's unrelated details.
        event.getToolTip().add(
                Math.min(REINFORCEMENT_TOOLTIP_INDEX, event.getToolTip().size()),
                Component.translatable(
                        "tooltip.tempered.reinforced.title",
                        toRomanNumeral(reinforcedData.level())
                )
                        .withStyle(ChatFormatting.AQUA)
        );

        event.getToolTip().add(
                Math.min(REINFORCEMENT_TOOLTIP_INDEX + 1, event.getToolTip().size()),
                Component.translatable(
                        "tooltip.tempered.reinforced.durability",
                        reinforcedData.durabilityBonusPercent()
                ).withStyle(ChatFormatting.BLUE)
        );
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
