package com.auco.tempered.equipment.attribute.swift;

import com.mojang.serialization.Codec;

/** Saved level remains valid even when a configured cap is lowered. */
public record SwiftData(int level) {
    public static final Codec<SwiftData> CODEC = Codec.intRange(1, Integer.MAX_VALUE).xmap(SwiftData::new, SwiftData::level);
    public boolean isValid() { return level > 0; }
}
