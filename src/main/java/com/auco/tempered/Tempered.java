package com.auco.tempered;

import com.auco.tempered.registry.ModCreativeTabs;
import com.auco.tempered.registry.ModItems;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Tempered.MODID)
public class Tempered {

    public static final String MODID = "tempered";

    public Tempered(IEventBus modEventBus) {
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
    }
}
