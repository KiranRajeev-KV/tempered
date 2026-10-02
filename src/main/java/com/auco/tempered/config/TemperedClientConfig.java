package com.auco.tempered.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Local visual preferences; never synchronized with the server. */
public final class TemperedClientConfig {
    public static final String FILE_NAME = "tempered/client.toml";
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue CUSTOM_CHESTS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.translation("tempered.config.rendering").push("rendering");
        CUSTOM_CHESTS = builder
                .comment("Use the custom treasure chest model for ordinary single and double chests, including items. "
                        + "Trapped and Ender chests are unaffected. Changes apply immediately.")
                .translation("tempered.config.rendering.custom_chests")
                .define("custom_chests", false);
        builder.pop();
        SPEC = builder.build();
    }

    public static boolean customChests() {
        return CUSTOM_CHESTS.get();
    }

    private TemperedClientConfig() {}
}
