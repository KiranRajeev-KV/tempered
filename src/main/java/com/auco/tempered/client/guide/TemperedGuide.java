package com.auco.tempered.client.guide;

import com.auco.tempered.Tempered;
import guideme.Guide;
import guideme.compiler.TagCompiler;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

/** Client entrypoint: register before the initial resource reload starts preparing pages. */
@Mod(value = Tempered.MODID, dist = Dist.CLIENT)
public final class TemperedGuide {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Tempered.MODID, "guide");

    public TemperedGuide() {
        Guide.builder(ID)
                .extension(TagCompiler.EXTENSION_POINT, new SettingsTagCompiler())
                .build();
    }
}
