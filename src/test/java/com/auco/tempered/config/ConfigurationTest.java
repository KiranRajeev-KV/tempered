package com.auco.tempered.config;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import com.auco.tempered.config.aspect.ReinforcedSettings;
import com.auco.tempered.config.aspect.SwiftSettings;
import com.auco.tempered.config.affix.ExecutionerSettings;
import com.auco.tempered.equipment.attribute.reinforced.ReinforcedData;
import com.auco.tempered.equipment.attribute.reinforced.ReinforcedRules;
import com.auco.tempered.equipment.attribute.swift.SwiftData;
import com.auco.tempered.equipment.affix.executioner.ExecutionerData;
import com.auco.tempered.util.Probability;
import com.electronwill.nightconfig.toml.TomlParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

class ConfigurationTest {
    @Test
    void exampleMatchesSpecAndParsesAsToml() throws Exception {
        assertEquals(ConfigExample.render(), Files.readString(ConfigExample.PATH),
                "Run ./gradlew updateConfigExample after editing config defaults or comments");
        var parsed = new TomlParser().parse(Files.readString(ConfigExample.PATH));
        TemperedConfig.SPEC.correct(parsed);
        assertTrue(TemperedConfig.SPEC.isCorrect(parsed));
        assertEquals(List.of(2, 5, 10), parsed.get("affixes.executioner.kills_required_by_tier"));
    }

    @Test
    void malformedListsRevertWholeValueWithoutShiftingLevels() {
        var parsed = new TomlParser().parse(ConfigExample.render());
        parsed.set("aspects.swift.mining_speed_bonus_percent_by_level", List.of(8, -1, 24));
        parsed.set("affixes.executioner.kills_required_by_tier", List.of(10, 2, 5));
        TemperedConfig.SPEC.correct(parsed);
        assertEquals(SwiftSettings.DEFAULT.bonuses(), parsed.get("aspects.swift.mining_speed_bonus_percent_by_level"));
        assertEquals(ExecutionerSettings.DEFAULT.killsRequired(), parsed.get("affixes.executioner.kills_required_by_tier"));
        assertFalse(ConfigValues.numbers(List.of(Double.NaN), 0, 1, false, false));
        assertFalse(ConfigValues.numbers(List.of(1.5), 1, 100, true, true));
    }

    @Test
    void independentNonlinearLevelsAndDisabledEffects() {
        var reinforced = new ReinforcedSettings(true, List.of(5.0, 17.5));
        var swift = new SwiftSettings(true, List.of(2.0, 12.0, 50.0));
        assertEquals(2, reinforced.maxLevel());
        assertEquals(3, swift.maxLevel());
        assertEquals(1175, ReinforcedRules.maxDamage(1000, 5, reinforced));
        assertEquals(50, swift.bonusPercent(5));
        assertEquals(1000, ReinforcedRules.maxDamage(1000, 5, new ReinforcedSettings(false, reinforced.bonuses())));
        assertThrows(UnsupportedOperationException.class, () -> reinforced.bonuses().add(1.0));
    }

    @Test
    void milestonesAndProgressCanChangeWithoutDiscardingHistory() {
        var defaults = ExecutionerSettings.DEFAULT;
        assertEquals(0, defaults.levelForKills(1));
        assertEquals(1, defaults.levelForKills(2));
        assertEquals(2, defaults.levelForKills(5));
        assertEquals(3, defaults.levelForKills(10));
        assertEquals(15, defaults.healthPercent(10));
        var settings = new ExecutionerSettings(true, true, List.of(64, 192, 384),
                List.of(4.0, 9.0, 20.0), 0.5, 3, List.of(0.1, 0.3, 0.75), false, true);
        assertEquals(1, settings.levelForKills(191));
        assertEquals(192, settings.nextMilestone(191));
        assertEquals(0.3, settings.executeChance(192));
        assertEquals(387, settings.advance(384));
        assertEquals(Integer.MAX_VALUE, settings.advance(Integer.MAX_VALUE - 1));
        assertThrows(IllegalArgumentException.class, () -> new ExecutionerSettings(true, true, List.of(2, 5),
                List.of(5.0), 1, 1, List.of(1.0, 1.0), false, true));
    }

    @Test
    void savedComponentsRoundTripAboveOldCaps() {
        var reinforced = new ReinforcedData(12, 1000);
        var swift = new SwiftData(12);
        var executioner = new ExecutionerData(10000);
        assertEquals(reinforced, ReinforcedData.CODEC.parse(JsonOps.INSTANCE,
                ReinforcedData.CODEC.encodeStart(JsonOps.INSTANCE, reinforced).getOrThrow()).getOrThrow());
        assertEquals(swift, SwiftData.CODEC.parse(JsonOps.INSTANCE,
                SwiftData.CODEC.encodeStart(JsonOps.INSTANCE, swift).getOrThrow()).getOrThrow());
        assertEquals(executioner, ExecutionerData.CODEC.parse(JsonOps.INSTANCE,
                ExecutionerData.CODEC.encodeStart(JsonOps.INSTANCE, executioner).getOrThrow()).getOrThrow());
        assertFalse(new SwiftData(0).isValid());
        assertFalse(new ReinforcedData(1, 0).isValid());
    }

    @Test
    void durabilityRebalancingRetainsRemainingFractionAndSaturates() {
        assertEquals(500, ReinforcedRules.scaledDamage(750, 1500, 1000));
        assertEquals(750, ReinforcedRules.scaledDamage(500, 1000, 1500));
        assertEquals(0, ReinforcedRules.scaledDamage(0, 1000, 1500));
        assertEquals(0, ReinforcedRules.scaledDamage(999, 1000, 1));
        assertEquals(999, ReinforcedRules.scaledDamage(Integer.MAX_VALUE, 1000, 1000));
        assertEquals(Integer.MAX_VALUE, ReinforcedRules.maxDamage(Integer.MAX_VALUE, 1,
                new ReinforcedSettings(true, List.of(10000.0))));
    }

    @Test
    void probabilityRollsOnceAndOnlyWhenNecessary() {
        AtomicInteger calls = new AtomicInteger();
        assertFalse(Probability.succeeds(0, () -> { calls.incrementAndGet(); return 0; }));
        assertTrue(Probability.succeeds(1, () -> { calls.incrementAndGet(); return 1; }));
        assertEquals(0, calls.get());
        assertTrue(Probability.succeeds(0.25, () -> { calls.incrementAndGet(); return 0.249; }));
        assertEquals(1, calls.get());
        assertFalse(Probability.succeeds(0.25, () -> 0.25));
        assertThrows(IllegalArgumentException.class, () -> Probability.succeeds(Double.NaN, () -> 0));
    }
}
