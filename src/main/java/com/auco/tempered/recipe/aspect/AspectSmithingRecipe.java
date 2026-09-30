package com.auco.tempered.recipe.aspect;

import com.auco.tempered.registry.ModItems;
import com.auco.tempered.registry.ModRecipeSerializers;
import com.auco.tempered.service.ReinforcementService;
import com.auco.tempered.service.SwiftService;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

/**
 * Applies an Aspect in the vanilla smithing table.
 *
 * <p>The target goes in the base slot and an Aspect goes in the addition
 * slot. The template slot stays empty. A custom recipe is necessary because
 * vanilla transform recipes have a fixed result and cannot increment a custom
 * component while retaining the target stack's other components.</p>
 */
public final class AspectSmithingRecipe implements SmithingRecipe {

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        if (!input.template().isEmpty()) {
            return false;
        }

        return switch (aspectKind(input.addition())) {
            case REINFORCED -> ReinforcementService.inspect(input.base()).isSuccess();
            case SWIFT -> SwiftService.inspect(input.base()).isSuccess();
            case NONE -> false;
        };
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
        // Smithing consumes the original base stack after the player takes the
        // result, so work on a copy to keep the inventory transaction safe.
        if (!input.template().isEmpty()) return ItemStack.EMPTY;
        ItemStack result = input.base().copyWithCount(1);

        switch (aspectKind(input.addition())) {
            case REINFORCED -> {
                if (!ReinforcementService.apply(result).isSuccess()) return ItemStack.EMPTY;
            }
            case SWIFT -> {
                if (!SwiftService.apply(result).isSuccess()) return ItemStack.EMPTY;
            }
            case NONE -> {
                return ItemStack.EMPTY;
            }
        }

        return result;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        // The output depends on the input stack, so recipe-book previews have
        // no representative fixed item to show.
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return stack.isDamageableItem() || stack.is(com.auco.tempered.tag.ModItemTags.SWIFT_APPLICABLE);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return aspectKind(stack) != AspectKind.NONE;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.ASPECT_SMITHING.get();
    }

    /** Distinguishes which existing service owns the upgrade rules. */
    private static AspectKind aspectKind(ItemStack stack) {
        if (stack.is(ModItems.REINFORCED_ASPECT.get())) {
            return AspectKind.REINFORCED;
        }
        if (stack.is(ModItems.SWIFT_ASPECT.get())) {
            return AspectKind.SWIFT;
        }
        return AspectKind.NONE;
    }

    private enum AspectKind {
        REINFORCED,
        SWIFT,
        NONE
    }

    /** Serializes the field-free recipe to JSON and to connected clients. */
    public static final class Serializer implements RecipeSerializer<AspectSmithingRecipe> {

        private static final AspectSmithingRecipe RECIPE = new AspectSmithingRecipe();
        private static final MapCodec<AspectSmithingRecipe> CODEC = MapCodec.unit(RECIPE);
        private static final StreamCodec<RegistryFriendlyByteBuf, AspectSmithingRecipe> STREAM_CODEC =
                StreamCodec.unit(RECIPE);

        @Override
        public MapCodec<AspectSmithingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AspectSmithingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
