package com.auco.tempered.config;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Shared declarations and whole-list validation; invalid entries never shift tier positions. */
public final class ConfigValues {
    public static final int MAX_TIERS = 100;
    public static final double MAX_BONUS_PERCENT = 10000.0;

    public static ModConfigSpec.Builder key(ModConfigSpec.Builder builder, String path, String comment) {
        return builder.comment(comment).translation("tempered.config." + path).worldRestart();
    }

    public static boolean numbers(Object value, double min, double max, boolean integers, boolean increasing) {
        if (!(value instanceof List<?> list) || list.isEmpty() || list.size() > MAX_TIERS) {
            return false;
        }
        double previous = Double.NEGATIVE_INFINITY;
        for (Object element : list) {
            if (!(element instanceof Number number)) return false;
            double current = number.doubleValue();
            if (!Double.isFinite(current) || current < min || current > max
                    || (integers && current != Math.rint(current))
                    || (increasing && current <= previous)) return false;
            previous = current;
        }
        return true;
    }

    public static List<Double> doubles(List<? extends Number> values) {
        return values.stream().map(Number::doubleValue).toList();
    }

    public static List<Integer> integers(List<? extends Number> values) {
        return values.stream().map(Number::intValue).toList();
    }

    private ConfigValues() {}
}
