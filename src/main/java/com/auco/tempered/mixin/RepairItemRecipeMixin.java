package com.auco.tempered.mixin;

import com.auco.tempered.service.EquipmentUpgradeService;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Guard the shared input validation so both matching and direct assembly reject history loss. */
@Mixin(RepairItemRecipe.class)
abstract class RepairItemRecipeMixin {
    @ModifyReturnValue(method = "getItemsToCombine", at = @At("RETURN"))
    private Pair<ItemStack, ItemStack> tempered$protectHistory(Pair<ItemStack, ItemStack> inputs) {
        if (inputs != null && (EquipmentUpgradeService.hasHistory(inputs.getFirst())
                || EquipmentUpgradeService.hasHistory(inputs.getSecond()))) return null;
        return inputs;
    }
}
