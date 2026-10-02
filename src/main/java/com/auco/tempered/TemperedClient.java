package com.auco.tempered;

import com.auco.tempered.config.TemperedClientConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Registers local visual settings and the native configuration screen on clients. */
@Mod(value = Tempered.MODID, dist = Dist.CLIENT)
public final class TemperedClient {
    public TemperedClient(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, TemperedClientConfig.SPEC, TemperedClientConfig.FILE_NAME);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
