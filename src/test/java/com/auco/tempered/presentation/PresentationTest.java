package com.auco.tempered.presentation;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.IntStream;
import com.auco.tempered.config.GameplaySettings;
import com.auco.tempered.config.TemperedConfig;
import com.auco.tempered.config.acquisition.AcquisitionSettings;
import com.auco.tempered.config.acquisition.AspectLootSettings;
import com.auco.tempered.config.affix.ExecutionerSettings;
import com.auco.tempered.config.aspect.ReinforcedSettings;
import com.auco.tempered.config.aspect.SwiftSettings;
import com.auco.tempered.equipment.affix.executioner.ExecutionerData;
import com.auco.tempered.equipment.attribute.reinforced.ReinforcedData;
import com.auco.tempered.equipment.attribute.swift.SwiftData;
import com.auco.tempered.registry.ModDataComponents;
import com.auco.tempered.registry.ModItems;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Exercises real tagged equipment and complete documents, rather than individual text helpers. */
@ExtendWith(EphemeralTestServerProvider.class)
class PresentationTest {
    private static final JsonObject ENGLISH = loadEnglish();

    @Test
    void tooltipsExplainActualEligibilityAndCapturedBalanceWithoutChangingEquipment(MinecraftServer server) throws Exception {
        server.submit(() -> {
            var settings = new GameplaySettings(new ReinforcedSettings(true, List.of(13.0, 27.0)),
                    new SwiftSettings(true, List.of(12.5, 22.5)), executioner(true, true, 0.25, 3));
            var activeBefore = TemperedConfig.active();
            var sword = new ItemStack(Items.IRON_SWORD);
            var swordBefore = sword.copy();
            var fresh = EquipmentTooltipContent.build(sword, settings, true);
            assertTrue(render(fresh).contains("0 / 4 progress points"));
            assertTrue(render(fresh).contains("25% chance to add 3"));
            assertTrue(render(fresh).contains("Reinforced: apply at smithing for level I"));
            assertFalse(render(fresh).contains("Swift: apply"));
            assertTrue(ItemStack.matches(swordBefore, sword));

            assertTrue(render(EquipmentTooltipContent.build(sword, GameplaySettings.DEFAULT, false)).contains("0 / 2 hostile kills"));
            sword.set(ModDataComponents.EXECUTIONER_DATA.get(), new ExecutionerData(4));
            assertTrue(render(EquipmentTooltipContent.build(sword, settings, false)).contains("Execution is unavailable"));
            sword.set(ModDataComponents.EXECUTIONER_DATA.get(), new ExecutionerData(9));
            var unlocked = EquipmentTooltipContent.build(sword, settings, true);
            assertTrue(render(unlocked).contains("37.5% execute chance"));
            assertTrue(render(unlocked).contains("18% health"));
            assertTrue(render(unlocked).contains("Highest tier reached (9 progress points)"));
            var paused = new GameplaySettings(settings.reinforced(), settings.swift(), executioner(true, false, 0.25, 3));
            assertTrue(render(EquipmentTooltipContent.build(sword, paused, false)).contains("progression is paused"));
            assertTrue(render(EquipmentTooltipContent.build(sword, paused, false)).contains("37.5% execute chance"));
            var zeroChance = new GameplaySettings(settings.reinforced(), settings.swift(), executioner(true, true, 0, 3));
            assertTrue(render(EquipmentTooltipContent.build(sword, zeroChance, false)).contains("progression is paused"));

            var pick = new ItemStack(Items.IRON_PICKAXE);
            pick.setDamageValue(42);
            pick.set(DataComponents.CUSTOM_NAME, Component.literal("My pick"));
            pick.set(ModDataComponents.REINFORCED_DATA.get(), new ReinforcedData(8, 250));
            pick.set(ModDataComponents.SWIFT_DATA.get(), new SwiftData(1));
            var pickBefore = pick.copy();
            var tooltip = render(EquipmentTooltipContent.build(pick, settings, true));
            assertTrue(tooltip.contains("Reinforced II"));
            assertTrue(tooltip.contains("+27% Maximum Durability"));
            assertTrue(tooltip.contains("Saved level 8 retained"));
            assertTrue(tooltip.contains("Reinforced: application cap reached"));
            assertTrue(tooltip.contains("+12.5% Mining Speed"));
            assertTrue(tooltip.contains("Swift: apply at smithing for level II"));
            assertFalse(tooltip.contains("Executioner"));
            assertTrue(ItemStack.matches(pickBefore, pick));

            var disabled = new GameplaySettings(new ReinforcedSettings(false, List.of(13.0)),
                    new SwiftSettings(false, List.of(12.5)), executioner(false, true, 0.25, 3));
            var dormant = render(EquipmentTooltipContent.build(pick, disabled, true));
            assertTrue(dormant.contains("Inactive: disabled"));
            assertFalse(dormant.contains("Maximum Durability"));
            assertFalse(dormant.contains("Mining Speed"));
            assertTrue(ItemStack.matches(pickBefore, pick));
            assertTrue(render(EquipmentTooltipContent.build(sword, disabled, true)).contains("saved progress: 9"));

            var incompatible = new ItemStack(Items.IRON_SWORD);
            incompatible.set(ModDataComponents.SWIFT_DATA.get(), new SwiftData(2));
            assertTrue(render(EquipmentTooltipContent.build(incompatible, settings, true)).contains("no longer compatible"));
            var aspect = render(EquipmentTooltipContent.build(new ItemStack(ModItems.SWIFT_ASPECT.get()), settings, true));
            assertTrue(aspect.contains("Template slot (left): leave empty"));
            assertTrue(aspect.contains("Application cap: level II"));
            assertTrue(aspect.contains("Level II: +22.5% mining speed"));
            assertTrue(render(EquipmentTooltipContent.build(new ItemStack(ModItems.REINFORCED_ASPECT.get()), disabled, true))
                    .contains("Reinforced is disabled"));
            var malformed = new ItemStack(Items.IRON_PICKAXE);
            malformed.set(ModDataComponents.SWIFT_DATA.get(), new SwiftData(0));
            malformed.set(ModDataComponents.REINFORCED_DATA.get(), new ReinforcedData(0, 250));
            malformed.set(ModDataComponents.EXECUTIONER_DATA.get(), new ExecutionerData(0));
            assertTrue(render(EquipmentTooltipContent.build(malformed, settings, true)).contains("invalid saved data"));
            assertTrue(EquipmentTooltipContent.build(new ItemStack(Items.COBBLESTONE), settings, true).isEmpty());
            assertTrue(EquipmentTooltipContent.build(ItemStack.EMPTY, settings, true).isEmpty());
            assertEquals(activeBefore, TemperedConfig.active());
        }).get();
    }

