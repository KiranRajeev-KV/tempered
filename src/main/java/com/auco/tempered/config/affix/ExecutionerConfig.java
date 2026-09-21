package com.auco.tempered.config.affix;

import java.util.List;
import com.auco.tempered.config.ConfigValues;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ExecutionerConfig {
    private final ModConfigSpec.BooleanValue enabled, progressionEnabled, creative, announce;
    private final ModConfigSpec.ConfigValue<List<? extends Number>> kills, health, chances;
    private final ModConfigSpec.DoubleValue progressChance;
    private final ModConfigSpec.IntValue progressAmount;

    public ExecutionerConfig(ModConfigSpec.Builder builder) {
        builder.translation("tempered.config.affixes.executioner").push("affixes.executioner");
        enabled = key(builder, "enabled", "Enable Executioner effects and progression; saved history is retained.").define("enabled", true);
        progressionEnabled = key(builder, "progression_enabled", "Allow earning progress. Existing tiers still work when false.").define("progression_enabled", true);
        kills = key(builder, "kills_required_by_tier", "Total progress required per tier: 1-100 strictly increasing integers, each 1-2147483647.")
                .define("kills_required_by_tier", ExecutionerSettings.DEFAULT.killsRequired(), value -> ConfigValues.numbers(value, 1, Integer.MAX_VALUE, true, true));
        health = key(builder, "execute_health_percent_by_tier", "Maximum-health execute threshold per tier, each 0-100 percent. Must match milestone list length.")
                .define("execute_health_percent_by_tier", ExecutionerSettings.DEFAULT.healthPercents(), value -> ConfigValues.numbers(value, 0, 100, false, false));
        progressChance = key(builder, "progress_chance", "Probability per qualifying melee kill: 0 never, 0.25 = 25%, 1 always.")
                .defineInRange("progress_chance", 1.0, 0.0, 1.0);
        progressAmount = key(builder, "progress_per_success", "Progress awarded by a successful kill roll, 1-2147483647. Counter saturates safely.")
                .defineInRange("progress_per_success", 1, 1, Integer.MAX_VALUE);
        chances = key(builder, "execute_chance_by_tier", "Probability per qualifying hit, each 0-1. Must match milestone list length. No roll for ineligible or naturally lethal hits.")
                .define("execute_chance_by_tier", ExecutionerSettings.DEFAULT.executeChances(), value -> ConfigValues.numbers(value, 0, 1, false, false));
        creative = key(builder, "allow_creative_progression", "Allow creative-mode kills to earn progress. Does not change combat eligibility.").define("allow_creative_progression", false);
        announce = key(builder, "announce_tier_up", "Show the player a message when a weapon reaches a new tier.").define("announce_tier_up", true);
        builder.pop(2);
    }

    private static ModConfigSpec.Builder key(ModConfigSpec.Builder builder, String name, String comment) {
        return ConfigValues.key(builder, "affixes.executioner." + name, comment);
    }

    public ExecutionerSettings read() {
        return new ExecutionerSettings(enabled.get(), progressionEnabled.get(), ConfigValues.integers(kills.get()),
                ConfigValues.doubles(health.get()), progressChance.get(), progressAmount.get(),
                ConfigValues.doubles(chances.get()), creative.get(), announce.get());
    }
}
