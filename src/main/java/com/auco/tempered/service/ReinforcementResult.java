package com.auco.tempered.service;

/** Result of inspecting or applying a Reinforced upgrade. */
public record ReinforcementResult(
        Status status,
        int previousLevel,
        int newLevel,
        int previousMaxDamage,
        int newMaxDamage
) {
    public enum Status {
        SUCCESS,
        NOT_DAMAGEABLE,
        INVALID_DATA,
        MAX_LEVEL
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
}
