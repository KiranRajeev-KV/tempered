package com.auco.tempered.progression;

import static com.auco.tempered.progression.BaselineAssertions.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.auco.tempered.config.GameplaySettings;
import com.auco.tempered.config.TemperedConfig;
import com.auco.tempered.config.affix.ExecutionerSettings;
import com.auco.tempered.config.aspect.ReinforcedSettings;
import com.auco.tempered.config.aspect.SwiftSettings;
import com.auco.tempered.config.equipment.EquipmentUpgradeSettings;
import com.auco.tempered.config.equipment.EquipmentUpgradeSettings.DamagePolicy;
import com.auco.tempered.equipment.affix.executioner.ExecutionerData;
import com.auco.tempered.equipment.attribute.swift.SwiftData;
import com.auco.tempered.registry.ModDataComponents;
import com.auco.tempered.service.ExecutionerService;
import com.auco.tempered.service.ReinforcementService;
import com.auco.tempered.service.SwiftService;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Exercise native recipes and actual menus so missing mixin registration fails these scenarios. */
@GameTestHolder("tempered")
@PrefixGameTestTemplate(false)
public final class EquipmentUpgradeTest {
    @GameTest(template = "empty")
    public static void materialUpgradesRetainHistoryRebaseDurabilityAndConsumeInputsOnce(GameTestHelper helper) {
        scenario(helper, () -> {
            var server = helper.getLevel().getServer();
            var player = player(server);
            for (Item[] pair : List.of(new Item[]{Items.DIAMOND_AXE, Items.NETHERITE_AXE},
                    new Item[]{Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE},
                    new Item[]{Items.DIAMOND_HELMET, Items.NETHERITE_HELMET})) {
                ItemStack base = progressed(server, pair[0]);
                ItemStack upgraded = upgrade(player, base, pair[1], 20.0, DamagePolicy.REMAINING_FRACTION);
                assertTrue(SwiftService.inspect(upgraded).isSuccess() == (pair[1] != Items.NETHERITE_HELMET));
            }

            ItemStack axe = progressed(server, Items.DIAMOND_AXE);
            var defaults = GameplaySettings.DEFAULT;
            var executioner = ExecutionerSettings.DEFAULT;
            var disabled = new ExecutionerSettings(false, false, executioner.killsRequired(), executioner.healthPercents(),
                    executioner.progressChance(), executioner.progressPerSuccess(), executioner.executeChances(), false, false);
            TemperedConfig.activate(new GameplaySettings(new ReinforcedSettings(false, List.of(10.0)),
                    new SwiftSettings(false, List.of(8.0)), disabled));
            ItemStack dormant = upgrade(player, axe, Items.NETHERITE_AXE, 0, DamagePolicy.REMAINING_FRACTION);
            TemperedConfig.activate(defaults);
            ReinforcementService.reconcile(dormant);
            assertEquals(2437, dormant.getMaxDamage());
            assertEquals(2, dormant.get(ModDataComponents.REINFORCED_DATA.get()).level());

            TemperedConfig.activate(new GameplaySettings(new ReinforcedSettings(true, List.of(10.0)),
                    defaults.swift(), defaults.executioner()));
            upgrade(player, axe, Items.NETHERITE_AXE, 10.0, DamagePolicy.REMAINING_FRACTION);
            TemperedConfig.activate(new GameplaySettings(defaults.reinforced(), defaults.swift(), defaults.executioner(),
                    defaults.acquisition(), new EquipmentUpgradeSettings(DamagePolicy.DAMAGE_POINTS)));
            upgrade(player, axe, Items.NETHERITE_AXE, 20.0, DamagePolicy.DAMAGE_POINTS);

            // A recipe's explicit baseline takes precedence over the destination item's default.
            TemperedConfig.activate(defaults);
            var template = new ItemStack(Items.NETHERITE_AXE);
            template.set(DataComponents.MAX_DAMAGE, 3000);
            var recipe = new SmithingTransformRecipe(Ingredient.EMPTY, Ingredient.of(Items.DIAMOND_AXE),
                    Ingredient.of(Items.NETHERITE_INGOT), template);
            var input = new SmithingRecipeInput(ItemStack.EMPTY, axe, new ItemStack(Items.NETHERITE_INGOT));
            ItemStack custom = recipe.assemble(input, server.registryAccess());
            assertEquals(3000, custom.get(ModDataComponents.REINFORCED_DATA.get()).baseMaxDamage());
            assertEquals(3600, custom.getMaxDamage());
            ReinforcementService.reconcile(custom);
            assertEquals(3600, custom.getMaxDamage(), "Reconciliation must not undo a recipe's explicit durability");

            var smaller = new SmithingTransformRecipe(Ingredient.EMPTY, Ingredient.of(Items.DIAMOND_AXE),
                    Ingredient.of(Items.NETHERITE_INGOT), new ItemStack(Items.IRON_AXE));
            TemperedConfig.activate(new GameplaySettings(defaults.reinforced(), defaults.swift(), defaults.executioner(),
                    defaults.acquisition(), new EquipmentUpgradeSettings(DamagePolicy.DAMAGE_POINTS)));
            ItemStack clamped = smaller.assemble(input, server.registryAccess());
            assertEquals(300, clamped.getMaxDamage());
            assertEquals(299, clamped.getDamageValue(), "Retained damage points must never break a lower-durability result");
            TemperedConfig.activate(defaults);

            // Ineligible destinations retain dormant history without inheriting a durability override.
            var nonEquipment = new SmithingTransformRecipe(Ingredient.EMPTY, Ingredient.of(Items.DIAMOND_AXE),
                    Ingredient.of(Items.NETHERITE_INGOT), new ItemStack(Items.STICK));
            ItemStack dormantStick = nonEquipment.assemble(input, server.registryAccess());
            assertHistory(axe, dormantStick);
            assertFalse(dormantStick.isDamageableItem());

            var ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
            ItemStack restored = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, custom).getOrThrow()).getOrThrow();
            assertTrue(ItemStack.isSameItemSameComponents(custom, restored));
            assertEquals(8, ExecutionerService.recordHostileKill(restored).hostileKills());
            assertTrue(ReinforcementService.apply(restored).isSuccess());
            assertEquals(3900, restored.getMaxDamage(), "Further Aspect levels must use the upgraded baseline once");
        });
    }

    @GameTest(template = "empty")
    public static void maintenanceKeepsBaseHistoryAndCraftingCannotEraseDisabledProgress(GameTestHelper helper) {
        scenario(helper, () -> {
            var server = helper.getLevel().getServer();
            var player = player(server);
            player.giveExperienceLevels(100);
            ItemStack base = progressed(server, Items.DIAMOND_AXE);
            var original = base.copy();
            var anvil = new AnvilMenu(1, player.getInventory());
            player.containerMenu = anvil;
            anvil.getSlot(0).set(base);
            anvil.setItemName("Preserved history");
            anvil.createResult();
            assertHistory(base, anvil.getSlot(2).getItem());
            anvil.clicked(2, 0, ClickType.PICKUP, player);
            ItemStack renamed = anvil.getCarried();
            assertEquals(Component.literal("Preserved history"), renamed.getHoverName());

            anvil.setCarried(ItemStack.EMPTY);
            anvil.getSlot(0).set(renamed);
            anvil.getSlot(1).set(new ItemStack(Items.DIAMOND));
            anvil.createResult();
            assertHistory(base, anvil.getSlot(2).getItem());
            assertTrue(anvil.getSlot(2).getItem().getDamageValue() < renamed.getDamageValue());
            anvil.clicked(2, 0, ClickType.PICKUP, player);
            ItemStack repaired = anvil.getCarried();
            assertHistory(base, repaired);

            anvil.setCarried(ItemStack.EMPTY);
            anvil.getSlot(0).set(repaired);
            var book = new ItemStack(Items.ENCHANTED_BOOK);
            book.enchant(server.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.UNBREAKING), 3);
            anvil.getSlot(1).set(book);
            anvil.createResult();
            assertHistory(base, anvil.getSlot(2).getItem());
            assertFalse(repaired.get(DataComponents.ENCHANTMENTS).equals(anvil.getSlot(2).getItem().get(DataComponents.ENCHANTMENTS)));

            ItemStack donor = progressed(server, Items.DIAMOND_AXE);
            donor.set(ModDataComponents.EXECUTIONER_DATA.get(), new ExecutionerData(100));
            donor.setDamageValue(0);
            anvil.getSlot(0).set(original.copy());
            anvil.getSlot(1).set(donor);
            anvil.createResult();
            assertHistory(base, anvil.getSlot(2).getItem());

            var grindstone = new GrindstoneMenu(2, player.getInventory());
            player.containerMenu = grindstone;
            grindstone.getSlot(0).set(original.copy());
            ItemStack disenchanted = grindstone.getSlot(2).getItem();
            assertHistory(original, disenchanted);
            assertTrue(disenchanted.get(DataComponents.ENCHANTMENTS).isEmpty());
            grindstone.clicked(2, 0, ClickType.PICKUP, player);
            assertHistory(original, grindstone.getCarried());

            ItemStack armor = progressed(server, Items.DIAMOND_HELMET);
            ItemStack armorHistory = armor.copy();
            var smithing = new SmithingMenu(3, player.getInventory());
            player.containerMenu = smithing;
            smithing.getSlot(0).set(new ItemStack(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE));
            smithing.getSlot(1).set(armor);
            smithing.getSlot(2).set(new ItemStack(Items.REDSTONE));
            smithing.createResult();
            ItemStack trimmed = smithing.getSlot(3).getItem();
            assertHistory(armor, trimmed);
            assertNotNull(trimmed.get(DataComponents.TRIM));
            assertEquals(armor.getDamageValue(), trimmed.getDamageValue());
            smithing.clicked(3, 0, ClickType.PICKUP, player);
            assertHistory(armorHistory, smithing.getCarried());

            var defaults = GameplaySettings.DEFAULT;
            var executioner = defaults.executioner();
            TemperedConfig.activate(new GameplaySettings(new ReinforcedSettings(false, List.of(10.0)),
                    new SwiftSettings(false, List.of(8.0)), new ExecutionerSettings(false, false,
                    executioner.killsRequired(), executioner.healthPercents(), 1, 1, executioner.executeChances(), false, false)));
            var repair = new RepairItemRecipe(CraftingBookCategory.MISC);
            for (var component : List.of(ModDataComponents.REINFORCED_DATA.get(), ModDataComponents.SWIFT_DATA.get(),
                    ModDataComponents.EXECUTIONER_DATA.get())) {
                ItemStack protectedInput = original.copy();
                for (var other : List.of(ModDataComponents.REINFORCED_DATA.get(), ModDataComponents.SWIFT_DATA.get(),
                        ModDataComponents.EXECUTIONER_DATA.get())) {
                    if (other != component) protectedInput.remove(other);
                }
                for (var inputs : List.of(List.of(protectedInput, new ItemStack(Items.DIAMOND_AXE)),
                        List.of(new ItemStack(Items.DIAMOND_AXE), protectedInput))) {
                    var crafting = CraftingInput.of(2, 1, inputs);
                    assertFalse(repair.matches(crafting, helper.getLevel()));
                    assertTrue(repair.assemble(crafting, server.registryAccess()).isEmpty());
                    assertNotNull(protectedInput.get(component));
                }
            }
            var vanilla = CraftingInput.of(2, 1, List.of(new ItemStack(Items.DIAMOND_AXE), new ItemStack(Items.DIAMOND_AXE)));
            assertTrue(repair.matches(vanilla, helper.getLevel()));
            assertFalse(repair.assemble(vanilla, server.registryAccess()).isEmpty());
        });
    }

    private static ItemStack upgrade(FakePlayer player, ItemStack base, Item destination, double bonus, DamagePolicy policy) {
        ItemStack untouched = base.copy();
        var menu = new SmithingMenu(1, player.getInventory());
        player.containerMenu = menu;
        menu.getSlot(0).set(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 2));
        menu.getSlot(1).set(base.copy());
        menu.getSlot(2).set(new ItemStack(Items.NETHERITE_INGOT, 3));
        menu.createResult();
        menu.createResult();
        assertTrue(ItemStack.isSameItemSameComponents(base, menu.getSlot(1).getItem()), "Previews must leave the base unchanged");
        assertEquals(2, menu.getSlot(0).getItem().getCount());
        assertEquals(3, menu.getSlot(2).getItem().getCount());
        ItemStack preview = menu.getSlot(3).getItem().copy();
        assertTrue(preview.is(destination));
        int baseline = new ItemStack(destination).getMaxDamage();
        int maximum = (int) Math.round(baseline * (1 + bonus / 100));
        assertEquals(maximum, preview.getMaxDamage());
        assertEquals(baseline, preview.get(ModDataComponents.REINFORCED_DATA.get()).baseMaxDamage());
        assertEquals(Optional.of(BuiltInRegistries.ITEM.getKey(destination)), preview.get(ModDataComponents.REINFORCED_DATA.get()).baselineItem());
        assertEquals(base.get(ModDataComponents.REINFORCED_DATA.get()).level(), preview.get(ModDataComponents.REINFORCED_DATA.get()).level());
        assertEquals(base.get(ModDataComponents.SWIFT_DATA.get()), preview.get(ModDataComponents.SWIFT_DATA.get()));
        assertEquals(base.get(ModDataComponents.EXECUTIONER_DATA.get()), preview.get(ModDataComponents.EXECUTIONER_DATA.get()));
        assertEquals(base.get(DataComponents.CUSTOM_NAME), preview.get(DataComponents.CUSTOM_NAME));
        assertEquals(base.get(DataComponents.ENCHANTMENTS), preview.get(DataComponents.ENCHANTMENTS));
        int damage = policy == DamagePolicy.DAMAGE_POINTS ? base.getDamageValue()
                : (int) Math.round((double) base.getDamageValue() / base.getMaxDamage() * maximum);
        assertEquals(damage, preview.getDamageValue());
        menu.clicked(3, 0, ClickType.PICKUP, player);
        ItemStack upgraded = menu.getCarried();
        assertTrue(ItemStack.isSameItemSameComponents(preview, upgraded));
        assertTrue(menu.getSlot(1).getItem().isEmpty());
        assertEquals(1, menu.getSlot(0).getItem().getCount());
        assertEquals(2, menu.getSlot(2).getItem().getCount());
        assertTrue(menu.getSlot(3).getItem().isEmpty());
        assertTrue(ItemStack.isSameItemSameComponents(untouched, base));
        ReinforcementService.reconcile(upgraded);
        assertEquals(maximum, upgraded.getMaxDamage());
        assertEquals(damage, upgraded.getDamageValue());
        return upgraded;
    }

    private static ItemStack progressed(MinecraftServer server, Item item) {
        ItemStack stack = new ItemStack(item);
        assertTrue(ReinforcementService.apply(stack).isSuccess());
        assertTrue(ReinforcementService.apply(stack).isSuccess());
        if (SwiftService.isApplicable(stack)) assertTrue(SwiftService.apply(stack).isSuccess());
        // Keep saved components on ineligible equipment too, exercising dormant history.
        else stack.set(ModDataComponents.SWIFT_DATA.get(), new SwiftData(1));
        stack.set(ModDataComponents.EXECUTIONER_DATA.get(), new ExecutionerData(7));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Upgrade history"));
        stack.enchant(server.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.UNBREAKING), 2);
        stack.setDamageValue(stack.getMaxDamage() / 2);
        return stack;
    }

    private static void assertHistory(ItemStack base, ItemStack output) {
        assertFalse(output.isEmpty());
        assertEquals(base.get(ModDataComponents.REINFORCED_DATA.get()), output.get(ModDataComponents.REINFORCED_DATA.get()));
        assertEquals(base.get(ModDataComponents.SWIFT_DATA.get()), output.get(ModDataComponents.SWIFT_DATA.get()));
        assertEquals(base.get(ModDataComponents.EXECUTIONER_DATA.get()), output.get(ModDataComponents.EXECUTIONER_DATA.get()));
    }

    private static FakePlayer player(MinecraftServer server) {
        return new FakePlayer(server.overworld(), new GameProfile(UUID.randomUUID(), "Upgrade baseline"));
    }

    private static void scenario(GameTestHelper helper, Runnable action) {
        var before = TemperedConfig.active();
        try {
            TemperedConfig.activate(GameplaySettings.DEFAULT);
            action.run();
            helper.succeed();
        } finally {
            TemperedConfig.activate(before);
        }
    }
}
