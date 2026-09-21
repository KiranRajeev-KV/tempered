package com.auco.tempered.config.affix;

import java.util.List;
import com.auco.tempered.config.ConfigValues;

public record ExecutionerSettings(
        boolean enabled, boolean progressionEnabled, List<Integer> killsRequired,
        List<Double> healthPercents, double progressChance, int progressPerSuccess,
        List<Double> executeChances, boolean allowCreativeProgression, boolean announceTierUp
) {
    public static final ExecutionerSettings DEFAULT = new ExecutionerSettings(
            true, true, List.of(2, 5, 10), List.of(5.0, 10.0, 15.0),
            1.0, 1, List.of(1.0, 1.0, 1.0), false, true);

    public ExecutionerSettings {
        if (!ConfigValues.numbers(killsRequired, 1, Integer.MAX_VALUE, true, true)
                || !ConfigValues.numbers(healthPercents, 0, 100, false, false)
                || !ConfigValues.numbers(executeChances, 0, 1, false, false)
                || killsRequired.size() != healthPercents.size()
                || killsRequired.size() != executeChances.size()
                || !Double.isFinite(progressChance) || progressChance < 0 || progressChance > 1
                || progressPerSuccess < 1) {
            throw new IllegalArgumentException("Executioner requires equally sized tier lists, increasing positive milestones, health percentages 0-100 and chances 0-1");
        }
        killsRequired = List.copyOf(killsRequired);
        healthPercents = List.copyOf(healthPercents);
        executeChances = List.copyOf(executeChances);
    }

    public int maxLevel() { return killsRequired.size(); }
    public int levelForKills(int kills) {
        if (!enabled) return 0;
        int level = 0;
        while (level < maxLevel() && kills >= killsRequired.get(level)) level++;
        return level;
    }
    public int nextMilestone(int kills) {
        return killsRequired.get(Math.min(levelForKills(kills), maxLevel() - 1));
    }
    public double healthPercent(int kills) {
        int level = levelForKills(kills);
        return level == 0 ? 0 : healthPercents.get(level - 1);
    }
    public double executeChance(int kills) {
        int level = levelForKills(kills);
        return level == 0 ? 0 : executeChances.get(level - 1);
    }
    public int advance(int kills) {
        // Keep earning history beyond the current top tier, so raised milestones remain meaningful.
        return (int) Math.min(Integer.MAX_VALUE, (long) kills + progressPerSuccess);
    }
}
