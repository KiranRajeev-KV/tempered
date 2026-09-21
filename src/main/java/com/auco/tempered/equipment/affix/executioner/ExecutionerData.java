package com.auco.tempered.equipment.affix.executioner;

import com.mojang.serialization.Codec;

/** Saved progress remains valid independently of configured milestones. */
public record ExecutionerData(int hostileKills) {
    public static final Codec<ExecutionerData> CODEC = Codec.intRange(1, Integer.MAX_VALUE)
            .xmap(ExecutionerData::new, ExecutionerData::hostileKills);
    public boolean isValid() { return hostileKills > 0; }
}
