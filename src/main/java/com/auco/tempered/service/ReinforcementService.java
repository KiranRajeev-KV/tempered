package com.auco.tempered.service;

import com.auco.tempered.equipment.attribute.reinforced.ReinforcedData;
import com.auco.tempered.equipment.attribute.reinforced.ReinforcedRules;
import com.auco.tempered.config.TemperedConfig;
import com.auco.tempered.registry.ModDataComponents;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

/**
 * Contains the rules for inspecting and applying Reinforced.
 *
 * <p>Keeping gameplay rules outside the item class makes them easier to test
 * and reuse later from a Tempering Block or another user interface. ItemStack
 * changes are only made by {@link #apply(ItemStack)} after validation.</p>
 */
public final class ReinforcementService {

    private ReinforcementService() {
    }

    public static ReinforcementResult inspect(ItemStack targetStack) {
        if (!TemperedConfig.active().reinforced().enabled()) {
            return new ReinforcementResult(ReinforcementResult.Status.DISABLED, 0, 0, 0, 0);
        }
        // "All equipment" currently means every item with a durability bar.
        // This naturally includes vanilla tools, weapons, armor, shields, and
        // other damageable equipment without maintaining a large item list.
        if (!targetStack.isDamageableItem()) {
            return new ReinforcementResult(
                    ReinforcementResult.Status.NOT_DAMAGEABLE,
                    0,
                    0,
                    0,
                    0
            );
        }

        ReinforcedData currentData = targetStack.get(
                ModDataComponents.REINFORCED_DATA.get()
        );
        int currentMaxDamage = targetStack.getMaxDamage();

        if (currentData == null) {
            // No component means this is the first application. Capture the
            // current maximum as the permanent baseline for all later levels.
            return successfulResult(0, targetStack.getMaxDamage(), currentMaxDamage);
        }

        if (!currentData.isValid()) {
            return new ReinforcementResult(
                    ReinforcementResult.Status.INVALID_DATA,
                    currentData.level(),
                    currentData.level(),
                    currentMaxDamage,
                    currentMaxDamage
            );
        }

        if (currentData.level() >= TemperedConfig.active().reinforced().maxLevel()) {
            return new ReinforcementResult(
                    ReinforcementResult.Status.MAX_LEVEL,
                    currentData.level(),
                    currentData.level(),
                    currentMaxDamage,
                    currentMaxDamage
            );
        }

        return successfulResult(
                currentData.level(),
                currentData.baseMaxDamage(),
                currentMaxDamage
        );
    }

    public static ReinforcementResult apply(ItemStack targetStack) {
        // Validate immediately before changing the stack. This protects future
        // callers that may invoke apply without first calling inspect.
        ReinforcementResult result = inspect(targetStack);
        if (!result.isSuccess()) {
            return result;
        }

        reconcile(targetStack);
        result = inspect(targetStack);

        ReinforcedData currentData = targetStack.get(
                ModDataComponents.REINFORCED_DATA.get()
        );
        int baseMaxDamage = currentData == null
                ? targetStack.getMaxDamage()
                : currentData.baseMaxDamage();

        // DataComponents.MAX_DAMAGE changes durability for this one ItemStack;
        // it does not change every instance of the underlying Item type.
        targetStack.set(
                ModDataComponents.REINFORCED_DATA.get(),
                new ReinforcedData(result.newLevel(), baseMaxDamage)
        );
        // A nonlinear configuration can reduce durability at the next level.
        int damage = targetStack.getDamageValue();
        if (result.newMaxDamage() < result.previousMaxDamage()) {
            damage = ReinforcedRules.scaledDamage(damage, result.previousMaxDamage(), result.newMaxDamage());
        }
        targetStack.set(DataComponents.MAX_DAMAGE, result.newMaxDamage());
        targetStack.setDamageValue(damage);

        return result;
    }

    private static ReinforcementResult successfulResult(
            int currentLevel,
            int baseMaxDamage,
            int currentMaxDamage
    ) {
        int newLevel = currentLevel + 1;
        return new ReinforcementResult(
                ReinforcementResult.Status.SUCCESS,
                currentLevel,
                newLevel,
                currentMaxDamage,
                ReinforcedRules.maxDamage(baseMaxDamage, newLevel, TemperedConfig.active().reinforced())
        );
    }

    /**
     * Returns the stack's valid Reinforced data, or {@code null} when absent
     * or invalid. Unlike {@link SwiftService#getActiveData(ItemStack)}, no
     * tag gate is applied because Reinforced eligibility is damageability,
     * which the component's presence already implies.
     */
    public static ReinforcedData getActiveData(ItemStack stack) {
        ReinforcedData data = stack.get(ModDataComponents.REINFORCED_DATA.get());
        return TemperedConfig.active().reinforced().enabled() && data != null && data.isValid() ? data : null;
    }
    /** Lazy server-side reconciliation for carried and newly encountered equipment. */
    public static void reconcile(ItemStack stack) {
        ReinforcedData data = stack.get(ModDataComponents.REINFORCED_DATA.get());
        if (data == null || !data.isValid() || !stack.isDamageableItem()) return;
        int oldMaximum = stack.getMaxDamage();
        int newMaximum = ReinforcedRules.maxDamage(data);
        if (oldMaximum == newMaximum) return;
        int newDamage = ReinforcedRules.scaledDamage(stack.getDamageValue(), oldMaximum, newMaximum);
        stack.set(DataComponents.MAX_DAMAGE, newMaximum);
        stack.setDamageValue(newDamage);
    }

}
