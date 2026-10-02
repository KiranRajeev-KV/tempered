package com.auco.tempered.client;

import com.auco.tempered.Tempered;
import com.auco.tempered.config.TemperedClientConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Optional local chest appearance, with a live switch back to vanilla rendering. */
@EventBusSubscriber(modid = Tempered.MODID, value = Dist.CLIENT)
public final class TreasureChestPreviewRenderer extends ChestRenderer<ChestBlockEntity> {
    private static final ResourceLocation GEOMETRY = ResourceLocation.fromNamespaceAndPath(
            Tempered.MODID, "models/preview/treasure_chest.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Tempered.MODID, "textures/entity/treasure_chest.png");
    private final List<Face> base;
    private final List<Face> lid;

    public TreasureChestPreviewRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        try (Reader reader = Minecraft.getInstance().getResourceManager().openAsReader(GEOMETRY)) {
            JsonObject model = JsonParser.parseReader(reader).getAsJsonObject();
            base = readFaces(model.getAsJsonArray("base"));
            lid = readFaces(model.getAsJsonArray("lid"));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load treasure chest preview geometry", e);
        }
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityType.CHEST, TreasureChestPreviewRenderer::new);
    }

    @Override
    public void render(ChestBlockEntity chest, float partialTick, PoseStack poses,
            MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (!TemperedClientConfig.customChests()) {
            super.render(chest, partialTick, poses, buffers, packedLight, packedOverlay);
            return;
        }
        // The inventory renderer also dispatches through this renderer, without a level.
        BlockState state = chest.hasLevel() ? chest.getBlockState()
                : Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        if (!(state.getBlock() instanceof ChestBlock chestBlock) || !state.is(Blocks.CHEST)) {
            super.render(chest, partialTick, poses, buffers, packedLight, packedOverlay);
            return;
        }
        ChestType type = state.getValue(ChestBlock.TYPE);
        // Draw one continuous wide chest, rather than two models with duplicate locks.
        if (type == ChestType.LEFT) {
            return;
        }
        DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combined = chest.hasLevel()
                ? chestBlock.combine(state, chest.getLevel(), chest.getBlockPos(), true)
                : DoubleBlockCombiner.Combiner::acceptNone;
        float openness = combined.apply(ChestBlock.opennessCombiner(chest)).get(partialTick);
        float eased = 1.0F - (float) Math.pow(1.0F - openness, 3);
        int light = combined.apply(new BrightnessCombiner<>()).applyAsInt(packedLight);
        Direction neighbor = type == ChestType.SINGLE ? null : ChestBlock.getConnectedDirection(state);
        poses.pushPose();
        poses.translate(0.5 + (neighbor == null ? 0 : neighbor.getStepX() * 0.5), 0,
                0.5 + (neighbor == null ? 0 : neighbor.getStepZ() * 0.5));
        // The Blockbench front is -Z; vanilla chests' canonical front is +Z.
        poses.mulPose(Axis.YP.rotationDegrees(180 - state.getValue(ChestBlock.FACING).toYRot()));
        if (neighbor != null) {
            poses.scale(30.0F / 14.0F, 1, 1);
        }
        poses.translate(-0.5, 0, -0.5);
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        draw(base, poses.last(), vertices, light, packedOverlay);
        poses.pushPose();
        poses.translate(8.0 / 16, 10.0 / 16, 15.0 / 16);
        poses.mulPose(Axis.XP.rotationDegrees(eased * 90));
        poses.translate(-8.0 / 16, -10.0 / 16, -15.0 / 16);
        draw(lid, poses.last(), vertices, light, packedOverlay);
        poses.popPose();
        poses.popPose();
    }

    private static List<Face> readFaces(JsonArray faces) {
        List<Face> result = new ArrayList<>();
        for (var entry : faces) {
            JsonObject face = entry.getAsJsonObject();
            float[] vertices = new float[20];
            JsonArray data = face.getAsJsonArray("vertices");
            for (int i = 0; i < vertices.length; i++) {
                vertices[i] = data.get(i).getAsFloat();
            }
            JsonArray normal = face.getAsJsonArray("normal");
            result.add(new Face(vertices, normal.get(0).getAsFloat(),
                    normal.get(1).getAsFloat(), normal.get(2).getAsFloat()));
        }
        return List.copyOf(result);
    }

    private static void draw(List<Face> faces, PoseStack.Pose pose, VertexConsumer consumer,
            int light, int overlay) {
        for (Face face : faces) {
            for (int i = 0; i < 20; i += 5) {
                float[] v = face.vertices;
                consumer.addVertex(pose, v[i], v[i + 1], v[i + 2])
                        .setColor(255, 255, 255, 255).setUv(v[i + 3], v[i + 4])
                        .setOverlay(overlay).setLight(light)
                        .setNormal(pose, face.nx, face.ny, face.nz);
            }
        }
    }

    private record Face(float[] vertices, float nx, float ny, float nz) {}
}
