package com.auco.tempered.client;

import com.auco.tempered.Tempered;
import com.auco.tempered.equipment.affix.executioner.ExecutionerData;
import com.auco.tempered.equipment.attribute.reinforced.ReinforcedData;
import com.auco.tempered.equipment.attribute.swift.SwiftData;
import com.auco.tempered.service.ExecutionerService;
import com.auco.tempered.service.ReinforcementService;
import com.auco.tempered.service.SwiftService;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * Development-only HUD that shows live Attribute and Affix state for the held
 * equipment, including contextual block-breaking and combat diagnostics.
 *
 * <p>The data components are synchronized with the ItemStack, so everything
 * renders purely from client state without network packets.</p>
 *
 * <p>The layer never renders in production builds ({@link FMLLoader#isProduction()})
 * and only appears while holding equipment with tracked Tempered data.</p>
 */
@EventBusSubscriber(
        modid = Tempered.MODID,
        value = Dist.CLIENT
)
public final class AspectDebugOverlay {

    /** Vertical space one text row occupies, matching the vanilla font height plus padding. */
    private static final int LINE_HEIGHT = 10;

    /** Screen-edge padding so text does not hug the window border. */
    private static final int SCREEN_PADDING = 4;

    /** Vertical padding above the hotbar row along the bottom of the screen. */
    private static final int BOTTOM_MARGIN = 26;

    /** ARGB color for value rows. A constant avoids unboxing {@link ChatFormatting#getColor()}. */
    private static final int VALUE_TEXT_COLOR = 0xFFFFFFFF;

    private static final ResourceLocation OVERLAY_ID =
            ResourceLocation.fromNamespaceAndPath(Tempered.MODID, "aspect_debug");

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(OVERLAY_ID, AspectDebugOverlay::render);
    }

    private static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        // Debug aid for development only, so players never see engineering HUD text.
        if (FMLLoader.isProduction()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || player.isSpectator()) {
            return;
        }

        ItemStack heldStack = player.getMainHandItem();
        SwiftData swiftData = SwiftService.getActiveData(heldStack);
        ReinforcedData reinforcedData = ReinforcementService.getActiveData(heldStack);
        ExecutionerData executionerData = ExecutionerService.getProgressData(heldStack);
        if (swiftData == null && reinforcedData == null && executionerData == null) {
            return;
        }

        Font font = minecraft.font;
        int screenHeight = guiGraphics.guiHeight();
        List<Component> lines = new ArrayList<>();
        if (reinforcedData != null) {
            lines.add(Component.translatable(
                    "tooltip.tempered.reinforced.title",
                    toRomanNumeral(reinforcedData.level())
            ).withStyle(ChatFormatting.AQUA));
            lines.add(Component.translatable(
                    "hud.tempered.reinforced.max_damage",
                    reinforcedData.baseMaxDamage(),
                    reinforcedData.reinforcedMaxDamage()
            ));
        }
        if (swiftData != null) {
            lines.add(Component.translatable(
                    "tooltip.tempered.swift.title",
                    toRomanNumeral(swiftData.level())
            ).withStyle(ChatFormatting.GOLD));
            Component speedLine = resolveBreakSpeedLine(player, minecraft, swiftData);
            if (speedLine != null) {
                lines.add(speedLine);
            }
        }
        if (executionerData != null) {
            addExecutionerLines(lines, minecraft, executionerData);
        }

        // Bottom-up stacking: the last line sits just above the hotbar row.
        int y = screenHeight - BOTTOM_MARGIN - (lines.size() - 1) * LINE_HEIGHT;
        for (Component line : lines) {
            guiGraphics.drawString(font, line, SCREEN_PADDING, y, VALUE_TEXT_COLOR);
            y += LINE_HEIGHT;
        }
    }

    private static void addExecutionerLines(
            List<Component> lines,
            Minecraft minecraft,
            ExecutionerData data
    ) {
        if (data.isUnlocked()) {
            lines.add(Component.translatable(
                    "tooltip.tempered.executioner.title",
                    toRomanNumeral(data.level())
            ).withStyle(ChatFormatting.DARK_RED));
        } else {
            lines.add(Component.translatable("hud.tempered.executioner.awakening")
                    .withStyle(ChatFormatting.DARK_RED));
        }

        if (data.level() >= ExecutionerData.MAX_LEVEL) {
            lines.add(Component.translatable(
                    "hud.tempered.executioner.kills_max",
                    data.hostileKills()
            ));
        } else {
            lines.add(Component.translatable(
                    "hud.tempered.executioner.kills",
                    data.hostileKills(),
                    data.nextMilestoneKills()
            ));
        }

        if (data.isUnlocked()) {
            lines.add(Component.translatable(
                    "hud.tempered.executioner.threshold",
                    data.executeHealthPercent()
            ).withStyle(ChatFormatting.RED));
        } else {
            lines.add(Component.translatable("hud.tempered.executioner.threshold_locked")
                    .withStyle(ChatFormatting.GRAY));
        }

        LivingEntity target = resolveLivingTarget(minecraft);
        if (target == null) {
            return;
        }

        lines.add(Component.translatable(
                "hud.tempered.executioner.target",
                target.getDisplayName()
        ).withStyle(ChatFormatting.GRAY));

        if (!ExecutionerService.isHostileTarget(target)) {
            lines.add(Component.translatable("hud.tempered.executioner.target_ineligible")
                    .withStyle(ChatFormatting.YELLOW));
            return;
        }
        if (ExecutionerService.isImmuneTarget(target)) {
            lines.add(Component.translatable("hud.tempered.executioner.target_immune")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            return;
        }

        float currentHealth = target.getHealth();
        float maxHealth = target.getMaxHealth();
        float healthPercent = maxHealth > 0.0F
                ? currentHealth * 100.0F / maxHealth
                : 0.0F;
        lines.add(Component.translatable(
                "hud.tempered.executioner.target_health",
                formatNumber(currentHealth),
                formatNumber(maxHealth),
                formatNumber(healthPercent)
        ));

        if (!data.isUnlocked()) {
            lines.add(Component.translatable("hud.tempered.executioner.target_locked")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        float thresholdHealth = maxHealth * data.executeHealthPercent() / 100.0F;
        lines.add(Component.translatable(
                "hud.tempered.executioner.target_threshold",
                formatNumber(thresholdHealth),
                data.executeHealthPercent()
        ));

        boolean executable = currentHealth > 0.0F && currentHealth <= thresholdHealth;
        lines.add(Component.translatable(
                executable
                        ? "hud.tempered.executioner.executable_yes"
                        : "hud.tempered.executioner.executable_no"
        ).withStyle(executable ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    /** Resolves multipart bosses such as the Ender Dragon to their parent mob. */
    private static LivingEntity resolveLivingTarget(Minecraft minecraft) {
        if (!(minecraft.hitResult instanceof EntityHitResult entityHit)) {
            return null;
        }

        Entity target = entityHit.getEntity();
        if (target instanceof LivingEntity livingTarget) {
            return livingTarget;
        }
        if (target instanceof net.neoforged.neoforge.entity.PartEntity<?> part
                && part.getParent() instanceof LivingEntity parent) {
            return parent;
        }
        return null;
    }

    /**
     * Builds the "Break speed" readout for the currently targeted block, or
     * {@code null} when no mineable block is being looked at. The base value
     * excludes the Swift multiplier so the arrow shows the exact delta the
     * Aspect contributes. Player status effects such as Haste are not part of
     * this readout because this HUD tracks Aspect deltas specifically.
     */
    private static Component resolveBreakSpeedLine(
            LocalPlayer player,
            Minecraft minecraft,
            SwiftData swiftData
    ) {
        if (!(minecraft.hitResult instanceof BlockHitResult blockHit)
                || blockHit.getType() != HitResult.Type.BLOCK) {
            return null;
        }

        var blockState = player.level().getBlockState(blockHit.getBlockPos());
        float baseSpeed = player.getMainHandItem().getDestroySpeed(blockState);
        if (baseSpeed <= 0.0F || blockState.isAir()) {
            return null;
        }

        float effectiveSpeed = baseSpeed;
        if (swiftData != null) {
            effectiveSpeed *= swiftData.miningSpeedMultiplier();
        }

        return Component.translatable(
                "hud.tempered.aspect.break_speed",
                formatNumber(baseSpeed),
                formatNumber(effectiveSpeed)
        );
    }

    /** Formats a debug value with two decimals, hiding trailing ".00". */
    private static String formatNumber(float value) {
        if (Math.abs(value - Math.round(value)) < 0.005F) {
            return String.valueOf(Math.round(value));
        }
        return String.format("%.2f", value);
    }

    /**
     * Minecraft represents tiered effects with Roman numerals (for example,
     * enchantments and potion effects), so using them keeps this HUD familiar
     * to players.
     */
    private static String toRomanNumeral(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(level);
        };
    }

    private AspectDebugOverlay() {
    }
}
