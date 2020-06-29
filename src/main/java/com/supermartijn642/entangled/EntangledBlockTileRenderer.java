package com.supermartijn642.entangled;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Quaternion;

/**
 * Created 3/16/2020 by SuperMartijn642
 */
public class EntangledBlockTileRenderer extends TileEntityRenderer<EntangledBlockTile> {

    public EntangledBlockTileRenderer(TileEntityRendererDispatcher dispatcher){
        super(dispatcher);
    }

    @Override
    public void render(EntangledBlockTile tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn){
        if(!tileEntityIn.isBound())
            return;

        Block boundBlock = tileEntityIn.getWorld().func_234923_W_() == tileEntityIn.getBoundDimension() ? tileEntityIn.getWorld().getBlockState(tileEntityIn.getBoundBlockPos()).getBlock() : null;
        TileEntity boundTile = tileEntityIn.getWorld().func_234923_W_() == tileEntityIn.getBoundDimension() ? tileEntityIn.getWorld().getTileEntity(tileEntityIn.getBoundBlockPos()) : null;
        BlockState state = tileEntityIn.getBoundBlockState();

        matrixStackIn.push();

        matrixStackIn.translate(0.5, 0.5, 0.5);
        float angleX = System.currentTimeMillis() % 10000 / 10000f * 360f;
        float angleY = System.currentTimeMillis() % 11000 / 11000f * 360f;
        float angleZ = System.currentTimeMillis() % 12000 / 12000f * 360f;
        matrixStackIn.rotate(new Quaternion(angleX, angleY, angleZ, true));

        matrixStackIn.scale(0.6f, 0.6f, 0.6f);
        matrixStackIn.translate(-0.5, -0.5, -0.5);

        if(boundBlock != null && boundTile != null && !Entangled.RENDER_BLACKLISTED_MODS.contains(boundBlock.getRegistryName().getNamespace()))
            TileEntityRendererDispatcher.instance.renderTileEntity(boundTile, partialTicks, matrixStackIn, bufferIn);
        if(state != null && state.getRenderType() == BlockRenderType.MODEL)
            Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);

        matrixStackIn.pop();
    }
}
