package com.auco.tempered.registry;

import com.auco.tempered.Tempered;
import com.auco.tempered.loot.AspectLootModifier;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModLootModifiers {
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Tempered.MODID);

    static {
        SERIALIZERS.register("aspect", () -> AspectLootModifier.CODEC);
    }

    public static void register(IEventBus eventBus) { SERIALIZERS.register(eventBus); }

    private ModLootModifiers() {}
}
