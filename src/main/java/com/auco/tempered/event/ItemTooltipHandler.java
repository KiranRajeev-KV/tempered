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

        event.getToolTip().add(
                Component.translatable(
                        "tooltip.tempered.reinforced.level",
                        getRomanLevel(reinforcedData.level())
                )
                        .withStyle(ChatFormatting.AQUA)
        );

        event.getToolTip().add(
                Component.translatable(
                        "tooltip.tempered.reinforced.durability",
                        reinforcedData.durabilityBonusPercent()
                ).withStyle(ChatFormatting.GRAY)
        );
    }

    private static String getRomanLevel(int level) {
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
