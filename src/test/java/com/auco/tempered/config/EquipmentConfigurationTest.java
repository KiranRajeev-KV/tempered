package com.auco.tempered.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import com.auco.tempered.config.aspect.ReinforcedSettings;
import com.auco.tempered.config.aspect.SwiftSettings;
import com.auco.tempered.config.affix.ExecutionerSettings;
import com.auco.tempered.equipment.attribute.reinforced.ReinforcedData;
import com.auco.tempered.equipment.affix.executioner.ExecutionerData;
import com.auco.tempered.network.ActiveSettingsPayload;
import com.auco.tempered.recipe.aspect.AspectSmithingRecipe;
import com.auco.tempered.registry.ModDataComponents;
import com.auco.tempered.registry.ModItems;
import com.auco.tempered.service.ReinforcementService;
import com.auco.tempered.service.SwiftService;
import com.auco.tempered.service.ExecutionerService;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Tests real registered items, tags, codecs and server config loading. */
@ExtendWith(EphemeralTestServerProvider.class)
class EquipmentConfigurationTest {
    @Test
    void mismatchedTierListsFallBackToTheCompleteSectionAndUnloadingResetsSettings(MinecraftServer server) throws Exception {
        server.submit(() -> {
            var before = TemperedConfig.active();
            var config = ModConfigs.getFileMap().get(TemperedConfig.FILE_NAME);
            var raw = config.getLoadedConfig().config();
            String key = "affixes.executioner.kills_required_by_tier";
            Object previous = raw.get(key);
            ModConfigSpec.ConfigValue<?> value = TemperedConfig.SPEC.getValues().get(key);
            try {
                raw.set(key, List.of(64, 192));
                // Model the cleared cache at the next world load, without saving edits to disk.
                value.clearCache();
                ConfigLifecycle.onLoading(new ModConfigEvent.Loading(config));
                assertEquals(ExecutionerSettings.DEFAULT, TemperedConfig.active().executioner());
                ConfigLifecycle.onUnloading(new ModConfigEvent.Unloading(config));
                assertEquals(GameplaySettings.DEFAULT, TemperedConfig.active());
            } finally {
                raw.set(key, previous);
                value.clearCache();
                TemperedConfig.activate(before);
            }
        }).get();
    }

