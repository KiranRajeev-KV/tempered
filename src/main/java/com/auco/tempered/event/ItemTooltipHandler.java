package com.auco.tempered.event;

import com.auco.tempered.Tempered;
import com.auco.tempered.config.TemperedConfig;
import com.auco.tempered.presentation.EquipmentTooltipContent;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Adds read-only guidance after the name, preserving the order of vanilla tooltip sections. */
@EventBusSubscriber(modid = Tempered.MODID, value = Dist.CLIENT)
public final class ItemTooltipHandler {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        // Search indexing generates tooltips without a player: do not read keyboard state there.
        boolean expanded = event.getEntity() != null && Screen.hasShiftDown();
        var lines = EquipmentTooltipContent.build(event.getItemStack(), TemperedConfig.active(), expanded);
        event.getToolTip().addAll(Math.min(1, event.getToolTip().size()), lines);
    }

    private ItemTooltipHandler() {}
}
