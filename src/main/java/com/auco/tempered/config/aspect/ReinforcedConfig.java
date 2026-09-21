package com.auco.tempered.config.aspect;

import java.util.List;
import com.auco.tempered.config.ConfigValues;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ReinforcedConfig {
    private final ModConfigSpec.BooleanValue enabled;
    private final ModConfigSpec.ConfigValue<List<? extends Number>> bonuses;

    public ReinforcedConfig(ModConfigSpec.Builder builder) {
        builder.translation("tempered.config.aspects.reinforced").push("aspects.reinforced");
        enabled = ConfigValues.key(builder, "aspects.reinforced.enabled",
                "Enable Reinforced. Disabling preserves saved progression.").define("enabled", true);
        bonuses = ConfigValues.key(builder, "aspects.reinforced.durability_bonus_percent_by_level",
                "Total bonus percent at each level (not cumulative). 1-100 entries, each 0-10000. List length is the level cap.")
                .define("durability_bonus_percent_by_level", ReinforcedSettings.DEFAULT.bonuses(),
                        value -> ConfigValues.numbers(value, 0, ConfigValues.MAX_BONUS_PERCENT, false, false));
        builder.pop(2);
    }

    public ReinforcedSettings read() {
        return new ReinforcedSettings(enabled.get(), ConfigValues.doubles(bonuses.get()));
    }
}