    @Test
    void activeSettingsPacketRoundTripsIndependentValues(MinecraftServer server) throws Exception {
        server.submit(() -> {
            var settings = new GameplaySettings(
                    new ReinforcedSettings(false, List.of(3.0, 17.5)),
                    new SwiftSettings(true, List.of(2.0)),
                    new ExecutionerSettings(true, false, List.of(64, 192), List.of(4.5, 20.0),
                            0.25, 3, List.of(0.1, 0.75), true, false));
            var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), server.registryAccess());
            try {
                ActiveSettingsPayload.STREAM_CODEC.encode(buffer, new ActiveSettingsPayload(settings));
                assertEquals(settings, ActiveSettingsPayload.STREAM_CODEC.decode(buffer).settings());
                assertEquals(0, buffer.readableBytes());
            } finally {
                buffer.release();
            }
        }).get();
    }

    @Test
    void registeredConfigLoadsAndPendingChangesDoNotChangeActiveSettings(MinecraftServer server) throws Exception {
        server.submit(() -> {
            assertTrue(TemperedConfig.SPEC.isLoaded());
            var config = ModConfigs.getFileMap().get(TemperedConfig.FILE_NAME);
            assertNotNull(config);
            var active = TemperedConfig.active();
            var raw = config.getLoadedConfig().config();
            Object previous = raw.get("aspects.swift.enabled");
            try {
                raw.set("aspects.swift.enabled", !active.swift().enabled());
                ConfigLifecycle.onReloading(new ModConfigEvent.Reloading(config));
                assertEquals(active, TemperedConfig.active());
                // This is the snapshot sent on login, even if the TOML now contains pending edits.
                var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), server.registryAccess());
                try {
                    ActiveSettingsPayload.STREAM_CODEC.encode(buffer, new ActiveSettingsPayload(active));
                    assertEquals(active, ActiveSettingsPayload.STREAM_CODEC.decode(buffer).settings());
                } finally {
                    buffer.release();
                }
            } finally {
                raw.set("aspects.swift.enabled", previous);
            }
        }).get();
    }

    @Test
    void smithingPreservesInputAndRejectsDisabledOrCappedMechanics(MinecraftServer server) throws Exception {
        server.submit(() -> {
            var before = TemperedConfig.active();
            try {
                TemperedConfig.activate(GameplaySettings.DEFAULT);
                ItemStack tool = new ItemStack(Items.IRON_PICKAXE);
                tool.setDamageValue(80);
                tool.set(DataComponents.CUSTOM_NAME, Component.literal("Reliable pick"));
                var input = new SmithingRecipeInput(ItemStack.EMPTY, tool, new ItemStack(ModItems.SWIFT_ASPECT.get()));
                var recipe = new AspectSmithingRecipe();
                ItemStack result = recipe.assemble(input, server.registryAccess());
                assertFalse(result.isEmpty());
                assertEquals(80, result.getDamageValue());
                assertEquals(tool.getHoverName(), result.getHoverName());
                assertNotNull(SwiftService.getActiveData(result));
                assertNull(SwiftService.getActiveData(tool));

                var disabled = new SwiftSettings(false, List.of(8.0));
                TemperedConfig.activate(new GameplaySettings(before.reinforced(), disabled, before.executioner()));
                assertTrue(recipe.assemble(input, server.registryAccess()).isEmpty());
                assertFalse(SwiftService.inspect(result).isSuccess());
                assertNotNull(result.get(ModDataComponents.SWIFT_DATA.get()));

                TemperedConfig.activate(new GameplaySettings(before.reinforced(), new SwiftSettings(true, List.of(8.0)), before.executioner()));
                assertFalse(SwiftService.inspect(result).isSuccess());
                assertTrue(recipe.assemble(new SmithingRecipeInput(ItemStack.EMPTY, result,
                        new ItemStack(ModItems.SWIFT_ASPECT.get())), server.registryAccess()).isEmpty());
                assertTrue(recipe.assemble(new SmithingRecipeInput(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        tool, new ItemStack(ModItems.SWIFT_ASPECT.get())), server.registryAccess()).isEmpty());
            } finally {
                TemperedConfig.activate(before);
            }
        }).get();
    }

    @Test
    void existingReinforcementRebalancesAndSurvivesAnItemSave(MinecraftServer server) throws Exception {
        server.submit(() -> {
            var before = TemperedConfig.active();
            try {
                TemperedConfig.activate(GameplaySettings.DEFAULT);
                ItemStack tool = new ItemStack(Items.IRON_PICKAXE);
                var history = new ReinforcedData(5, 1000);
                tool.set(ModDataComponents.REINFORCED_DATA.get(), history);
                tool.set(DataComponents.MAX_DAMAGE, 1500);
                tool.setDamageValue(750);

                TemperedConfig.activate(new GameplaySettings(new ReinforcedSettings(false, List.of(10.0)),
                        before.swift(), before.executioner()));
                ReinforcementService.reconcile(tool);
                assertEquals(1000, tool.getMaxDamage());
                assertEquals(500, tool.getDamageValue());
                assertEquals(history, tool.get(ModDataComponents.REINFORCED_DATA.get()));
                assertNull(ReinforcementService.getActiveData(tool));
                assertFalse(ReinforcementService.inspect(tool).isSuccess());
                ReinforcementService.reconcile(tool);
                assertEquals(500, tool.getDamageValue());

                TemperedConfig.activate(GameplaySettings.DEFAULT);
                ReinforcementService.reconcile(tool);
                assertEquals(1500, tool.getMaxDamage());
                assertEquals(750, tool.getDamageValue());

                var ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
                ItemStack restored = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, tool).getOrThrow()).getOrThrow();
                assertEquals(history, restored.get(ModDataComponents.REINFORCED_DATA.get()));
                assertEquals(1500, restored.getMaxDamage());
                assertEquals(750, restored.getDamageValue());
            } finally {
                TemperedConfig.activate(before);
            }
        }).get();
    }

    @Test
    void executionerProgressUsesConfiguredAmountAndRetainsHistoryWhenDisabled(MinecraftServer server) throws Exception {
        server.submit(() -> {
            var before = TemperedConfig.active();
            try {
                var defaults = ExecutionerSettings.DEFAULT;
                var settings = new ExecutionerSettings(true, true, List.of(2, 5, 10), defaults.healthPercents(),
                        1, 3, defaults.executeChances(), false, true);
                TemperedConfig.activate(new GameplaySettings(before.reinforced(), before.swift(), settings));
                ItemStack weapon = new ItemStack(Items.IRON_SWORD);
                assertEquals(3, ExecutionerService.recordHostileKill(weapon).hostileKills());
                weapon.set(ModDataComponents.EXECUTIONER_DATA.get(), new ExecutionerData(Integer.MAX_VALUE - 1));
                assertEquals(Integer.MAX_VALUE, ExecutionerService.recordHostileKill(weapon).hostileKills());
                var disabled = new ExecutionerSettings(false, true, settings.killsRequired(), settings.healthPercents(),
                        1, 3, settings.executeChances(), false, true);
                TemperedConfig.activate(new GameplaySettings(before.reinforced(), before.swift(), disabled));
                assertNull(ExecutionerService.recordHostileKill(weapon));
                assertNull(ExecutionerService.getActiveData(weapon));
                assertEquals(Integer.MAX_VALUE, weapon.get(ModDataComponents.EXECUTIONER_DATA.get()).hostileKills());
            } finally {
                TemperedConfig.activate(before);
            }
        }).get();
    }
}
