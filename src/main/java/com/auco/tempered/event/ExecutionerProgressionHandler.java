package com.auco.tempered.event;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.auco.tempered.Tempered;
import com.auco.tempered.config.TemperedConfig;
import com.auco.tempered.config.affix.ExecutionerSettings;
import com.auco.tempered.service.ExecutionerService;
import com.auco.tempered.util.Probability;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Defers awards until all death listeners have decided whether death proceeds. */
@EventBusSubscriber(modid = Tempered.MODID)
public final class ExecutionerProgressionHandler {
    // Accessed only on the logical server thread. Drained every tick; never saved.
    private static final Map<MinecraftServer, Map<UUID, PendingAward>> PENDING = new IdentityHashMap<>();

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onLivingDeath(LivingDeathEvent event) {
        var settings = TemperedConfig.active().executioner();
        if (!settings.enabled() || !settings.progressionEnabled()
                || !ExecutionerService.isEligibleTarget(event.getEntity())
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || (player.isCreative() && !settings.allowCreativeProgression())) return;

        ItemStack weapon = ExecutionerService.getDirectMeleeWeapon(event.getSource());
        if (weapon.isEmpty()) return;
        // Retain the original stack, rather than consulting a possibly changed hand at commit time.
        PENDING.computeIfAbsent(player.server, ignored -> new LinkedHashMap<>())
                .compute(event.getEntity().getUUID(), (ignored, previous) ->
                        previous == null || previous.death().isCanceled()
                                ? new PendingAward(event, player, weapon, settings) : previous);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerTick(ServerTickEvent.Post event) {
        var awards = PENDING.remove(event.getServer());
        if (awards == null) return;
        for (PendingAward award : awards.values()) {
            if (award.death().isCanceled()
                    || !Probability.succeeds(award.settings().progressChance(), award.player().getRandom()::nextDouble)) continue;
            var result = ExecutionerService.advanceProgress(award.weapon(), award.settings());
            if (result != null && result.tierIncreased() && award.settings().announceTierUp()) {
                award.player().displayClientMessage(Component.translatable(
                        "message.tempered.executioner.level_up", tierLabel(result.updatedTier()),
                        award.weapon().getHoverName()), false);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PENDING.remove(event.getServer());
    }

    private static String tierLabel(int tier) {
        return switch (tier) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> Integer.toString(tier);
        };
    }

    private record PendingAward(LivingDeathEvent death, ServerPlayer player,
            ItemStack weapon, ExecutionerSettings settings) {}

    private ExecutionerProgressionHandler() {}
}
