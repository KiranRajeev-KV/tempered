package com.auco.tempered.item.aspect;

import com.auco.tempered.service.ReinforcementResult;
import com.auco.tempered.service.ReinforcementService;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Consumable aspect that upgrades the damageable item held in the main hand.
 *
 * <p>Minecraft calls {@link #use(Level, Player, InteractionHand)} once for
 * each hand during a right-click interaction. This item acts only from the
 * offhand, leaving the main hand available for the target equipment.</p>
 */
public final class ReinforcedAspectItem extends Item {

    public ReinforcedAspectItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack aspectStack = player.getItemInHand(hand);

        // PASS tells Minecraft this hand did not handle the interaction. It is
        // important here: returning success in the main hand would stop the
        // interaction pipeline before the offhand could be considered.
        if (hand != InteractionHand.OFF_HAND) {
            return InteractionResultHolder.pass(aspectStack);
        }

        ItemStack targetStack = player.getMainHandItem();
        // Inspect performs no mutation, so it is safe on client and server.
        // The client needs this result to choose the same interaction outcome.
        ReinforcementResult preview = ReinforcementService.inspect(targetStack);
        if (!preview.isSuccess()) {
            if (!level.isClientSide) {
                player.displayClientMessage(failureMessage(preview), true);
            }
            return InteractionResultHolder.pass(aspectStack);
        }

        if (!level.isClientSide) {
            // The server owns inventory and component mutations. Doing this on
            // both sides would cause duplicate consumption or desyncs.
            ReinforcementResult applied = ReinforcementService.apply(targetStack);
            if (!applied.isSuccess()) {
                player.displayClientMessage(failureMessage(applied), true);
                return InteractionResultHolder.pass(aspectStack);
            }

            if (!player.getAbilities().instabuild) {
                // Creative mode normally does not consume ordinary items.
                aspectStack.shrink(1);
            }

            player.playSound(SoundEvents.ANVIL_USE, 0.7F, 1.15F);
            player.displayClientMessage(successMessage(applied), true);
        }

        return InteractionResultHolder.sidedSuccess(aspectStack, level.isClientSide);
    }

    private static Component successMessage(ReinforcementResult result) {
        return Component.translatable(
                "message.tempered.reinforced.success",
                romanNumeral(result.newLevel()),
                result.previousMaxDamage(),
                result.newMaxDamage()
        ).withStyle(ChatFormatting.AQUA);
    }

    private static Component failureMessage(ReinforcementResult result) {
        return switch (result.status()) {
            case NOT_DAMAGEABLE -> Component.translatable(
                    "message.tempered.reinforced.not_damageable"
            ).withStyle(ChatFormatting.RED);
            case MAX_LEVEL -> Component.translatable(
                    "message.tempered.reinforced.max_level"
            ).withStyle(ChatFormatting.RED);
            case INVALID_DATA -> Component.translatable(
                    "message.tempered.reinforced.invalid_data"
            ).withStyle(ChatFormatting.RED);
            case SUCCESS -> throw new IllegalArgumentException(
                    "A successful reinforcement has no failure message"
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
