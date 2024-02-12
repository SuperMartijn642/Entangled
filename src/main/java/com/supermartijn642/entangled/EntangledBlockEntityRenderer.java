package com.supermartijn642.entangled;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created 3/16/2020 by SuperMartijn642
 */
public class EntangledBlockEntityRenderer implements CustomBlockEntityRenderer<EntangledBlockEntity> {

    public static final Tag<Block> BLACKLISTED_BLOCKS = new BlockTags.Wrapper(new ResourceLocation("entangled", "render_blacklist"));
    private static final Set<TileEntityType<?>> ERRORED_BLOCK_ENTITIES = Collections.synchronizedSet(new HashSet<>());
    private static final Set<BlockState> ERRORED_BLOCK_STATES = Collections.synchronizedSet(new HashSet<>());

    private static int depth = 0;

    @Override
    public void render(EntangledBlockEntity entity, float partialTicks, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay){
        if(!entity.isBound())
            return;

        TileEntity boundTile = entity.getLevel().getDimension().getType() == entity.getBoundDimensionType() ? entity.getLevel().getBlockEntity(entity.getBoundBlockPos()) : null;
        BlockState boundState = entity.getBoundBlockState();

        boolean renderTile = boundTile != null
            && !ERRORED_BLOCK_ENTITIES.contains(boundTile.getType());
        boolean renderBlock = boundState != null && boundState.getRenderShape() == BlockRenderType.MODEL
            && !boundState.is(BLACKLISTED_BLOCKS)
            && !ERRORED_BLOCK_STATES.contains(boundState);

        // get the bounding box
        AxisAlignedBB bounds = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
        if(renderBlock && entity.getLevel().getDimension().getType() == entity.getBoundDimensionType()){
            VoxelShape shape = boundState.getOcclusionShape(entity.getLevel(), entity.getBoundBlockPos());
            if(!shape.isEmpty())
                bounds = shape.bounds();
        }

        poseStack.pushPose();

        // rotate and scale
        poseStack.translate(0.5, 0.5, 0.5);
        if(EntangledConfig.rotateRenderedBlock.get()){
            float angleX = System.currentTimeMillis() % 10000 / 10000f * 360f;
            float angleY = System.currentTimeMillis() % 11000 / 11000f * 360f;
            float angleZ = System.currentTimeMillis() % 12000 / 12000f * 360f;
            poseStack.mulPose(new Quaternion(angleX, angleY, angleZ, true));
        }
        float scale = 0.4763f / (float)Math.sqrt((bounds.getXsize() * bounds.getXsize() + bounds.getYsize() * bounds.getYsize() + bounds.getZsize() * bounds.getZsize()) / 4);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-bounds.getCenter().x, -bounds.getCenter().y, -bounds.getCenter().z);

        if(renderTile){
            if(!(boundTile instanceof EntangledBlockEntity) || depth < 10){
                depth++;
                try{
                    TileEntityRendererDispatcher.instance.render(boundTile, partialTicks, poseStack, bufferSource);
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
