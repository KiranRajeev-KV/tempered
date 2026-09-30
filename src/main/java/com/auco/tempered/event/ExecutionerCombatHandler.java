package com.auco.tempered.event;

import com.auco.tempered.Tempered;
import com.auco.tempered.equipment.affix.executioner.ExecutionerData;
import com.auco.tempered.service.ExecutionerService;
import com.auco.tempered.equipment.affix.executioner.ExecutionerRules;
import com.auco.tempered.util.Probability;

import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/** Applies Executioner inside the original melee damage sequence. */
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

        if (!Probability.succeeds(ExecutionerRules.executeChance(data), event.getEntity().getRandom()::nextDouble)) return;

        // Modify this damage sequence instead of creating a second hit. Vanilla
        // can then retain attribution, loot, advancements, and Totem handling.
        event.setNewDamage(Math.max(
                event.getNewDamage(),
                ExecutionerService.lethalDamage(event.getEntity())
        ));
    }

    private ExecutionerCombatHandler() {
    }
}
