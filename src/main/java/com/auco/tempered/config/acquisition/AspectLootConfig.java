package com.auco.tempered.config.acquisition;

import java.util.List;
import java.util.stream.Collectors;
import com.auco.tempered.config.ConfigValues;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

/** The same declaration and validation rules apply independently to every Aspect. */
public final class AspectLootConfig {
    private final String path;
    private final ModConfigSpec.BooleanValue enabled;
    private final ModConfigSpec.ConfigValue<Number> chance;
    private final ModConfigSpec.IntValue minCount;
    private final ModConfigSpec.IntValue maxCount;
    private final ModConfigSpec.ConfigValue<List<? extends String>> lootTables;

    public AspectLootConfig(ModConfigSpec.Builder builder, String aspect) {
        path = "acquisition." + aspect;
        builder.translation("tempered.config." + path).push(path);
        enabled = ConfigValues.key(builder, path + ".enabled",
                "Enable loot acquisition for this Aspect. The Aspect mechanic must also be enabled. Existing items are preserved.")
                .define("enabled", AspectLootSettings.DEFAULT.enabled());
        chance = ConfigValues.key(builder, path + ".chance",
                "One independent roll per eligible loot generation, finite number 0-1: 0 never, 0.15 = 15%, 1 always. Both Aspects may appear together.")
                .define("chance", (Number) AspectLootSettings.DEFAULT.chance(), value -> value instanceof Number number
                        && Double.isFinite(number.doubleValue()) && number.doubleValue() >= 0 && number.doubleValue() <= 1);
        minCount = ConfigValues.key(builder, path + ".min_count",
                "Minimum items on success, inclusive: 1-16. Must not exceed max_count; an inconsistent pair uses the default counts (1, 1).")
                .defineInRange("min_count", AspectLootSettings.DEFAULT.minCount(), 1, AspectLootSettings.MAX_COUNT);
        maxCount = ConfigValues.key(builder, path + ".max_count",
                "Maximum items on success, inclusive: 1-16. Every integer between min_count and max_count is equally likely.")
                .defineInRange("max_count", AspectLootSettings.DEFAULT.maxCount(), 1, AspectLootSettings.MAX_COUNT);
        lootTables = ConfigValues.key(builder, path + ".loot_tables",
                "Exact loot-table IDs (0-256), each namespace:path. [] disables all sources. Duplicate IDs count once. Unknown IDs are inert.\n"
                + "Example modded source: othermod:chests/ruins. Add only container loot tables to keep acquisition exploration-only.\n"
                + "Already-generated loot is unchanged; unopened containers with pending loot can receive Aspects. Reopening never rerolls.")
                .define("loot_tables", AspectLootSettings.DEFAULT.lootTables().stream().map(ResourceLocation::toString).sorted().toList(),
                        AspectLootConfig::validSources);
        builder.pop(2);
    }

    public static boolean validSources(Object value) {
        return value instanceof List<?> list && list.size() <= AspectLootSettings.MAX_SOURCES
                && list.stream().allMatch(entry -> entry instanceof String id && id.length() <= 256
                && id.indexOf(':') > 0 && !id.endsWith(":") && ResourceLocation.tryParse(id) != null);
    }

    public AspectLootSettings read() {
        int minimum = minCount.get();
        int maximum = maxCount.get();
        if (minimum > maximum) {
            LogUtils.getLogger().error("Invalid {} count range: min_count exceeds max_count. Using default counts (1, 1) until restart.", path);
            minimum = maximum = 1;
        }
        return new AspectLootSettings(enabled.get(), chance.get().doubleValue(), minimum, maximum,
                lootTables.get().stream().map(ResourceLocation::parse).collect(Collectors.toUnmodifiableSet()));
    }
}
