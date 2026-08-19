package com.auco.tempered.event;

import com.auco.tempered.Tempered;
import com.auco.tempered.attributes.ReinforcedData;
import com.auco.tempered.registry.ModDataComponents;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(
        modid = Tempered.MODID,
        value = Dist.CLIENT
)
public final class ItemTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        // Normal items have no Reinforced component.
        if (!stack.has(ModDataComponents.REINFORCED_DATA.get())) {
            return;
        }

        ReinforcedData reinforcedData = stack.get(
                ModDataComponents.REINFORCED_DATA.get()
        );

        if (reinforcedData == null) {
            return;
        }

        int level = reinforcedData.level();
        int durabilityBonusPercent = level * 10;

        event.getToolTip().add(
                Component.literal("Reinforced " + getRomanLevel(level))
                        .withStyle(ChatFormatting.AQUA)
        );

        event.getToolTip().add(
                Component.literal(
                        "+" + durabilityBonusPercent + "% Durability"
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