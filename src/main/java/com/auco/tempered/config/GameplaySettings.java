package com.auco.tempered.config;

import com.auco.tempered.config.aspect.ReinforcedSettings;
import com.auco.tempered.config.aspect.SwiftSettings;
import com.auco.tempered.config.affix.ExecutionerSettings;
import com.auco.tempered.config.acquisition.AcquisitionSettings;

/** One immutable snapshot is shared by calculations, previews and presentation. */
public record GameplaySettings(ReinforcedSettings reinforced, SwiftSettings swift, ExecutionerSettings executioner,
                               AcquisitionSettings acquisition) {
    public static final GameplaySettings DEFAULT = new GameplaySettings(
            ReinforcedSettings.DEFAULT, SwiftSettings.DEFAULT, ExecutionerSettings.DEFAULT, AcquisitionSettings.DEFAULT);

    public GameplaySettings(ReinforcedSettings reinforced, SwiftSettings swift, ExecutionerSettings executioner) {
        this(reinforced, swift, executioner, AcquisitionSettings.DEFAULT);
    }
}
