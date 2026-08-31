package com.auco.tempered.equipment.affix.executioner;

import com.mojang.serialization.Codec;

/** Persistent kill progression for the Executioner affix on one weapon. */
public record ExecutionerData(int hostileKills) {

    public static final int MAX_LEVEL = 3;
    public static final int EXECUTE_PERCENT_PER_LEVEL = 5;

    public static final int TIER_ONE_KILLS = 2;
    public static final int TIER_TWO_KILLS = 5;
    public static final int TIER_THREE_KILLS = 10;
    public static final int MAX_TRACKED_KILLS = TIER_THREE_KILLS;

    /** Saves the bounded kill count directly as the component value. */
    public static final Codec<ExecutionerData> CODEC = Codec.intRange(1, MAX_TRACKED_KILLS)
            .xmap(ExecutionerData::new, ExecutionerData::hostileKills);

    public boolean isValid() {
        return hostileKills >= 1 && hostileKills <= MAX_TRACKED_KILLS;
    }

    /** Returns zero while the weapon is still progressing toward Tier I. */
    public int level() {
        return levelForKills(hostileKills);
    }

    public boolean isUnlocked() {
        return level() > 0;
    }

    public int executeHealthPercent() {
        return level() * EXECUTE_PERCENT_PER_LEVEL;
    }

    /** Returns the next milestone, or the Tier III cap when fully progressed. */
    public int nextMilestoneKills() {
        return switch (level()) {
            case 0 -> TIER_ONE_KILLS;
            case 1 -> TIER_TWO_KILLS;
            default -> TIER_THREE_KILLS;
        };
    }

    public ExecutionerData withOneMoreKill() {
        return new ExecutionerData(Math.min(hostileKills + 1, MAX_TRACKED_KILLS));
    }

    public static int levelForKills(int hostileKills) {
        if (hostileKills >= TIER_THREE_KILLS) {
            return 3;
        }
        if (hostileKills >= TIER_TWO_KILLS) {
            return 2;
        }
        if (hostileKills >= TIER_ONE_KILLS) {
            return 1;
        }
        return 0;
    }
}
