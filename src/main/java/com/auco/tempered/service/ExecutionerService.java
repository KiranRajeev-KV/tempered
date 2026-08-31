package com.auco.tempered.service;

import com.auco.tempered.equipment.affix.executioner.ExecutionerData;
import com.auco.tempered.registry.ModDataComponents;
import com.auco.tempered.tag.ModEntityTypeTags;
import com.auco.tempered.tag.ModItemTags;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;

/** Server-authoritative progression and combat rules for Executioner. */
public final class ExecutionerService {

    /**
     * Returns the attacking player's eligible main-hand weapon for a direct
     * melee damage source. Projectiles and damage-over-time sources fail the
     * identity check because their direct entity is not the player.
     */
    public static ItemStack getDirectMeleeWeapon(DamageSource source) {
        if (!(source.getEntity() instanceof ServerPlayer player)
                || source.getDirectEntity() != player) {
            return ItemStack.EMPTY;
        }

        ItemStack weapon = player.getMainHandItem();
        return weapon.is(ModItemTags.EXECUTIONER_APPLICABLE)
                ? weapon
                : ItemStack.EMPTY;
    }

    public static boolean isHostileTarget(LivingEntity target) {
        return target instanceof Enemy;
    }

    public static boolean isImmuneTarget(LivingEntity target) {
        return target.getType().is(ModEntityTypeTags.EXECUTIONER_IMMUNE);
    }

    /** Hostile mobs are valid by default; datapacks may blacklist exceptions. */
    public static boolean isEligibleTarget(LivingEntity target) {
        return isHostileTarget(target) && !isImmuneTarget(target);
    }

    /** Returns valid progression data, including progress before Tier I. */
    public static ExecutionerData getProgressData(ItemStack stack) {
        if (!stack.is(ModItemTags.EXECUTIONER_APPLICABLE)) {
            return null;
        }

        ExecutionerData data = stack.get(ModDataComponents.EXECUTIONER_DATA.get());
        return data != null && data.isValid() ? data : null;
    }

    /** Returns data only after the affix has reached Tier I. */
    public static ExecutionerData getActiveData(ItemStack stack) {
        ExecutionerData data = getProgressData(stack);
        return data != null && data.isUnlocked() ? data : null;
    }

    /**
     * Records one direct hostile kill and returns the new value. The component
     * is capped at Tier III so maxed weapons do not receive pointless updates.
     */
    public static ExecutionerData recordHostileKill(ItemStack weapon) {
        if (!weapon.is(ModItemTags.EXECUTIONER_APPLICABLE)) {
            return null;
        }

        ExecutionerData current = weapon.get(ModDataComponents.EXECUTIONER_DATA.get());
        if (current == null) {
            ExecutionerData firstKill = new ExecutionerData(1);
            weapon.set(ModDataComponents.EXECUTIONER_DATA.get(), firstKill);
            return firstKill;
        }
        if (!current.isValid()) {
            return null;
        }
        if (current.hostileKills() >= ExecutionerData.MAX_TRACKED_KILLS) {
            return current;
        }

        ExecutionerData updated = current.withOneMoreKill();
        weapon.set(ModDataComponents.EXECUTIONER_DATA.get(), updated);
        return updated;
    }

    /**
     * Checks the projected health after armor, enchantment, potion, and
     * absorption reductions. Exact threshold values count as executable.
     */
    public static boolean shouldExecute(
            LivingEntity target,
            float damageBeforeAbsorption,
            ExecutionerData data
    ) {
        if (damageBeforeAbsorption <= 0.0F || !data.isUnlocked()) {
            return false;
        }

        float healthDamage = Math.max(
                0.0F,
                damageBeforeAbsorption - target.getAbsorptionAmount()
        );
        if (healthDamage <= 0.0F) {
            return false;
        }

        float projectedHealth = target.getHealth() - healthDamage;
        float executeThreshold = target.getMaxHealth()
                * data.executeHealthPercent()
                / 100.0F;

        // A naturally lethal hit needs no adjustment and is not an execution.
        return projectedHealth > 0.0F && projectedHealth <= executeThreshold;
    }

    /** Damage required for the current hit to consume absorption and health. */
    public static float lethalDamage(LivingEntity target) {
        return target.getAbsorptionAmount() + target.getHealth();
    }

    private ExecutionerService() {
    }
}
