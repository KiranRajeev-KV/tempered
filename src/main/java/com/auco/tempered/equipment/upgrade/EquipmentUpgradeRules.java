package com.auco.tempered.equipment.upgrade;

import com.auco.tempered.config.equipment.EquipmentUpgradeSettings.DamagePolicy;
import com.auco.tempered.equipment.attribute.reinforced.ReinforcedRules;

/** Calculations shared by recipe previews and committed results; never mutates a stack. */
public final class EquipmentUpgradeRules {
    public static int carriedDamage(int damage, int oldMaximum, int newMaximum, DamagePolicy policy) {
        if (policy == DamagePolicy.REMAINING_FRACTION) {
            return ReinforcedRules.scaledDamage(damage, oldMaximum, newMaximum);
        }
        return Math.max(0, Math.min(damage, newMaximum - 1));
    }

    private EquipmentUpgradeRules() {}
}
