package com.auco.tempered.network;

import com.auco.tempered.Tempered;
import com.auco.tempered.config.TemperedConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = Tempered.MODID)
public final class SettingsNetworking {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("3").playToClient(ActiveSettingsPayload.TYPE, ActiveSettingsPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> TemperedConfig.activate(payload.settings())));
    }
    private SettingsNetworking() {}
}
