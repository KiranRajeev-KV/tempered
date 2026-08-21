package com.auco.tempered.service;

import com.auco.tempered.equipment.attribute.reinforced.ReinforcedData;
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

        if (currentData.level() >= ReinforcedData.MAX_LEVEL) {
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
        targetStack.set(DataComponents.MAX_DAMAGE, result.newMaxDamage());

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
                ReinforcedData.calculateMaxDamage(baseMaxDamage, newLevel)
        );
    }
}
