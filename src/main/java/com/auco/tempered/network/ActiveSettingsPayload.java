package com.auco.tempered.network;

import java.util.ArrayList;
import java.util.List;
import com.auco.tempered.Tempered;
import com.auco.tempered.config.ConfigValues;
import com.auco.tempered.config.GameplaySettings;
import com.auco.tempered.config.aspect.ReinforcedSettings;
import com.auco.tempered.config.aspect.SwiftSettings;
import com.auco.tempered.config.affix.ExecutionerSettings;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Active values override pending on-disk changes sent by NeoForge during login. */
public record ActiveSettingsPayload(GameplaySettings settings) implements CustomPacketPayload {
    public static final Type<ActiveSettingsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Tempered.MODID, "active_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ActiveSettingsPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> write(buffer, payload.settings()),
            buffer -> new ActiveSettingsPayload(read(buffer)));

    @Override
    public Type<ActiveSettingsPayload> type() { return TYPE; }

    private static void write(RegistryFriendlyByteBuf buffer, GameplaySettings settings) {
        buffer.writeBoolean(settings.reinforced().enabled());
        writeDoubles(buffer, settings.reinforced().bonuses());
        buffer.writeBoolean(settings.swift().enabled());
        writeDoubles(buffer, settings.swift().bonuses());
        var executioner = settings.executioner();
        buffer.writeBoolean(executioner.enabled());
        buffer.writeBoolean(executioner.progressionEnabled());
        buffer.writeVarInt(executioner.killsRequired().size());
        executioner.killsRequired().forEach(buffer::writeVarInt);
        writeDoubles(buffer, executioner.healthPercents());
        buffer.writeDouble(executioner.progressChance());
        buffer.writeVarInt(executioner.progressPerSuccess());
        writeDoubles(buffer, executioner.executeChances());
        buffer.writeBoolean(executioner.allowCreativeProgression());
        buffer.writeBoolean(executioner.announceTierUp());
    }

    private static GameplaySettings read(RegistryFriendlyByteBuf buffer) {
        var reinforced = new ReinforcedSettings(buffer.readBoolean(), readDoubles(buffer));
        var swift = new SwiftSettings(buffer.readBoolean(), readDoubles(buffer));
        boolean enabled = buffer.readBoolean();
        boolean progression = buffer.readBoolean();
        int count = readSize(buffer);
        List<Integer> kills = new ArrayList<>(count);
        for (int i = 0; i < count; i++) kills.add(buffer.readVarInt());
        var executioner = new ExecutionerSettings(enabled, progression, kills, readDoubles(buffer),
                buffer.readDouble(), buffer.readVarInt(), readDoubles(buffer), buffer.readBoolean(), buffer.readBoolean());
        return new GameplaySettings(reinforced, swift, executioner);
    }

    private static void writeDoubles(RegistryFriendlyByteBuf buffer, List<Double> values) {
        buffer.writeVarInt(values.size());
        values.forEach(buffer::writeDouble);
    }

    private static List<Double> readDoubles(RegistryFriendlyByteBuf buffer) {
        int count = readSize(buffer);
        List<Double> values = new ArrayList<>(count);
        for (int i = 0; i < count; i++) values.add(buffer.readDouble());
        return values;
    }

    private static int readSize(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 1 || count > ConfigValues.MAX_TIERS) throw new DecoderException("Invalid Tempered tier count: " + count);
        return count;
    }
}
