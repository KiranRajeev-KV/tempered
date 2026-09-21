package com.auco.tempered.config;

import com.auco.tempered.config.aspect.ReinforcedSettings;
import com.auco.tempered.config.aspect.SwiftSettings;
import com.auco.tempered.config.affix.ExecutionerSettings;

/** One immutable snapshot is shared by calculations, previews and presentation. */
public record GameplaySettings(ReinforcedSettings reinforced, SwiftSettings swift, ExecutionerSettings executioner) {
    public static final GameplaySettings DEFAULT = new GameplaySettings(
            ReinforcedSettings.DEFAULT, SwiftSettings.DEFAULT, ExecutionerSettings.DEFAULT);
}
