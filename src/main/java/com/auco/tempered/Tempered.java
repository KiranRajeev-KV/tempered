package com.auco.tempered;

import com.auco.tempered.registry.ModCreativeTabs;
import com.auco.tempered.registry.ModDataComponents;
import com.auco.tempered.registry.ModItems;
import com.auco.tempered.registry.ModRecipeSerializers;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import com.auco.tempered.config.TemperedConfig;

/**
 * NeoForge discovers this class from {@link Mod}. Its constructor receives the
 * mod event bus, which is where registry objects must be registered.
 */
@Mod(Tempered.MODID)
public class Tempered {

    public static final String MODID = "tempered";

    public Tempered(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, TemperedConfig.SPEC, TemperedConfig.FILE_NAME);
        // Deferred registers collect entries during class loading. Attaching
        // them here lets NeoForge register those entries at the correct time.
        ModDataComponents.register(modEventBus);
        ModItems.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
    }
}
