package com.auco.tempered.service;

import com.auco.tempered.equipment.affix.executioner.ExecutionerData;

/** Outcome of a validated progression award, including multi-tier advances. */
public record ExecutionerProgressResult(
        int previousProgress,
        ExecutionerData updated,
        int previousTier,
        int updatedTier
) {
    public boolean tierIncreased() {
        return updatedTier > previousTier;
    }
}
