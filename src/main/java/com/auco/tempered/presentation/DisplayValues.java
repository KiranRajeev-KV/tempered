package com.auco.tempered.presentation;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import com.auco.tempered.config.affix.ExecutionerSettings;
import net.minecraft.network.chat.Component;

/** Compact numbers without floating-point noise or rounding tiny chances to zero. */
public final class DisplayValues {
    private static final MathContext PRECISION = new MathContext(4, RoundingMode.HALF_UP);

    public static String number(double value) {
        return decimal(BigDecimal.valueOf(value));
    }

    /** The returned value is in percent; the translated sentence supplies the % sign. */
    public static String chance(double probability) {
        BigDecimal percent = BigDecimal.valueOf(probability).movePointRight(2);
        // Near-certain probabilities must not be presented as a guaranteed 100%.
        if (probability < 1 && percent.round(PRECISION).compareTo(BigDecimal.valueOf(100)) == 0) {
            return percent.stripTrailingZeros().toPlainString();
        }
        return decimal(percent);
    }

    private static String decimal(BigDecimal value) {
        BigDecimal rounded = value.round(PRECISION).stripTrailingZeros();
        return rounded.scale() > 6 ? rounded.toString() : rounded.toPlainString();
    }

    public static String level(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> Integer.toString(level);
        };
    }

    public static Component progressUnit(ExecutionerSettings settings) {
        return Component.translatable(settings.progressChance() == 1 && settings.progressPerSuccess() == 1
                ? "guide.tempered.unit.kills" : "guide.tempered.unit.points");
    }

    public static boolean progressionAvailable(ExecutionerSettings settings) {
        return settings.enabled() && settings.progressionEnabled() && settings.progressChance() > 0;
    }

    private DisplayValues() {}
}
