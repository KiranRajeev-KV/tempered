package com.auco.tempered.item.aspect;

import com.auco.tempered.service.SwiftResult;
import com.auco.tempered.service.SwiftService;
import com.auco.tempered.equipment.attribute.swift.SwiftData;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Consumable aspect that increases the mining speed of a main-hand tool. */
public final class SwiftAspectItem extends Item {

    public SwiftAspectItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack aspectStack = player.getItemInHand(hand);

        // Passing from the main hand lets Minecraft continue to the offhand,
        // where the Aspect can act on the main-hand target.
        if (hand != InteractionHand.OFF_HAND) {
            return InteractionResultHolder.pass(aspectStack);
        }

        ItemStack targetStack = player.getMainHandItem();
        SwiftResult preview = SwiftService.inspect(targetStack);
        if (!preview.isSuccess()) {
            if (!level.isClientSide) {
                player.displayClientMessage(failureMessage(preview), true);
            }
            return InteractionResultHolder.pass(aspectStack);
        }

        if (!level.isClientSide) {
            SwiftResult applied = SwiftService.apply(targetStack);
            if (!applied.isSuccess()) {
                player.displayClientMessage(failureMessage(applied), true);
                return InteractionResultHolder.pass(aspectStack);
            }

            if (!player.getAbilities().instabuild) {
                aspectStack.shrink(1);
            }

            player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.7F, 1.2F);
            player.displayClientMessage(successMessage(applied), true);
        }

        return InteractionResultHolder.sidedSuccess(aspectStack, level.isClientSide);
    }

    private static Component successMessage(SwiftResult result) {
        return Component.translatable(
                "message.tempered.swift.success",
                romanNumeral(result.newLevel()),
                result.newLevel() * SwiftData.MINING_SPEED_BONUS_PER_LEVEL_PERCENT
        ).withStyle(ChatFormatting.GOLD);
    }

    private static Component failureMessage(SwiftResult result) {
        return switch (result.status()) {
            case NOT_SWIFT_APPLICABLE -> Component.translatable(
                    "message.tempered.swift.not_applicable"
            ).withStyle(ChatFormatting.RED);
            case MAX_LEVEL -> Component.translatable(
                    "message.tempered.swift.max_level"
            ).withStyle(ChatFormatting.RED);
            case INVALID_DATA -> Component.translatable(
                    "message.tempered.swift.invalid_data"
            ).withStyle(ChatFormatting.RED);
            case SUCCESS -> throw new IllegalArgumentException(
                    "A successful Swift application has no failure message"
            );
        };
    }

    private static String romanNumeral(int level) {
        return switch (level) {
            case 0 -> "0";
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> Integer.toString(level);
        };
    }
}
