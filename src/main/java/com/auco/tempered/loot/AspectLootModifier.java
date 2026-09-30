package com.auco.tempered.loot;

import com.auco.tempered.config.TemperedConfig;
import com.auco.tempered.util.Probability;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/** Appends one reward stack without replacing or rerolling the original loot. */
public final class AspectLootModifier extends LootModifier {
    public static final MapCodec<AspectLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).and(AspectLootType.CODEC.fieldOf("aspect").forGetter(modifier -> modifier.aspect))
                    .apply(instance, AspectLootModifier::new));
    private final AspectLootType aspect;

    public AspectLootModifier(LootItemCondition[] conditions, AspectLootType aspect) {
        super(conditions);
        this.aspect = aspect;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        var active = TemperedConfig.active();
        var settings = aspect.settings(active);
        if (aspect.mechanicEnabled(active) && settings.enabled()
                && settings.lootTables().contains(context.getQueriedLootTableId())
                && Probability.succeeds(settings.chance(), context.getRandom()::nextDouble)) {
            int count = settings.minCount() == settings.maxCount() ? settings.minCount()
                    : settings.minCount() + context.getRandom().nextInt(settings.maxCount() - settings.minCount() + 1);
            generatedLoot.add(new ItemStack(aspect.item(), count));
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() { return CODEC; }
}
