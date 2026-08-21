package com.auco.tempered.equipment.attribute.reinforced;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Persistent Reinforced state for one equipment stack.
 *
 * <p>A Java {@code record} is a good data-component value because it is
 * immutable and automatically provides {@code equals} and {@code hashCode}.
 * NeoForge can therefore safely store, save, and synchronize it on an
 * {@code ItemStack}.</p>
 *
 * <p>The original maximum durability is retained so every level is calculated
 * from the same baseline instead of compounding rounding errors.</p>
 */
public record ReinforcedData(int level, int baseMaxDamage) {

    public static final int MAX_LEVEL = 5;
    public static final int DURABILITY_BONUS_PER_LEVEL_PERCENT = 10;

    /** Converts this record to and from the item's saved component data. */
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
        // Math.round keeps the result an integer because Minecraft durability
        // is represented as a whole-number maximum damage value.
        return Math.round(baseMaxDamage * (1.0F + level * 0.10F));
    }
}
