package com.auco.tempered.service;

import com.auco.tempered.equipment.affix.executioner.ExecutionerData;
import com.auco.tempered.equipment.affix.executioner.ExecutionerRules;
import com.auco.tempered.config.TemperedConfig;
import com.auco.tempered.config.affix.ExecutionerSettings;
import com.auco.tempered.registry.ModDataComponents;
import com.auco.tempered.tag.ModEntityTypeTags;
import com.auco.tempered.tag.ModItemTags;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;

/** Server-authoritative progression and combat rules for Executioner. */
public final class ExecutionerService {

    /**
     * Returns the attacking player's eligible main-hand weapon for a direct
     * player-attack damage source. Vanilla sweeping attacks use this same type;
     * reflected damage and custom attributed sources must not count as melee.
     */
    public static ItemStack getDirectMeleeWeapon(DamageSource source) {
        if (!source.is(DamageTypes.PLAYER_ATTACK)
                || !(source.getEntity() instanceof ServerPlayer player)
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
        if (!TemperedConfig.active().executioner().enabled() || !stack.is(ModItemTags.EXECUTIONER_APPLICABLE)) {
            return null;
        }

        ExecutionerData data = stack.get(ModDataComponents.EXECUTIONER_DATA.get());
        return data != null && data.isValid() ? data : null;
    }

    /** Returns data only after the affix has reached Tier I. */
    public static ExecutionerData getActiveData(ItemStack stack) {
        ExecutionerData data = getProgressData(stack);
        return data != null && ExecutionerRules.unlocked(data) ? data : null;
    }

    /**
     * Records one direct hostile kill and returns the new value. The component
     * preserves history beyond the current top tier and saturates at the integer limit.
     */
    public static ExecutionerData recordHostileKill(ItemStack weapon) {
        ExecutionerProgressResult result = advanceProgress(weapon, TemperedConfig.active().executioner());
        return result == null ? null : result.updated();
    }

    /** Applies one successful award using the settings captured for that death. */
    public static ExecutionerProgressResult advanceProgress(ItemStack weapon, ExecutionerSettings settings) {
        if (!settings.enabled() || !settings.progressionEnabled() || !weapon.is(ModItemTags.EXECUTIONER_APPLICABLE)) {
            return null;
        }

        ExecutionerData current = weapon.get(ModDataComponents.EXECUTIONER_DATA.get());
        if (current != null && !current.isValid()) {
            return null;
        }
        int previous = current == null ? 0 : current.hostileKills();
        ExecutionerData updated = new ExecutionerData(settings.advance(previous));
        if (!updated.equals(current)) weapon.set(ModDataComponents.EXECUTIONER_DATA.get(), updated);
        return new ExecutionerProgressResult(previous, updated,
                settings.levelForKills(previous), settings.levelForKills(updated.hostileKills()));
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
        if (damageBeforeAbsorption <= 0.0F || !ExecutionerRules.unlocked(data)) {
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
                * (float) ExecutionerRules.healthPercent(data)
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
