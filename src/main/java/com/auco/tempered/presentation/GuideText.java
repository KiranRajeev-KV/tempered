package com.auco.tempered.presentation;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** Translated explanations shared by tooltips and the guide's dynamic sections. */
public final class GuideText {
    public static MutableComponent text(String key, Object... arguments) {
        return Component.translatable("guide.tempered." + key, arguments);
    }

    public static List<Component> smithingSteps() {
        return List.of(text("smithing.template"), text("smithing.base"), text("smithing.addition"), text("smithing.consume"));
    }

    private GuideText() {}
}
