package com.auco.tempered.event;

import com.auco.tempered.Tempered;
import com.auco.tempered.equipment.affix.executioner.ExecutionerData;
import com.auco.tempered.service.ExecutionerService;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/** Applies Executioner and advances it through direct hostile melee kills. */
@EventBusSubscriber(modid = Tempered.MODID)
public final class ExecutionerCombatHandler {

    // Run after ordinary damage modifiers so the threshold uses their final
    // post-armor value instead of guessing what a later handler will do.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!ExecutionerService.isEligibleTarget(event.getEntity())) {
            return;
        }

        ItemStack weapon = ExecutionerService.getDirectMeleeWeapon(event.getSource());
        ExecutionerData data = ExecutionerService.getActiveData(weapon);
        if (data == null
                || !ExecutionerService.shouldExecute(event.getEntity(), event.getNewDamage(), data)) {
            return;
        }

        // Modify this damage sequence instead of creating a second hit. Vanilla
        // can then retain attribution, loot, advancements, and Totem handling.
        event.setNewDamage(Math.max(
                event.getNewDamage(),
                ExecutionerService.lethalDamage(event.getEntity())
        ));
    }

    /**
     * LOWEST observes cancellations made by ordinary-priority handlers before
     * mutating the weapon. Canceled death events are not delivered by default.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!ExecutionerService.isEligibleTarget(event.getEntity())
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || player.isCreative()) {
            return;
        }

        ItemStack weapon = ExecutionerService.getDirectMeleeWeapon(event.getSource());
        if (weapon.isEmpty()) {
            return;
        }

        ExecutionerData previous = ExecutionerService.getProgressData(weapon);
        int previousLevel = previous == null ? 0 : previous.level();
        ExecutionerData updated = ExecutionerService.recordHostileKill(weapon);
        if (updated != null && updated.level() > previousLevel) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.tempered.executioner.level_up",
                            toRomanNumeral(updated.level()),
                            weapon.getHoverName()
                    ),
                    false
            );
        }
    }

    private static String toRomanNumeral(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> String.valueOf(level);
        };
    }

    private ExecutionerCombatHandler() {
    }
}
