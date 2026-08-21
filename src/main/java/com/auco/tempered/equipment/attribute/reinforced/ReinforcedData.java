package com.auco.tempered.equipment.attribute.reinforced;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Persistent Reinforced state for one equipment stack.
 *
 * <p>The original maximum durability is retained so that each level is
 * calculated from the same baseline instead of compounding rounding errors.</p>
 */
public record ReinforcedData(int level, int baseMaxDamage) {

    public static final int MAX_LEVEL = 5;
    public static final int DURABILITY_BONUS_PER_LEVEL_PERCENT = 10;

    public static final Codec<ReinforcedData> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.intRange(1, MAX_LEVEL)
                            .fieldOf("level")
                            .forGetter(ReinforcedData::level),
                    Codec.INT
                            .fieldOf("base_max_damage")
                            .forGetter(ReinforcedData::baseMaxDamage)
            ).apply(instance, ReinforcedData::new));

    public boolean isValid() {
        return level >= 1 && level <= MAX_LEVEL && baseMaxDamage > 0;
    }

    public int durabilityBonusPercent() {
        return level * DURABILITY_BONUS_PER_LEVEL_PERCENT;
    }

    public int reinforcedMaxDamage() {
        return calculateMaxDamage(baseMaxDamage, level);
    }

    public static int calculateMaxDamage(int baseMaxDamage, int level) {
        return Math.round(baseMaxDamage * (1.0F + level * 0.10F));
    }
}
