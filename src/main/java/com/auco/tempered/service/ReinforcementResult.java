package com.auco.tempered.service;

/**
 * Immutable description of a reinforcement attempt.
 *
 * <p>Returning a result instead of showing messages inside the service keeps
 * the rules reusable. The item decides how each result is presented to a
 * player.</p>
 */
public record ReinforcementResult(
        Status status,
        int previousLevel,
        int newLevel,
        int previousMaxDamage,
        int newMaxDamage
) {
    /** The possible outcomes before an item stack is changed. */
    public enum Status {
        SUCCESS,
        DISABLED,
        NOT_DAMAGEABLE,
        INVALID_DATA,
        MAX_LEVEL
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
}
