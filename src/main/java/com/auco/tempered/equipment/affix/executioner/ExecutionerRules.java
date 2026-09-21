package com.auco.tempered.equipment.affix.executioner;

import com.auco.tempered.config.TemperedConfig;

public final class ExecutionerRules {
    public static int level(ExecutionerData data) { return TemperedConfig.active().executioner().levelForKills(data.hostileKills()); }
    public static boolean unlocked(ExecutionerData data) { return level(data) > 0; }
    public static double healthPercent(ExecutionerData data) { return TemperedConfig.active().executioner().healthPercent(data.hostileKills()); }
    public static double executeChance(ExecutionerData data) { return TemperedConfig.active().executioner().executeChance(data.hostileKills()); }
    public static int nextMilestone(ExecutionerData data) { return TemperedConfig.active().executioner().nextMilestone(data.hostileKills()); }
    public static int maxLevel() { return TemperedConfig.active().executioner().maxLevel(); }
    private ExecutionerRules() {}
}
