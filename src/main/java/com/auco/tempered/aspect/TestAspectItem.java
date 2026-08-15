package com.auco.tempered.aspect;

import com.auco.tempered.component.TestData;
import com.auco.tempered.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
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
            TestData currentTestData = stack.getOrDefault(
                    ModDataComponents.TEST_DATA.get(),
                    new TestData(0, false)
            );

            TestData updatedTestData = currentTestData.incrementLevel();

            stack.set(
                    ModDataComponents.TEST_DATA.get(),
                    updatedTestData
            );

            player.displayClientMessage(
                    Component.literal("Level: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(
                                    Component.literal(String.valueOf(updatedTestData.level()))
                                            .withStyle(ChatFormatting.AQUA)
                            ),
                    true
            );
        }

        return InteractionResultHolder.sidedSuccess(
                stack,
                level.isClientSide
        );
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        TestData currentTestData = stack.getOrDefault(
                ModDataComponents.TEST_DATA.get(),
                new TestData(0, false)
        );

        return currentTestData.empowered() || super.isFoil(stack);
    }

    @Override
    public boolean onLeftClickEntity(
            ItemStack stack,
            Player player,
            Entity entity
    ) {
        if (!player.level().isClientSide) {
            TestData currentTestData = stack.getOrDefault(
                    ModDataComponents.TEST_DATA.get(),
                    new TestData(0, false)
            );

            TestData updatedTestData = currentTestData.toggleEmpowered();

            stack.set(
                    ModDataComponents.TEST_DATA.get(),
                    updatedTestData
            );

            player.displayClientMessage(
                    Component.literal("Empowered: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(
                                    Component.literal(
                                            updatedTestData.empowered() ? "Yes" : "No"
                                    ).withStyle(
                                            updatedTestData.empowered()
                                                    ? ChatFormatting.GREEN
                                                    : ChatFormatting.RED
                                    )
                            ),
                    true
            );
        }

        return false;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        TestData currentTestData = stack.getOrDefault(
                ModDataComponents.TEST_DATA.get(),
                new TestData(0, false)
        );

        tooltipComponents.add(
                Component.literal("Level: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(
                                Component.literal(String.valueOf(currentTestData.level()))
                                        .withStyle(ChatFormatting.AQUA)
                        )
        );

        tooltipComponents.add(
                Component.literal("Empowered: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(
                                Component.literal(
                                        currentTestData.empowered() ? "Yes" : "No"
                                ).withStyle(
                                        currentTestData.empowered()
                                                ? ChatFormatting.GREEN
                                                : ChatFormatting.RED
                                )
                        )
        );

        super.appendHoverText(
                stack,
                context,
                tooltipComponents,
                tooltipFlag
        );
    }
}