package com.auco.tempered.attributes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ReinforcedData(int level, int baseMaxDamage) {
    public static final ReinforcedData DEFAULT = new ReinforcedData(0, 0);

    public static final Codec<ReinforcedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("level").forGetter(ReinforcedData::level),
            Codec.INT.fieldOf("baseMaxDamage").forGetter(ReinforcedData::baseMaxDamage)
    ).apply(instance, ReinforcedData::new));
}
