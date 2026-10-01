package com.auco.tempered.mixin;

import com.auco.tempered.config.TemperedConfig;
import com.auco.tempered.service.EquipmentUpgradeService;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SmithingTransformRecipe.class)
abstract class SmithingTransformRecipeMixin {
    @Shadow @Final ItemStack result;

    @ModifyReturnValue(method = "assemble(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"))
    private ItemStack tempered$preserveProgress(ItemStack output, SmithingRecipeInput input, HolderLookup.Provider registries) {
        return EquipmentUpgradeService.transform(input.base(), result, output, TemperedConfig.active());
    }
}
