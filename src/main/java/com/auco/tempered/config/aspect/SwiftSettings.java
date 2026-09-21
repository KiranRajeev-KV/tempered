package com.auco.tempered.config.aspect;

import java.util.List;
import com.auco.tempered.config.ConfigValues;

/** Total percentage bonuses indexed by level, with a cap independent of saved item levels. */
public record SwiftSettings(boolean enabled, List<Double> bonuses) {
    public static final SwiftSettings DEFAULT = new SwiftSettings(true, List.of(8.0, 16.0, 24.0, 32.0, 40.0));

    public SwiftSettings {
        if (!ConfigValues.numbers(bonuses, 0, ConfigValues.MAX_BONUS_PERCENT, false, false)) {
            throw new IllegalArgumentException("Invalid swift bonus levels");
        }
        bonuses = List.copyOf(bonuses);
    }

    public int maxLevel() { return bonuses.size(); }
    public int effectiveLevel(int savedLevel) { return Math.max(0, Math.min(savedLevel, maxLevel())); }
    public double bonusPercent(int savedLevel) {
        int level = effectiveLevel(savedLevel);
        return enabled && level > 0 ? bonuses.get(level - 1) : 0;
    }
}
