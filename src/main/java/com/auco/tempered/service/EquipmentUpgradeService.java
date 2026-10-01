package com.auco.tempered.service;

import com.auco.tempered.config.GameplaySettings;
import com.auco.tempered.equipment.attribute.reinforced.ReinforcedData;
import com.auco.tempered.equipment.attribute.reinforced.ReinforcedRules;
import com.auco.tempered.equipment.upgrade.EquipmentUpgradeRules;
import com.auco.tempered.registry.ModDataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/** Adjusts only Tempered history and durability on a recipe's already assembled output. */
public final class EquipmentUpgradeService {
    /** Presence, rather than active effects, protects paused and disabled history too. */
    public static boolean hasHistory(ItemStack stack) {
        return stack.has(ModDataComponents.REINFORCED_DATA.get())
                || stack.has(ModDataComponents.SWIFT_DATA.get())
                || stack.has(ModDataComponents.EXECUTIONER_DATA.get());
    }

    public static ItemStack transform(ItemStack base, ItemStack recipeResult, ItemStack output, GameplaySettings settings) {
        if (output.isEmpty() || base.is(output.getItem()) || !hasHistory(base)) return output;

        copyHistory(base, output, ModDataComponents.REINFORCED_DATA.get());
        copyHistory(base, output, ModDataComponents.SWIFT_DATA.get());
        copyHistory(base, output, ModDataComponents.EXECUTIONER_DATA.get());

        // Use the destination template, including explicit recipe components. The assembled
        // output may still contain the source item's inherited MAX_DAMAGE override.
        int baseline = recipeResult.getMaxDamage();
        if (baseline <= 0) {
            // A non-damageable destination must not become damageable just because the
            // source carried a Reinforced override. Keep history dormant on that item.
            output.remove(DataComponents.MAX_DAMAGE);
            output.remove(DataComponents.DAMAGE);
            return output;
        }
        int maximum = baseline;
        ReinforcedData reinforced = base.get(ModDataComponents.REINFORCED_DATA.get());
        if (reinforced != null && reinforced.isValid()) {
            output.set(ModDataComponents.REINFORCED_DATA.get(), new ReinforcedData(reinforced.level(), baseline,
                    BuiltInRegistries.ITEM.getKey(output.getItem())));
            maximum = ReinforcedRules.maxDamage(baseline, reinforced.level(), settings.reinforced());
        }
        output.set(DataComponents.MAX_DAMAGE, maximum);
        output.setDamageValue(EquipmentUpgradeRules.carriedDamage(base.getDamageValue(), base.getMaxDamage(), maximum,
                settings.upgrades().damagePolicy()));
        return output;
    }

    private static <T> void copyHistory(ItemStack base, ItemStack output, DataComponentType<T> type) {
        T data = base.get(type);
        if (data != null) output.set(type, data);
    }

    private EquipmentUpgradeService() {}
}
