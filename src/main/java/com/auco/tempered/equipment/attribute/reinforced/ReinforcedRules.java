package com.auco.tempered.equipment.attribute.reinforced;

import com.auco.tempered.config.TemperedConfig;
import com.auco.tempered.config.aspect.ReinforcedSettings;

public final class ReinforcedRules {
    public static int level(ReinforcedData data) { return TemperedConfig.active().reinforced().effectiveLevel(data.level()); }
    public static double bonusPercent(ReinforcedData data) { return TemperedConfig.active().reinforced().bonusPercent(data.level()); }
    public static int maxDamage(ReinforcedData data) { return maxDamage(data.baseMaxDamage(), data.level(), TemperedConfig.active().reinforced()); }

    public static int maxDamage(int baseline, int level, ReinforcedSettings settings) {
        return (int) Math.max(1, Math.min(Integer.MAX_VALUE, Math.round(baseline * (1 + settings.bonusPercent(level) / 100))));
    }

    /** Rebalancing preserves the fraction of durability remaining, never breaking an item. */
    public static int scaledDamage(int damage, int oldMaximum, int newMaximum) {
        if (oldMaximum <= 0) return 0;
        return (int) Math.max(0, Math.min(newMaximum - 1L, Math.round((double) damage / oldMaximum * newMaximum)));
    }
    private ReinforcedRules() {}
}
