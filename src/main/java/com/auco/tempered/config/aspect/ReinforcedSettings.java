package com.auco.tempered.config.aspect;

import java.util.List;
import com.auco.tempered.config.ConfigValues;

/** Total percentage bonuses indexed by level, with a cap independent of saved item levels. */
public record ReinforcedSettings(boolean enabled, List<Double> bonuses) {
    public static final ReinforcedSettings DEFAULT = new ReinforcedSettings(true, List.of(10.0, 20.0, 30.0, 40.0, 50.0));

    public ReinforcedSettings {
        if (!ConfigValues.numbers(bonuses, 0, ConfigValues.MAX_BONUS_PERCENT, false, false)) {
            throw new IllegalArgumentException("Invalid reinforced bonus levels");
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
