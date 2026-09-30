package com.auco.tempered.progression;

import static com.auco.tempered.progression.BaselineAssertions.*;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import com.auco.tempered.config.GameplaySettings;
import com.auco.tempered.config.TemperedConfig;
import com.auco.tempered.config.affix.ExecutionerSettings;
import com.auco.tempered.equipment.affix.executioner.ExecutionerData;
import com.auco.tempered.event.ExecutionerProgressionHandler;
import com.auco.tempered.registry.ModDataComponents;
import com.auco.tempered.registry.ModItems;
import com.auco.tempered.service.ExecutionerService;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** A few complete gameplay scenarios rather than isolated implementation checks. */
@GameTestHolder("tempered")
@PrefixGameTestTemplate(false)
@Mod("tempered_gametests")
@EventBusSubscriber(modid = "tempered_gametests")
public class ProgressionBaselineTest {
    @SubscribeEvent
    public static void createTestTemplate(ServerStartedEvent event) throws Exception {
        event.getServer().getStructureManager().getOrCreate(ResourceLocation.fromNamespaceAndPath("tempered", "empty"))
                .load(event.getServer().registryAccess().lookupOrThrow(Registries.BLOCK),
                        TagParser.parseTag("{size:[8,5,8],entities:[],blocks:[],palette:[{Name:\"minecraft:air\"}]}"));
    }

