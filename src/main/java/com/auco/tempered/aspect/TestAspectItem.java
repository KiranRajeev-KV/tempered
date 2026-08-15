package com.auco.tempered.aspect;

import com.auco.tempered.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public final class TestAspectItem extends Item {

    public TestAspectItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            int currentLevel = stack.getOrDefault(
                    ModDataComponents.TEST_LEVEL.get(),
                    0
            );

            int newLevel = currentLevel + 1;

            stack.set(
                    ModDataComponents.TEST_LEVEL.get(),
                    newLevel
            );

            player.displayClientMessage(
                    Component.literal("Test Level: " + newLevel),
                    true
            );
        }

        return InteractionResultHolder.sidedSuccess(
                stack,
                level.isClientSide
        );
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        int level = stack.getOrDefault(
                ModDataComponents.TEST_LEVEL.get(),
                0
        );

        tooltipComponents.add(
                Component.literal("Test Level: " + level)
        );

        super.appendHoverText(
                stack,
                context,
                tooltipComponents,
                tooltipFlag
        );
    }
}