    @Test
    void guidesUseConfiguredValuesAndKeepEveryTierAndSource(MinecraftServer server) throws Exception {
        server.submit(() -> {
            Set<ResourceLocation> sources = new HashSet<>(AspectLootSettings.DEFAULT.lootTables());
            IntStream.range(0, 252).forEach(i -> sources.add(ResourceLocation.fromNamespaceAndPath("othermod", "chests/ruin_" + i)));
            var acquisition = new AcquisitionSettings(new AspectLootSettings(true, 0.29, 2, 5, sources),
                    new AspectLootSettings(true, 1, 1, 1, Set.of()));
            List<Double> bonuses = IntStream.rangeClosed(1, 100).mapToObj(i -> i + 0.5).toList();
            var settings = new GameplaySettings(new ReinforcedSettings(true, bonuses), new SwiftSettings(false, List.of(3.5)),
                    new ExecutionerSettings(true, true, List.of(4, 9), List.of(7.0, 18.0), 0.25, 3,
                            List.of(0.0, 0.375), true, false), acquisition);

            for (GuideTopic topic : GuideTopic.values()) {
                if (topic != GuideTopic.OVERVIEW && topic != GuideTopic.SMITHING) {
                    render(GuideSettingsContent.build(topic, settings)); // Every nested translation must exist.
                }
            }
            var loot = GuideSettingsContent.build(GuideTopic.LOOT, settings);
            assertTrue(render(loot).contains("29% chance per eligible loot generation; 2–5"));
            assertTrue(render(loot).contains("Swift is disabled"));
            var listed = loot.stream().filter(line -> line.getContents() instanceof TranslatableContents translation
                    && translation.getKey().startsWith("guide.tempered.source.")).toList();
            assertEquals(sources.stream().sorted().map(LootSourceNames::name).map(PresentationTest::render).toList(),
                    listed.stream().map(PresentationTest::render).toList());
            assertEquals(100, GuideSettingsContent.build(GuideTopic.REINFORCED, settings).stream()
                    .filter(line -> line.getContents() instanceof TranslatableContents translation
                            && translation.getKey().equals("guide.tempered.reinforced.level")).count());
            var executioner = render(GuideSettingsContent.build(GuideTopic.EXECUTIONER, settings));
            assertTrue(executioner.contains("4 progress points; threshold 7%; execute chance 0%"));
            assertTrue(executioner.contains("9 progress points; threshold 18%; execute chance 37.5%"));
            assertTrue(executioner.contains("Creative-mode kills can grant"));

            // All three independent ways to suppress acquisition explain unavailability.
            for (AspectLootSettings unavailable : List.of(new AspectLootSettings(false, 1, 1, 1, sources),
                    new AspectLootSettings(true, 0, 1, 1, sources), new AspectLootSettings(true, 1, 1, 1, Set.of()))) {
                var noLoot = new GameplaySettings(settings.reinforced(), SwiftSettings.DEFAULT, settings.executioner(),
                        new AcquisitionSettings(unavailable, unavailable));
                String explanation = render(GuideSettingsContent.build(GuideTopic.LOOT, noLoot));
                assertTrue(explanation.contains("Loot acquisition is unavailable"));
                assertFalse(explanation.contains("chance per eligible loot generation"));
            }
            var defaultExecutioner = render(GuideSettingsContent.build(GuideTopic.EXECUTIONER, GameplaySettings.DEFAULT));
            assertTrue(defaultExecutioner.contains("2 hostile kills"));
            assertTrue(defaultExecutioner.contains("Creative-mode kills do not grant"));
            assertFalse(DisplayValues.chance(1e-12).equals("0"));
            assertFalse(DisplayValues.chance(0.9999999).equals("100"));
        }).get();
    }

