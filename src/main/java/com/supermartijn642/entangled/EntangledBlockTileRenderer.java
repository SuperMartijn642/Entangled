package com.supermartijn642.entangled;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Quaternion;

/**
 * Created 3/16/2020 by SuperMartijn642
 */
public class EntangledBlockTileRenderer extends TileEntityRenderer<EntangledBlockTile> {

    private static int depth = 0;

    public EntangledBlockTileRenderer(TileEntityRendererDispatcher dispatcher){
        super(dispatcher);
    }

    @Override
    public void render(EntangledBlockTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay){
        if(!tile.isBound())
            return;

        TileEntity boundTile = tile.getWorld().getDimensionKey() == tile.getBoundDimension() ? tile.getWorld().getTileEntity(tile.getBoundBlockPos()) : null;
        BlockState boundState = tile.getBoundBlockState();

        boolean renderTile = boundTile != null && canRenderTileEntity(boundTile.getType().getRegistryName());
        boolean renderBlock = boundState != null && boundState.getRenderType() == BlockRenderType.MODEL && canRenderBlock(boundState.getBlock().getRegistryName());

        // get the bounding box
        AxisAlignedBB bounds = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
        if(renderBlock && tile.getWorld().getDimensionKey() == tile.getBoundDimension()){
            VoxelShape shape = boundState.getRenderShape(tile.getWorld(), tile.getBoundBlockPos());
            if(!shape.isEmpty())
                bounds = shape.getBoundingBox();
        }

        matrixStack.push();

        // rotate and scale
        matrixStack.translate(0.5, 0.5, 0.5);
        float angleX = System.currentTimeMillis() % 10000 / 10000f * 360f;
        float angleY = System.currentTimeMillis() % 11000 / 11000f * 360f;
        float angleZ = System.currentTimeMillis() % 12000 / 12000f * 360f;
        matrixStack.rotate(new Quaternion(angleX, angleY, angleZ, true));
        float scale = 0.4763f / (float)Math.sqrt((bounds.getXSize() * bounds.getXSize() + bounds.getYSize() * bounds.getYSize() + bounds.getZSize() * bounds.getZSize()) / 4);
        matrixStack.scale(scale, scale, scale);
        matrixStack.translate(-bounds.getXSize() / 2, -bounds.getYSize() / 2, -bounds.getZSize() / 2);

        if(renderTile){
            if(!(boundTile instanceof EntangledBlockTile) || depth < 10){
                depth++;
                TileEntityRendererDispatcher.instance.renderTileEntity(boundTile, partialTicks, matrixStack, buffer);
                depth--;
            }
        }
        if(renderBlock)
            Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(boundState, matrixStack, buffer, combinedLight, combinedOverlay);

        matrixStack.pop();
    }

    private static boolean canRenderBlock(ResourceLocation block){
        return !Entangled.RENDER_BLACKLISTED_MODS.contains(block.getNamespace()) && !Entangled.RENDER_BLACKLISTED_BLOCKS.contains(block);
    }

    private static boolean canRenderTileEntity(ResourceLocation tile){
        return !Entangled.RENDER_BLACKLISTED_MODS.contains(tile.getNamespace()) && !Entangled.RENDER_BLACKLISTED_TILE_ENTITIES.contains(tile);
    }
}
