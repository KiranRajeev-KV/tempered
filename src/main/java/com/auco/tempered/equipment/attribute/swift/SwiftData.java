package com.auco.tempered.equipment.attribute.swift;

import com.mojang.serialization.Codec;

/**
 * Persistent Swift state for one mining-tool stack.
 *
 * <p>Unlike Reinforced, Swift does not need to remember a baseline value:
 * mining speed is calculated at block-breaking time. The level alone is
 * therefore enough to reproduce the bonus after saving and loading.</p>
 */
public record SwiftData(int level) {

    public static final int MAX_LEVEL = 5;
    public static final int MINING_SPEED_BONUS_PER_LEVEL_PERCENT = 8;

    /** Saves this one-value record directly as its level number. */
    public static final Codec<SwiftData> CODEC = Codec.intRange(1, MAX_LEVEL)
            .xmap(SwiftData::new, SwiftData::level);

    public boolean isValid() {
        return level >= 1 && level <= MAX_LEVEL;
    }

    public int miningSpeedBonusPercent() {
        return level * MINING_SPEED_BONUS_PER_LEVEL_PERCENT;
    }

    public float miningSpeedMultiplier() {
        return 1.0F + miningSpeedBonusPercent() / 100.0F;
    }
}
