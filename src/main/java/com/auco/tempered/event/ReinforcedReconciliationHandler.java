package com.auco.tempered.event;

import com.auco.tempered.Tempered;
import com.auco.tempered.service.ReinforcementService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Reconcile before player work; unloaded storage is left alone until brought into use. */
@EventBusSubscriber(modid = Tempered.MODID)
public final class ReinforcedReconciliationHandler {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ReinforcementService.reconcile(inventory.getItem(slot));
        }
        // Include a stack currently held by the mouse in an open menu.
        ReinforcementService.reconcile(player.containerMenu.getCarried());
    }
    private ReinforcedReconciliationHandler() {}
}
