package com.auco.tempered.event;

import com.auco.tempered.Tempered;
import com.auco.tempered.equipment.attribute.swift.SwiftData;
import com.auco.tempered.service.SwiftService;

import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Applies a Swift tool's bonus when Minecraft calculates block-breaking speed. */
@EventBusSubscriber(modid = Tempered.MODID)
public final class SwiftMiningSpeedHandler {

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        ItemStack miningTool = event.getEntity().getMainHandItem();
        SwiftData swiftData = SwiftService.getActiveData(miningTool);
        if (swiftData == null) {
            return;
        }

        // getNewSpeed includes vanilla and earlier mod adjustments. Multiplying
        // it makes Swift compose with effects such as Haste and Efficiency
        // instead of replacing the player's calculated mining speed.
        event.setNewSpeed(event.getNewSpeed() * swiftData.miningSpeedMultiplier());
    }

    private SwiftMiningSpeedHandler() {
    }
}
