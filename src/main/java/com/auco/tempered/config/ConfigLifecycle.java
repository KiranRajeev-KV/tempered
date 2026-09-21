package com.auco.tempered.config;

import com.auco.tempered.Tempered;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = Tempered.MODID)
public final class ConfigLifecycle {
    @SubscribeEvent
    public static void onLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == TemperedConfig.SPEC) TemperedConfig.load();
    }

    @SubscribeEvent
    public static void onReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == TemperedConfig.SPEC) {
            LogUtils.getLogger().info("Tempered config changed. Gameplay settings take effect after world/server restart.");
        }
    }

    @SubscribeEvent
    public static void onUnloading(ModConfigEvent.Unloading event) {
        if (event.getConfig().getSpec() == TemperedConfig.SPEC) TemperedConfig.activate(GameplaySettings.DEFAULT);
    }

    private ConfigLifecycle() {}
}
