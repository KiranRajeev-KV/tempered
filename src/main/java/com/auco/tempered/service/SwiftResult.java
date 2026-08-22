package com.auco.tempered.service;

/**
 * Immutable description of a Swift Aspect application attempt.
 *
 * <p>The service reports rules through this value; the item class decides how
 * to present that result to the player. This keeps gameplay logic separate
 * from chat messages and sounds.</p>
 */
public record SwiftResult(Status status, int previousLevel, int newLevel) {

    public static SwiftResult success(int previousLevel, int newLevel) {
        return new SwiftResult(Status.SUCCESS, previousLevel, newLevel);
    }

    public static SwiftResult failure(Status status) {
        return new SwiftResult(status, 0, 0);
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public enum Status {
        SUCCESS,
        NOT_SWIFT_APPLICABLE,
        MAX_LEVEL,
        INVALID_DATA
    }
}
