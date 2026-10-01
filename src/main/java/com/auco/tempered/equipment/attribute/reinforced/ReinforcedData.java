package com.auco.tempered.equipment.attribute.reinforced;

import java.util.Objects;
import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

/** Persistent history, independent of configurable gameplay caps and balance. */
public record ReinforcedData(int level, int baseMaxDamage, Optional<ResourceLocation> baselineItem) {
    public static final int FORMAT_VERSION = 2;
    public static final Codec<ReinforcedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            // Missing version means the original format. A present invalid version must fail, not reset history.
            Codec.intRange(1, FORMAT_VERSION).optionalFieldOf("format_version", 1)
                    .forGetter((ReinforcedData data) -> FORMAT_VERSION),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("level").forGetter(ReinforcedData::level),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("base_max_damage").forGetter(ReinforcedData::baseMaxDamage),
            ResourceLocation.CODEC.optionalFieldOf("baseline_item").forGetter(ReinforcedData::baselineItem)
    ).apply(instance, (version, level, maximum, item) -> new ReinforcedData(level, maximum, item)));

    public ReinforcedData {
        Objects.requireNonNull(baselineItem);
    }

    /** Legacy baselines have no reliable owner; keep the recorded durability without guessing. */
    public ReinforcedData(int level, int baseMaxDamage) {
        this(level, baseMaxDamage, Optional.empty());
    }

    public ReinforcedData(int level, int baseMaxDamage, ResourceLocation baselineItem) {
        this(level, baseMaxDamage, Optional.of(baselineItem));
    }

    public boolean isValid() { return level > 0 && baseMaxDamage > 0; }
}
