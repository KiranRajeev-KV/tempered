package com.auco.tempered.registry;

import com.auco.tempered.Tempered;
import com.auco.tempered.recipe.aspect.AspectSmithingRecipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers the codecs that load Tempered's data-driven recipe files. */
public final class ModRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Tempered.MODID);

    /**
     * One recipe implementation handles every Aspect. Its runtime checks can
     * preserve and update the unique data components on the target stack.
     */
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AspectSmithingRecipe>>
            ASPECT_SMITHING = RECIPE_SERIALIZERS.register(
                    "aspect_smithing",
                    AspectSmithingRecipe.Serializer::new
            );

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }

    private ModRecipeSerializers() {
    }
}
