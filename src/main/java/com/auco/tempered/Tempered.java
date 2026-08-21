package com.auco.tempered;

import com.auco.tempered.registry.ModCreativeTabs;
import com.auco.tempered.registry.ModDataComponents;
import com.auco.tempered.registry.ModItems;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * NeoForge discovers this class from {@link Mod}. Its constructor receives the
 * mod event bus, which is where registry objects must be registered.
 */
@Mod(Tempered.MODID)
public class Tempered {

    public static final String MODID = "tempered";

    public Tempered(IEventBus modEventBus) {
        // Deferred registers collect entries during class loading. Attaching
        // them here lets NeoForge register those entries at the correct time.
        ModDataComponents.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
    }
}