    @GameTest(template = "empty")
    public static void realAttackAndSweepCreditTheOriginalWeaponButOtherSourcesDoNot(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        scenario(helper, () -> {
            FakePlayer player = player(server);
            ItemStack weapon = new ItemStack(Items.IRON_SWORD);
            player.setItemSlot(EquipmentSlot.MAINHAND, weapon);
            BlockPos origin = helper.absolutePos(new BlockPos(2, 2, 2));
            player.setPos(origin.getX(), origin.getY(), origin.getZ());
            player.setOnGround(true);
            player.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(100);
            player.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.1);
            player.tick();
            Zombie primary = zombie(server, 0.5F);
            Zombie swept = zombie(server, 0.5F);
            primary.setPos(player.getX(), player.getY(), player.getZ() + 1);
            swept.setPos(player.getX() + 0.5, player.getY(), player.getZ() + 1);
            server.overworld().addFreshEntity(primary);
            server.overworld().addFreshEntity(swept);
            try {
                player.attack(primary);
                assertTrue(primary.isDeadOrDying());
                assertTrue(swept.isDeadOrDying(), "The real sword attack must sweep the nearby hostile");
                assertNull(weapon.get(ModDataComponents.EXECUTIONER_DATA.get()), "Credit waits for final death cancellation");
                ItemStack replacement = new ItemStack(Items.IRON_AXE);
                player.setItemSlot(EquipmentSlot.MAINHAND, replacement);
                flush(server);
                assertEquals(2, progress(weapon));
                assertEquals(0, progress(replacement));
                flush(server);
                assertEquals(2, progress(weapon), "A second tick must not repeat awards");

                Zombie reflected = zombie(server, 1);
                reflected.hurt(player.damageSources().thorns(player), 100);
                Zombie projectile = zombie(server, 1);
                var arrow = EntityType.ARROW.create(server.overworld());
                projectile.hurt(new DamageSource(server.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.ARROW), arrow, player), 100);
                Zombie environmental = zombie(server, 1);
                environmental.hurt(player.damageSources().fall(), 100);
                var passive = EntityType.COW.create(server.overworld());
                passive.hurt(player.damageSources().playerAttack(player), 100);
                var immune = EntityType.HUSK.create(server.overworld());
                assertTrue(ExecutionerService.isImmuneTarget(immune), "The test datapack must load the immunity tag");
                immune.hurt(player.damageSources().playerAttack(player), 100);
                player.setGameMode(GameType.CREATIVE);
                zombie(server, 1).hurt(player.damageSources().playerAttack(player), 100);
                flush(server);
                assertEquals(0, progress(replacement));
            } finally {
                primary.discard();
                swept.discard();
            }
        });
    }

    @GameTest(template = "empty")
    public static void lateCancellationAndDuplicateNotificationDoNotAwardExtraProgress(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        scenario(helper, () -> {
            FakePlayer player = player(server);
            ItemStack weapon = new ItemStack(Items.IRON_SWORD);
            player.setItemSlot(EquipmentSlot.MAINHAND, weapon);
            Zombie target = zombie(server, 1);
            Consumer<LivingDeathEvent> cancel = event -> {
                if (event.getEntity() == target) {
                    // Deliberately enqueue first to exercise cancellation after our handler.
                    ExecutionerProgressionHandler.onLivingDeath(event);
                    event.setCanceled(true);
                    target.setHealth(1);
                }
            };
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, LivingDeathEvent.class, cancel);
            try {
                target.hurt(player.damageSources().playerAttack(player), 100);
                flush(server);
                assertEquals(0, progress(weapon));
                assertFalse(target.isDeadOrDying());
            } finally {
                NeoForge.EVENT_BUS.unregister(cancel);
            }
            target.invulnerableTime = 0;
            target.hurt(player.damageSources().playerAttack(player), 100);
            // A duplicate notification in the same tick must still award only once.
            NeoForge.EVENT_BUS.post(new LivingDeathEvent(target, player.damageSources().playerAttack(player)));
            flush(server);
            assertEquals(1, progress(weapon));
        });
    }

    @GameTest(template = "empty")
    public static void executionRespectsAbsorptionExactThresholdTotemsAndProbabilityBoundaries(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        scenario(helper, () -> {
            FakePlayer player = player(server);
            ItemStack weapon = new ItemStack(Items.IRON_SWORD);
            weapon.set(ModDataComponents.EXECUTIONER_DATA.get(), new ExecutionerData(2));
            player.setItemSlot(EquipmentSlot.MAINHAND, weapon);

            Zombie absorbed = zombie(server, 2);
            absorbed.setAbsorptionAmount(2);
            absorbed.hurt(player.damageSources().playerAttack(player), 2);
            assertEquals(2, absorbed.getHealth(), 0.0001F);
            assertFalse(absorbed.isDeadOrDying());

            Zombie above = zombie(server, 2.1F);
            above.hurt(player.damageSources().playerAttack(player), 1);
            assertEquals(1.1F, above.getHealth(), 0.0001F);
            assertFalse(above.isDeadOrDying());

            Zombie exact = zombie(server, 2);
            exact.setAbsorptionAmount(2);
            exact.hurt(player.damageSources().playerAttack(player), 3);
            assertTrue(exact.isDeadOrDying(), "One health is exactly 5% of this target's maximum");
            flush(server);
            assertEquals(3, progress(weapon));
            assertSame(player, exact.getKillCredit());

            Zombie saved = zombie(server, 2);
            saved.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
            saved.hurt(player.damageSources().playerAttack(player), 1);
            assertFalse(saved.isDeadOrDying());
            assertTrue(saved.getOffhandItem().isEmpty());
            flush(server);
            assertEquals(3, progress(weapon));

            var defaults = ExecutionerSettings.DEFAULT;
            TemperedConfig.activate(new GameplaySettings(GameplaySettings.DEFAULT.reinforced(),
                    GameplaySettings.DEFAULT.swift(), new ExecutionerSettings(true, true,
                    defaults.killsRequired(), defaults.healthPercents(), 0, 1, List.of(0.0, 0.0, 0.0), false, false)));
            Zombie noProc = zombie(server, 2);
            noProc.hurt(player.damageSources().playerAttack(player), 1);
            assertFalse(noProc.isDeadOrDying());
            noProc.invulnerableTime = 0;
            noProc.hurt(player.damageSources().playerAttack(player), 100);
            flush(server);
            assertEquals(3, progress(weapon), "Zero progress chance also rejects a real lethal melee hit");
        });
    }

    @GameTest(template = "empty")
    public static void smithingTransactionsPreserveCombinedEquipmentAndRestoredProgressContinues(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        scenario(helper, () -> {
            FakePlayer player = player(server);
            ItemStack axe = new ItemStack(Items.IRON_AXE);
            axe.setDamageValue(80);
            axe.set(DataComponents.CUSTOM_NAME, Component.literal("Tempered baseline"));
            axe.enchant(server.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                    .getHolderOrThrow(Enchantments.UNBREAKING), 2);
            axe.set(ModDataComponents.EXECUTIONER_DATA.get(), new ExecutionerData(1));
            int originalMaximum = axe.getMaxDamage();
            var originalEnchantments = axe.get(DataComponents.ENCHANTMENTS);
            SmithingMenu menu = new SmithingMenu(1, player.getInventory());
            player.containerMenu = menu;

            menu.getSlot(1).set(axe);
            menu.getSlot(2).set(new ItemStack(ModItems.REINFORCED_ASPECT.get(), 3));
            menu.createResult();
            menu.createResult();
            assertEquals(3, menu.getSlot(2).getItem().getCount());
            assertNull(axe.get(ModDataComponents.REINFORCED_DATA.get()));
            menu.clicked(3, 0, ClickType.PICKUP, player);
            ItemStack upgraded = menu.getCarried();
            assertFalse(upgraded.isEmpty());
            assertTrue(menu.getSlot(1).getItem().isEmpty());
            assertEquals(2, menu.getSlot(2).getItem().getCount());
            assertEquals(1, upgraded.get(ModDataComponents.REINFORCED_DATA.get()).level());
            menu.setCarried(ItemStack.EMPTY);
            menu.getSlot(1).set(upgraded);
            menu.createResult();
            menu.clicked(3, 0, ClickType.PICKUP, player);
            upgraded = menu.getCarried();
            assertEquals(2, upgraded.get(ModDataComponents.REINFORCED_DATA.get()).level());
            assertEquals(originalMaximum, upgraded.get(ModDataComponents.REINFORCED_DATA.get()).baseMaxDamage());
            assertEquals((int) (originalMaximum * 1.2), upgraded.getMaxDamage());
            assertEquals(1, menu.getSlot(2).getItem().getCount());

            menu.setCarried(ItemStack.EMPTY);
            menu.getSlot(1).set(upgraded);
            menu.getSlot(2).set(new ItemStack(ModItems.SWIFT_ASPECT.get(), 2));
            menu.createResult();
            menu.clicked(3, 0, ClickType.PICKUP, player);
            ItemStack combined = menu.getCarried();
            assertEquals(1, combined.get(ModDataComponents.SWIFT_DATA.get()).level());
            assertEquals(originalEnchantments, combined.get(DataComponents.ENCHANTMENTS));
            assertEquals(Component.literal("Tempered baseline"), combined.getHoverName());
            assertEquals(80, combined.getDamageValue());
            assertEquals(1, progress(combined));
            assertEquals(1, menu.getSlot(2).getItem().getCount());

            var ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
            ItemStack restored = ItemStack.CODEC.parse(ops,
                    ItemStack.CODEC.encodeStart(ops, combined).getOrThrow()).getOrThrow();
            assertTrue(ItemStack.isSameItemSameComponents(combined, restored));
            menu.setCarried(ItemStack.EMPTY);
            menu.getSlot(1).set(restored);
            menu.createResult();
            menu.clicked(3, 0, ClickType.PICKUP, player);
            ItemStack continued = menu.getCarried();
            assertEquals(2, continued.get(ModDataComponents.SWIFT_DATA.get()).level());
            player.setItemSlot(EquipmentSlot.MAINHAND, continued);
            Consumer<PlayerEvent.BreakSpeed> modifier = event -> {
                if (event.getEntity() == player) event.setNewSpeed(10);
            };
            NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.BreakSpeed.class, modifier);
            try {
                var speed = new PlayerEvent.BreakSpeed(player, Blocks.OAK_LOG.defaultBlockState(), 5, BlockPos.ZERO);
                NeoForge.EVENT_BUS.post(speed);
                assertEquals(11.6F, speed.getNewSpeed(), 0.0001F);
            } finally {
                NeoForge.EVENT_BUS.unregister(modifier);
            }
            zombie(server, 1).hurt(player.damageSources().playerAttack(player), 100);
            flush(server);
            assertEquals(2, progress(continued));
            assertNotNull(ExecutionerService.getActiveData(continued));

            // Datapacks can admit stackable modded items. Output must still be one item.
            menu.setCarried(ItemStack.EMPTY);
            menu.getSlot(1).set(new ItemStack(Items.STICK, 4));
            menu.getSlot(2).set(new ItemStack(ModItems.SWIFT_ASPECT.get(), 2));
            menu.createResult();
            assertEquals(1, menu.getSlot(3).getItem().getCount());
            menu.clicked(3, 0, ClickType.PICKUP, player);
            assertEquals(1, menu.getCarried().getCount());
            assertEquals(3, menu.getSlot(1).getItem().getCount());
            assertEquals(1, menu.getSlot(2).getItem().getCount());
            assertNull(menu.getSlot(1).getItem().get(ModDataComponents.SWIFT_DATA.get()));
        });
    }

    private static void scenario(GameTestHelper helper, Runnable action) {
        var before = TemperedConfig.active();
        try {
            TemperedConfig.activate(GameplaySettings.DEFAULT);
            action.run();
            helper.succeed();
        } finally {
            flush(helper.getLevel().getServer());
            TemperedConfig.activate(before);
        }
    }

    private static FakePlayer player(MinecraftServer server) {
        return new FakePlayer(server.overworld(), new GameProfile(UUID.randomUUID(), "Baseline"));
    }

    private static Zombie zombie(MinecraftServer server, float health) {
        Zombie target = new Zombie(server.overworld());
        // Isolate threshold behavior from the zombie's natural armor reduction.
        target.getAttribute(Attributes.ARMOR).setBaseValue(0);
        target.getAttribute(Attributes.MAX_ABSORPTION).setBaseValue(20);
        target.setHealth(health);
        return target;
    }

    private static int progress(ItemStack stack) {
        var data = stack.get(ModDataComponents.EXECUTIONER_DATA.get());
        return data == null ? 0 : data.hostileKills();
    }

    private static void flush(MinecraftServer server) {
        NeoForge.EVENT_BUS.post(new ServerTickEvent.Post(() -> true, server));
    }
}
