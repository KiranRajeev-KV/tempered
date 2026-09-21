package com.auco.tempered.equipment.attribute.swift;

import com.auco.tempered.config.TemperedConfig;

public final class SwiftRules {
    public static int level(SwiftData data) { return TemperedConfig.active().swift().effectiveLevel(data.level()); }
    public static double bonusPercent(SwiftData data) { return TemperedConfig.active().swift().bonusPercent(data.level()); }
    public static float multiplier(SwiftData data) { return (float) (1 + bonusPercent(data) / 100); }
    private SwiftRules() {}
}
