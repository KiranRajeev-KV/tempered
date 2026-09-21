package com.auco.tempered.event;

import com.auco.tempered.Tempered;
import com.auco.tempered.config.TemperedConfig;
import com.auco.tempered.network.ActiveSettingsPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Tempered.MODID)
public final class SettingsSyncHandler {
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new ActiveSettingsPayload(TemperedConfig.active()));
        }
    }
    private SettingsSyncHandler() {}
}
