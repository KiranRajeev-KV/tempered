package com.auco.tempered.equipment;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import com.auco.tempered.config.GameplaySettings;
import com.auco.tempered.config.TemperedConfig;
import com.auco.tempered.config.aspect.ReinforcedSettings;
import com.auco.tempered.equipment.attribute.reinforced.ReinforcedData;
import com.auco.tempered.equipment.attribute.swift.SwiftData;
import com.auco.tempered.equipment.affix.executioner.ExecutionerData;
import com.auco.tempered.registry.ModDataComponents;
import com.auco.tempered.service.ReinforcementService;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Literal old data catches compatibility regressions that self-round-trip tests cannot. */
@ExtendWith(EphemeralTestServerProvider.class)
class SavedEquipmentCompatibilityTest {
    @Test
    void legacyHistorySurvivesBalanceChangesSavingAndNetworkAndInvalidVersionsFail(MinecraftServer server) throws Exception {
        String fixture;
        try (var stream = getClass().getResourceAsStream("/equipment/legacy-diamond-axe.snbt")) {
            assertNotNull(stream);
            fixture = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        server.submit(() -> {
            var before = TemperedConfig.active();
            var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), server.registryAccess());
            try {
                var ops = server.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                ItemStack stack = ItemStack.CODEC.parse(ops, TagParser.parseTag(fixture)).getOrThrow();
                var history = new ReinforcedData(12, 1561);
                assertEquals(history, stack.get(ModDataComponents.REINFORCED_DATA.get()));
                assertEquals(new SwiftData(12), stack.get(ModDataComponents.SWIFT_DATA.get()));
                assertEquals(new ExecutionerData(10000), stack.get(ModDataComponents.EXECUTIONER_DATA.get()));

                TemperedConfig.activate(new GameplaySettings(new ReinforcedSettings(false, List.of(1.0)),
                        before.swift(), before.executioner()));
                ReinforcementService.reconcile(stack);
                assertEquals(1561, stack.getMaxDamage());
                assertEquals(781, stack.getDamageValue());
                assertEquals(history, stack.get(ModDataComponents.REINFORCED_DATA.get()));
                TemperedConfig.activate(GameplaySettings.DEFAULT);
                ReinforcementService.reconcile(stack);
                assertEquals(2342, stack.getMaxDamage());
                assertTrue(Math.abs(stack.getDamageValue() - 1171) <= 1, "Only integer rounding may change wear");

                var saved = ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();
                ItemStack restored = ItemStack.CODEC.parse(ops, saved).getOrThrow();
                assertTrue(ItemStack.isSameItemSameComponents(stack, restored));
                assertEquals(Optional.empty(), restored.get(ModDataComponents.REINFORCED_DATA.get()).baselineItem());
                assertEquals(2, assertInstanceOf(CompoundTag.class, saved).getCompound("components").getCompound("tempered:reinforced_data")
                        .getInt("format_version"));

                var owned = new ReinforcedData(12, 1561, ResourceLocation.parse("minecraft:diamond_axe"));
                restored.set(ModDataComponents.REINFORCED_DATA.get(), owned);
                ItemStack.STREAM_CODEC.encode(buffer, restored);
                assertTrue(ItemStack.isSameItemSameComponents(restored, ItemStack.STREAM_CODEC.decode(buffer)));
                assertEquals(0, buffer.readableBytes());
                assertEquals(owned, ReinforcedData.CODEC.parse(NbtOps.INSTANCE,
                        ReinforcedData.CODEC.encodeStart(NbtOps.INSTANCE, owned).getOrThrow()).getOrThrow());

                for (String invalid : List.of("{format_version:3,level:2,base_max_damage:1561}",
                        "{format_version:\"bad\",level:2,base_max_damage:1561}",
                        "{format_version:2,level:2,base_max_damage:1561,baseline_item:\"Bad ID\"}",
                        "{level:0,base_max_damage:1561}")) {
                    assertTrue(ReinforcedData.CODEC.parse(NbtOps.INSTANCE, TagParser.parseTag(invalid)).error().isPresent());
                }
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
                throw new AssertionError(exception);
            } finally {
                buffer.release();
                TemperedConfig.activate(before);
            }
        }).get();
    }
}
