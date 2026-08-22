package com.auco.tempered.service;

import com.auco.tempered.equipment.attribute.swift.SwiftData;
import com.auco.tempered.registry.ModDataComponents;
import com.auco.tempered.tag.ModItemTags;

import net.minecraft.world.item.ItemStack;

/** Contains the server-authoritative rules for applying and reading Swift. */
public final class SwiftService {

    /**
     * Checks the current target without changing it. Calling this on both
     * client and server lets the interaction pipeline agree on the outcome.
     */
    public static SwiftResult inspect(ItemStack targetStack) {
        if (!targetStack.is(ModItemTags.SWIFT_APPLICABLE)) {
            return SwiftResult.failure(SwiftResult.Status.NOT_SWIFT_APPLICABLE);
        }

        SwiftData currentData = targetStack.get(ModDataComponents.SWIFT_DATA.get());
        if (currentData == null) {
            return SwiftResult.success(0, 1);
        }

        if (!currentData.isValid()) {
            return SwiftResult.failure(SwiftResult.Status.INVALID_DATA);
        }

        if (currentData.level() >= SwiftData.MAX_LEVEL) {
            return SwiftResult.failure(SwiftResult.Status.MAX_LEVEL);
        }

        return SwiftResult.success(currentData.level(), currentData.level() + 1);
    }

    /**
     * Applies the next level after checking the same rules as {@link #inspect}.
     * Only the logical server should call this method because it mutates a
     * player's ItemStack.
     */
    public static SwiftResult apply(ItemStack targetStack) {
        SwiftResult result = inspect(targetStack);
        if (!result.isSuccess()) {
            return result;
        }

        targetStack.set(
                ModDataComponents.SWIFT_DATA.get(),
                new SwiftData(result.newLevel())
        );

        return result;
    }

    /**
     * Returns valid Swift data only while the stack remains eligible according
     * to the datapack-controlled tag. This makes tag reloads take effect
     * immediately instead of granting a stale bonus.
     */
    public static SwiftData getActiveData(ItemStack stack) {
        if (!stack.is(ModItemTags.SWIFT_APPLICABLE)) {
            return null;
        }

        SwiftData data = stack.get(ModDataComponents.SWIFT_DATA.get());
        return data != null && data.isValid() ? data : null;
    }

    private SwiftService() {
    }
}
