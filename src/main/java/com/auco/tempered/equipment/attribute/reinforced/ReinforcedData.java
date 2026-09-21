package com.auco.tempered.equipment.attribute.reinforced;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Persistent history, independent of configurable gameplay caps and balance. */
public record ReinforcedData(int level, int baseMaxDamage) {
    public static final Codec<ReinforcedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("level").forGetter(ReinforcedData::level),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("base_max_damage").forGetter(ReinforcedData::baseMaxDamage)
    ).apply(instance, ReinforcedData::new));

    public boolean isValid() { return level > 0 && baseMaxDamage > 0; }
}
