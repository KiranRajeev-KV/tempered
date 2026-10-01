package com.auco.tempered.client.guide;

import com.auco.tempered.Tempered;
import com.auco.tempered.presentation.GuideTopic;
import guideme.GuidesCommon;
import guideme.PageAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

/** Local, read-only help available to every player, including worlds with cheats disabled. */
@EventBusSubscriber(modid = Tempered.MODID, value = Dist.CLIENT)
public final class GuideCommandHandler {
    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        var guide = Commands.literal("guide").executes(context -> open(GuideTopic.OVERVIEW));
        for (GuideTopic topic : GuideTopic.values()) {
            guide.then(Commands.literal(topic.id()).executes(context -> open(topic)));
        }
        event.getDispatcher().register(Commands.literal("tempered").then(guide));
    }

    private static int open(GuideTopic topic) {
        // ChatScreen closes itself after executing a command. Open on the next client turn.
        Minecraft.getInstance().tell(() -> {
            var player = Minecraft.getInstance().player;
            if (player != null) GuidesCommon.openGuide(player, TemperedGuide.ID, PageAnchor.page(topic.pageId()));
        });
        return 1;
    }

    private GuideCommandHandler() {}
}
