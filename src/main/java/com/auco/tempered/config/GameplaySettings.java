package com.auco.tempered.config;

import com.auco.tempered.config.aspect.ReinforcedSettings;
import com.auco.tempered.config.aspect.SwiftSettings;
import com.auco.tempered.config.affix.ExecutionerSettings;
import com.auco.tempered.config.acquisition.AcquisitionSettings;
import com.auco.tempered.config.equipment.EquipmentUpgradeSettings;

/** One immutable snapshot is shared by calculations, previews and presentation. */
public record GameplaySettings(ReinforcedSettings reinforced, SwiftSettings swift, ExecutionerSettings executioner,
                               AcquisitionSettings acquisition, EquipmentUpgradeSettings upgrades) {
    public static final GameplaySettings DEFAULT = new GameplaySettings(
            ReinforcedSettings.DEFAULT, SwiftSettings.DEFAULT, ExecutionerSettings.DEFAULT, AcquisitionSettings.DEFAULT,
            EquipmentUpgradeSettings.DEFAULT);

    public GameplaySettings(ReinforcedSettings reinforced, SwiftSettings swift, ExecutionerSettings executioner,
                            AcquisitionSettings acquisition) {
        this(reinforced, swift, executioner, acquisition, EquipmentUpgradeSettings.DEFAULT);
    }

    public GameplaySettings(ReinforcedSettings reinforced, SwiftSettings swift, ExecutionerSettings executioner) {
        this(reinforced, swift, executioner, AcquisitionSettings.DEFAULT);
    }
}
