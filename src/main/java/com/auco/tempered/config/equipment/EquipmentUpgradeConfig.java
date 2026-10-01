package com.auco.tempered.config.equipment;

import com.auco.tempered.config.ConfigValues;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class EquipmentUpgradeConfig {
    private final ModConfigSpec.ConfigValue<String> damagePolicy;

    public EquipmentUpgradeConfig(ModConfigSpec.Builder builder) {
        builder.translation("tempered.config.equipment.upgrades").push("equipment.upgrades");
        damagePolicy = ConfigValues.key(builder, "equipment.upgrades.damage_policy",
                "Wear carried through material-changing smithing for equipment with Tempered history.\n"
                        + "Allowed: remaining_fraction (default) keeps the percentage of durability remaining; damage_points keeps the same damage points.\n"
                        + "Both clamp safely so the upgrade never breaks the result.\n"
                        + "Does not change Aspect application or balance reconciliation. Requires world/server restart.")
                .define("damage_policy", EquipmentUpgradeSettings.DEFAULT.damagePolicy().id(),
                        EquipmentUpgradeSettings.DamagePolicy::isValid);
        builder.pop(2);
    }

    public EquipmentUpgradeSettings read() {
        return new EquipmentUpgradeSettings(EquipmentUpgradeSettings.DamagePolicy.fromId(damagePolicy.get()));
    }
}
