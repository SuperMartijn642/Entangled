package com.supermartijn642.entangled;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import org.lwjgl.opengl.GL11;

/**
 * Created 3/16/2020 by SuperMartijn642
 */
public class EntangledBlockTileRenderer extends TileEntitySpecialRenderer<EntangledBlockTile> {

    private static int depth = 0;

    @Override
    public void render(EntangledBlockTile tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha){
        if(!tile.isBound())
            return;

        Block boundBlock = tile.getWorld().provider.getDimensionType().getId() == tile.getBoundDimension() ? tile.getWorld().getBlockState(tile.getBoundBlockPos()).getBlock() : null;
        TileEntity boundTile = tile.getWorld().provider.getDimensionType().getId() == tile.getBoundDimension() ? tile.getWorld().getTileEntity(tile.getBoundBlockPos()) : null;
        IBlockState state = tile.getBoundBlockState();

        GlStateManager.pushMatrix();

        GlStateManager.translate(x, y, z);

        GlStateManager.translate(0.5, 0.5, 0.5);
        float angleX = System.currentTimeMillis() % 10000 / 10000f * 360;
        float angleY = System.currentTimeMillis() % 11000 / 11000f * 360;
        float angleZ = System.currentTimeMillis() % 12000 / 12000f * 360;
        GlStateManager.rotate(angleX, 1, 0, 0);
        GlStateManager.rotate(angleY, 0, 1, 0);
        GlStateManager.rotate(angleZ, 0, 0, 1);

        GlStateManager.scale(0.6f, 0.6f, 0.6f);
        GlStateManager.translate(-0.5, -0.5, -0.5);

        if(boundBlock != null && boundTile != null && !Entangled.RENDER_BLACKLISTED_MODS.contains(boundBlock.getRegistryName().getResourceDomain())){
            if(!(boundTile instanceof EntangledBlockTile) || depth < 10){
                depth++;
                TileEntityRendererDispatcher.instance.render(boundTile, 0, 0, 0, partialTicks);
                depth--;
            }
        }

        if(state != null && state.getRenderType() == EnumBlockRenderType.MODEL){
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

            GlStateManager.disableLighting();

            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            try{
                BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
                IBakedModel model = brd.getModelForState(state);
                brd.getBlockModelRenderer().renderModel(tile.getWorld(), model, state, tile.getPos(), buffer, false);
            }catch(Exception e){
                e.printStackTrace();
            }

            GlStateManager.translate(-tile.getPos().getX(), -tile.getPos().getY(), -tile.getPos().getZ());

            tessellator.draw();
        }

        GlStateManager.popMatrix();
    }
}
