package com.auco.tempered.util;

import java.util.function.DoubleSupplier;

/** A single roll, with deterministic boundaries that do not consume random numbers. */
public final class Probability {
    public static boolean succeeds(double chance, DoubleSupplier random) {
        if (!Double.isFinite(chance) || chance < 0 || chance > 1) {
            throw new IllegalArgumentException("Probability must be finite and between 0 and 1");
        }
        return chance >= 1 || (chance > 0 && random.getAsDouble() < chance);
    }
    private Probability() {}
}
