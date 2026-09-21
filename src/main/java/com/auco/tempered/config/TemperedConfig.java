package com.auco.tempered.config;

import com.auco.tempered.config.aspect.ReinforcedConfig;
import com.auco.tempered.config.aspect.SwiftConfig;
import com.auco.tempered.config.affix.ExecutionerConfig;
import com.auco.tempered.config.affix.ExecutionerSettings;
import com.mojang.logging.LogUtils;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Only this adapter reads NeoForge config values. Gameplay reads an active snapshot. */
public final class TemperedConfig {
    public static final String FILE_NAME = "tempered/config.toml";
    private static final TemperedConfig CONFIG;
    public static final ModConfigSpec SPEC;
    private static volatile GameplaySettings active = GameplaySettings.DEFAULT;

    static {
        var pair = new ModConfigSpec.Builder().configure(TemperedConfig::new);
        CONFIG = pair.getLeft();
        SPEC = pair.getRight();
    }

    private final ReinforcedConfig reinforced;
    private final SwiftConfig swift;
    private final ExecutionerConfig executioner;

    private TemperedConfig(ModConfigSpec.Builder builder) {
        builder.translation("tempered.config.aspects").push("aspects").pop();
        builder.translation("tempered.config.affixes").push("affixes").pop();
        reinforced = new ReinforcedConfig(builder);
        swift = new SwiftConfig(builder);
        executioner = new ExecutionerConfig(builder);
    }

    public static GameplaySettings active() { return active; }

    public static void activate(GameplaySettings settings) { active = settings; }

    public static void load() {
        ExecutionerSettings executionerSettings;
        try {
            executionerSettings = CONFIG.executioner.read();
        } catch (IllegalArgumentException exception) {
            LogUtils.getLogger().error("Invalid affixes.executioner section: {}. Using the complete default Executioner section until restart.", exception.getMessage());
            executionerSettings = ExecutionerSettings.DEFAULT;
        }
        activate(new GameplaySettings(CONFIG.reinforced.read(), CONFIG.swift.read(), executionerSettings));
    }

}
