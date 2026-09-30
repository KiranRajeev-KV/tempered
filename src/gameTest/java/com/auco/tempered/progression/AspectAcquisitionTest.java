package com.auco.tempered.progression;

import static com.auco.tempered.progression.BaselineAssertions.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.auco.tempered.config.GameplaySettings;
import com.auco.tempered.config.TemperedConfig;
import com.auco.tempered.config.acquisition.AcquisitionSettings;
import com.auco.tempered.config.acquisition.AspectLootSettings;
import com.auco.tempered.config.aspect.ReinforcedSettings;
import com.auco.tempered.registry.ModItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Exercises the registered JSON modifiers and vanilla container lifecycle, not direct modifier calls. */
@GameTestHolder("tempered")
@PrefixGameTestTemplate(false)
public class AspectAcquisitionTest {
    @GameTest(template = "empty")
    public static void loadedModifiersRespectSourcesIndependentSettingsAndPreserveVanillaLoot(GameTestHelper helper) {
        scenario(helper, () -> {
            var sources = AspectLootSettings.DEFAULT.lootTables();
            var never = loot(true, 0, 1, 1, sources);
            var always = loot(true, 1, 1, 1, sources);
            for (var source : sources) {
                activate(never, never);
                var original = roll(helper, source, 1234);
                activate(always, always);
                var rewarded = roll(helper, source, 1234);
                assertEquals(1, count(rewarded, ModItems.REINFORCED_ASPECT.get()));
                assertEquals(1, count(rewarded, ModItems.SWIFT_ASPECT.get()));
                var retained = rewarded.stream().filter(stack -> !isAspect(stack)).toList();
                assertEquals(original.size(), retained.size());
                for (int i = 0; i < original.size(); i++) {
                    assertTrue(ItemStack.matches(original.get(i), retained.get(i)), "Original loot must remain unchanged");
                }
            }
            var source = BuiltInLootTables.SIMPLE_DUNGEON.location();
            activate(never, always);
            var onlySwift = roll(helper, source, 1234);
            assertEquals(0, count(onlySwift, ModItems.REINFORCED_ASPECT.get()));
            assertEquals(1, count(onlySwift, ModItems.SWIFT_ASPECT.get()));
            activate(always, loot(false, 1, 1, 1, sources));
            assertEquals(0, count(roll(helper, source, 1234), ModItems.SWIFT_ASPECT.get()));
            activate(always, always);
            var defaults = TemperedConfig.active();
            TemperedConfig.activate(new GameplaySettings(new ReinforcedSettings(false, defaults.reinforced().bonuses()),
                    defaults.swift(), defaults.executioner(), defaults.acquisition()));
            var disabledMechanic = roll(helper, source, 1234);
            assertEquals(0, count(disabledMechanic, ModItems.REINFORCED_ASPECT.get()));
            assertEquals(1, count(disabledMechanic, ModItems.SWIFT_ASPECT.get()));
            activate(always, loot(true, 1, 1, 1, Set.of()));
            assertEquals(0, count(roll(helper, source, 1234), ModItems.SWIFT_ASPECT.get()));
            activate(always, always);
            assertTrue(roll(helper, ResourceLocation.parse("minecraft:chests/village/village_plains_house"), 1234)
                    .stream().noneMatch(AspectAcquisitionTest::isAspect));

            activate(loot(true, 1, 16, 16, sources), loot(true, 1, 2, 5, sources));
            var quantities = roll(helper, source, 1234);
            assertEquals(16, count(quantities, ModItems.REINFORCED_ASPECT.get()));
            int swift = count(quantities, ModItems.SWIFT_ASPECT.get());
            assertTrue(swift >= 2 && swift <= 5);
        });
    }

    @GameTest(template = "empty")
    public static void pendingChestLootGeneratesOnceAndOrdinaryStorageGetsNothing(GameTestHelper helper) {
        scenario(helper, () -> {
            var always = loot(true, 1, 1, 1, AspectLootSettings.DEFAULT.lootTables());
            activate(always, always);
            var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "Acquisition"));
            helper.setBlock(new BlockPos(2, 1, 2), Blocks.CHEST);
            var chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(new BlockPos(2, 1, 2)));
            chest.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON);
            chest.setLootTableSeed(1234);
            assertNotNull(chest.getLootTable());
            assertNotNull(chest.createMenu(1, player.getInventory(), player));
            assertNull(chest.getLootTable(), "Vanilla must clear the pending loot table after generation");
            var generated = contents(chest);
            assertEquals(1, count(generated, ModItems.REINFORCED_ASPECT.get()));
            assertEquals(1, count(generated, ModItems.SWIFT_ASPECT.get()));
            assertNotNull(chest.createMenu(2, player.getInventory(), player));
            var reopened = contents(chest);
            for (int i = 0; i < generated.size(); i++) assertTrue(ItemStack.matches(generated.get(i), reopened.get(i)));
            chest.clearContent();
            assertNotNull(chest.createMenu(3, player.getInventory(), player));
            assertTrue(contents(chest).stream().allMatch(ItemStack::isEmpty), "Emptying and reopening cannot refill loot");

            helper.setBlock(new BlockPos(4, 1, 2), Blocks.CHEST);
            var storage = (ChestBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(new BlockPos(4, 1, 2)));
            storage.setItem(0, new ItemStack(Items.DIAMOND));
            assertNotNull(storage.createMenu(4, player.getInventory(), player));
            assertEquals(1, count(contents(storage), Items.DIAMOND));
            assertTrue(contents(storage).stream().noneMatch(AspectAcquisitionTest::isAspect));
        });
    }

    private static List<ItemStack> roll(GameTestHelper helper, ResourceLocation table, long seed) {
        var params = new LootParams.Builder(helper.getLevel()).withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                .create(LootContextParamSets.CHEST);
        return helper.getLevel().getServer().reloadableRegistries().getLootTable(
                net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, table))
                .getRandomItems(params, seed);
    }

    private static List<ItemStack> contents(ChestBlockEntity chest) {
        return java.util.stream.IntStream.range(0, chest.getContainerSize()).mapToObj(i -> chest.getItem(i).copy()).toList();
    }

    private static boolean isAspect(ItemStack stack) {
        return stack.is(ModItems.REINFORCED_ASPECT.get()) || stack.is(ModItems.SWIFT_ASPECT.get());
    }

    private static int count(List<ItemStack> stacks, Item item) {
        return stacks.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    private static AspectLootSettings loot(boolean enabled, double chance, int min, int max, Set<ResourceLocation> sources) {
        return new AspectLootSettings(enabled, chance, min, max, sources);
    }

    private static void activate(AspectLootSettings reinforced, AspectLootSettings swift) {
        var defaults = GameplaySettings.DEFAULT;
        TemperedConfig.activate(new GameplaySettings(defaults.reinforced(), defaults.swift(), defaults.executioner(),
                new AcquisitionSettings(reinforced, swift)));
    }

    private static void scenario(GameTestHelper helper, Runnable action) {
        var before = TemperedConfig.active();
        try {
            action.run();
            helper.succeed();
        } finally {
            TemperedConfig.activate(before);
        }
    }
}
