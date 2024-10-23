package com.supermartijn642.entangled;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created 3/16/2020 by SuperMartijn642
 */
public class EntangledBlockEntityRenderer implements CustomBlockEntityRenderer<EntangledBlockEntity> {

    public static final TagKey<Block> BLACKLISTED_BLOCKS = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("entangled", "render_blacklist"));
    public static final TagKey<BlockEntityType<?>> BLACKLISTED_ENTITIES = TagKey.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("entangled", "render_blacklist"));
    private static final Set<BlockEntityType<?>> ERRORED_BLOCK_ENTITIES = Collections.synchronizedSet(new HashSet<>());
    private static final Set<BlockState> ERRORED_BLOCK_STATES = Collections.synchronizedSet(new HashSet<>());

    private static int depth = 0;

    @Override
    public void render(EntangledBlockEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay){
        if(!entity.isBound())
            return;

        BlockEntity boundTile = entity.getLevel().dimension() == entity.getBoundDimensionIdentifier() ? entity.getLevel().getBlockEntity(entity.getBoundBlockPos()) : null;
        BlockState boundState = entity.getBoundBlockState();

        boolean renderTile = boundTile != null
            && !BuiltInRegistries.BLOCK_ENTITY_TYPE.get(BLACKLISTED_ENTITIES).map(tag -> tag.contains(BuiltInRegistries.BLOCK_ENTITY_TYPE.get(ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE, BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(boundTile.getType()))).orElseThrow())).orElse(false) // Dude who the fuck makes this garbage system, I just want to check if something's in a tag
            && !ERRORED_BLOCK_ENTITIES.contains(boundTile.getType());
        boolean renderBlock = boundState != null && boundState.getRenderShape() == RenderShape.MODEL
            && !boundState.is(BLACKLISTED_BLOCKS)
            && !ERRORED_BLOCK_STATES.contains(boundState);

        // get the bounding box
        AABB bounds = new AABB(0, 0, 0, 1, 1, 1);
        if(renderBlock && entity.getLevel().dimension() == entity.getBoundDimensionIdentifier()){
            VoxelShape shape = boundState.getOcclusionShape();
            if(!shape.isEmpty())
                bounds = shape.bounds();
        }

        poseStack.pushPose();

        // rotate and scale
        poseStack.translate(0.5, 0.5, 0.5);
        if(EntangledConfig.rotateRenderedBlock.get()){
            float angleX = System.currentTimeMillis() % 10000 / 10000f * 2 * (float)Math.PI;
            float angleY = System.currentTimeMillis() % 11000 / 11000f * 2 * (float)Math.PI;
            float angleZ = System.currentTimeMillis() % 12000 / 12000f * 2 * (float)Math.PI;
            poseStack.mulPose(new Quaternionf().setAngleAxis(angleX, 1, 0, 0));
            poseStack.mulPose(new Quaternionf().setAngleAxis(angleY, 0, 1, 0));
            poseStack.mulPose(new Quaternionf().setAngleAxis(angleZ, 0, 0, 1));
        }
        float scale = 0.4763f / (float)Math.sqrt((bounds.getXsize() * bounds.getXsize() + bounds.getYsize() * bounds.getYsize() + bounds.getZsize() * bounds.getZsize()) / 4);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-bounds.getCenter().x, -bounds.getCenter().y, -bounds.getCenter().z);

        if(renderTile){
            if(!(boundTile instanceof EntangledBlockEntity) || depth < 10){
                depth++;
                try{
                    ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().render(boundTile, partialTicks, poseStack, bufferSource);
                }catch(Exception e){
                    ERRORED_BLOCK_ENTITIES.add(boundTile.getType());
                    Entangled.LOGGER.error("Encountered an exception whilst rendering block entity '" + Registries.BLOCK_ENTITY_TYPES.getIdentifier(boundTile.getType()) + "'! Please report to Entangled!", e);
                }
                depth--;
            }
        }
        if(renderBlock){
            try{
                ClientUtils.getBlockRenderer().renderSingleBlock(boundState, poseStack, bufferSource, combinedLight, combinedOverlay);
            }catch(Exception e){
                ERRORED_BLOCK_STATES.add(boundState);
                Entangled.LOGGER.error("Encountered an exception whilst rendering block '" + boundState + "'! Please report to Entangled!", e);
            }
        }

        poseStack.popPose();
    }
}
