package com.auco.tempered.config.aspect;

import java.util.List;
import com.auco.tempered.config.ConfigValues;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class SwiftConfig {
    private final ModConfigSpec.BooleanValue enabled;
    private final ModConfigSpec.ConfigValue<List<? extends Number>> bonuses;

    public SwiftConfig(ModConfigSpec.Builder builder) {
        builder.translation("tempered.config.aspects.swift").push("aspects.swift");
        enabled = ConfigValues.key(builder, "aspects.swift.enabled",
                "Enable Swift. Disabling preserves saved progression.").define("enabled", true);
        bonuses = ConfigValues.key(builder, "aspects.swift.mining_speed_bonus_percent_by_level",
                "Total bonus percent at each level (not cumulative). 1-100 entries, each 0-10000. List length is the level cap.")
                .define("mining_speed_bonus_percent_by_level", SwiftSettings.DEFAULT.bonuses(),
                        value -> ConfigValues.numbers(value, 0, ConfigValues.MAX_BONUS_PERCENT, false, false));
        builder.pop(2);
    }

    public SwiftSettings read() {
        return new SwiftSettings(enabled.get(), ConfigValues.doubles(bonuses.get()));
    }
}
