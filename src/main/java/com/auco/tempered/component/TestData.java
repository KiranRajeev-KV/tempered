package com.auco.tempered.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TestData(int level, boolean empowered) {

    public static final TestData DEFAULT = new TestData(0, false);

    public TestData incrementLevel() {
        return new TestData(level + 1, empowered);
    }

    public TestData toggleEmpowered() {
        return new TestData(level, !empowered);
    }

    public static final Codec<TestData> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.INT
                                    .fieldOf("level")
                                    .forGetter(TestData::level),
                            Codec.BOOL
                                    .fieldOf("empowered")
                                    .forGetter(TestData::empowered)
                    ).apply(instance, TestData::new)
            );
}