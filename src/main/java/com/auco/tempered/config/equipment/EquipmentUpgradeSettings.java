package com.auco.tempered.config.equipment;

import java.util.Objects;

/** Material upgrades use one server-authoritative wear policy. */
public record EquipmentUpgradeSettings(DamagePolicy damagePolicy) {
    public static final EquipmentUpgradeSettings DEFAULT = new EquipmentUpgradeSettings(DamagePolicy.REMAINING_FRACTION);

    public EquipmentUpgradeSettings {
        Objects.requireNonNull(damagePolicy);
    }

    public enum DamagePolicy {
        REMAINING_FRACTION("remaining_fraction"),
        DAMAGE_POINTS("damage_points");

        private final String id;

        DamagePolicy(String id) { this.id = id; }

        public String id() { return id; }

        public static DamagePolicy fromId(String id) {
            for (var policy : values()) {
                if (policy.id.equals(id)) return policy;
            }
            throw new IllegalArgumentException("Unknown equipment upgrade damage policy: " + id);
        }

        public static boolean isValid(Object value) {
            for (var policy : values()) {
                if (policy.id.equals(value)) return true;
            }
            return false;
        }
    }
}
