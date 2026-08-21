package com.auco.tempered.service;

import com.auco.tempered.equipment.attribute.reinforced.ReinforcedData;
import com.auco.tempered.registry.ModDataComponents;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

/** Contains the server-authoritative rules for applying Reinforced. */
public final class ReinforcementService {

    private ReinforcementService() {
    }

    public static ReinforcementResult inspect(ItemStack targetStack) {
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
