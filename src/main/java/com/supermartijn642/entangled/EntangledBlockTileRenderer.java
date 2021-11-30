package com.supermartijn642.entangled;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Created 3/16/2020 by SuperMartijn642
 */
public class EntangledBlockTileRenderer implements BlockEntityRenderer<EntangledBlockTile> {

    private static int depth = 0;

    @Override
    public void render(EntangledBlockTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay){
        if(!tile.isBound())
            return;

        BlockEntity boundTile = tile.getLevel().dimension() == tile.getBoundDimension() ? tile.getLevel().getBlockEntity(tile.getBoundBlockPos()) : null;
        BlockState boundState = tile.getBoundBlockState();

        boolean renderTile = boundTile != null && canRenderTileEntity(boundTile.getType().getRegistryName());
        boolean renderBlock = boundState != null && boundState.getRenderShape() == RenderShape.MODEL && canRenderBlock(boundState.getBlock().getRegistryName());

        // get the bounding box
        AABB bounds = new AABB(0, 0, 0, 1, 1, 1);
        if(renderBlock && tile.getLevel().dimension() == tile.getBoundDimension()){
            VoxelShape shape = boundState.getOcclusionShape(tile.getLevel(), tile.getBoundBlockPos());
            if(!shape.isEmpty())
                bounds = shape.bounds();
        }

        matrixStack.pushPose();

        // rotate and scale
        matrixStack.translate(0.5, 0.5, 0.5);
        if(EntangledConfig.rotateRenderedBlock.get()){
            float angleX = System.currentTimeMillis() % 10000 / 10000f * 360f;
            float angleY = System.currentTimeMillis() % 11000 / 11000f * 360f;
            float angleZ = System.currentTimeMillis() % 12000 / 12000f * 360f;
            matrixStack.mulPose(new Quaternion(angleX, angleY, angleZ, true));
        }
        float scale = 0.4763f / (float)Math.sqrt((bounds.getXsize() * bounds.getXsize() + bounds.getYsize() * bounds.getYsize() + bounds.getZsize() * bounds.getZsize()) / 4);
        matrixStack.scale(scale, scale, scale);
        matrixStack.translate(-bounds.getCenter().x, -bounds.getCenter().y, -bounds.getCenter().z);

        if(renderTile){
            if(!(boundTile instanceof EntangledBlockTile) || depth < 10){
                depth++;
                ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().render(boundTile, partialTicks, matrixStack, buffer);
                depth--;
            }
        }
        if(renderBlock)
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(boundState, matrixStack, buffer, combinedLight, combinedOverlay);

        matrixStack.popPose();
    }

    private static boolean canRenderBlock(ResourceLocation block){
        return !Entangled.RENDER_BLACKLISTED_MODS.contains(block.getNamespace()) && !Entangled.RENDER_BLACKLISTED_BLOCKS.contains(block);
    }

    private static boolean canRenderTileEntity(ResourceLocation tile){
        return !Entangled.RENDER_BLACKLISTED_MODS.contains(tile.getNamespace()) && !Entangled.RENDER_BLACKLISTED_TILE_ENTITIES.contains(tile);
    }
}