    private static ExecutionerSettings executioner(boolean enabled, boolean progression, double chance, int award) {
        return new ExecutionerSettings(enabled, progression, List.of(4, 9), List.of(7.0, 18.0),
                chance, award, List.of(0.0, 0.375), false, true);
    }

    private static String render(List<Component> lines) {
        return String.join("\n", lines.stream().map(PresentationTest::render).toList());
    }

    /** Resolve mod translations explicitly; server-only Language does not load client resource packs. */
    private static String render(Component line) {
        String result;
        if (line.getContents() instanceof TranslatableContents translation) {
            assertTrue(ENGLISH.has(translation.getKey()), "Missing English translation: " + translation.getKey());
            Object[] arguments = java.util.Arrays.stream(translation.getArgs())
                    .map(value -> value instanceof Component nested ? render(nested) : value).toArray();
            result = String.format(Locale.ROOT, ENGLISH.get(translation.getKey()).getAsString(), arguments);
        } else {
            assertInstanceOf(PlainTextContents.class, line.getContents());
            result = ((PlainTextContents) line.getContents()).text();
        }
        var hover = line.getStyle().getHoverEvent();
        if (hover != null && hover.getAction() == HoverEvent.Action.SHOW_TEXT) render(hover.getValue(HoverEvent.Action.SHOW_TEXT));
        for (Component sibling : line.getSiblings()) result += render(sibling);
        return result;
    }

    private static JsonObject loadEnglish() {
        var stream = PresentationTest.class.getResourceAsStream("/assets/tempered/lang/en_us.json");
        assertNotNull(stream);
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (java.io.IOException exception) {
            throw new java.io.UncheckedIOException(exception);
        }
    }
}
