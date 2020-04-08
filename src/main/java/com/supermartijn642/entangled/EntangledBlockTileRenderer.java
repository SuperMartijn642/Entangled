package com.supermartijn642.entangled;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.lwjgl.opengl.GL11;

import java.util.Random;

/**
 * Created 3/16/2020 by SuperMartijn642
 */
public class EntangledBlockTileRenderer extends TileEntityRenderer<EntangledBlockTile> {

    @Override
    public void render(EntangledBlockTile tile, double x, double y, double z, float partialTicks, int destroyStage){
        if(!tile.isBound())
            return;

        TileEntity boundTile = tile.getWorld().getDimension().getType().getId() == tile.getBoundDimension() ? tile.getWorld().getTileEntity(tile.getBoundBlockPos()) : null;
        BlockState state = tile.getBoundBlockState();

        GlStateManager.pushMatrix();

        GlStateManager.translated(x,y,z);

        GlStateManager.translated(0.5, 0.5, 0.5);
        float angleX = System.currentTimeMillis() % 10000 / 10000f * 360;
        float angleY = System.currentTimeMillis() % 11000 / 11000f * 360;
        float angleZ = System.currentTimeMillis() % 12000 / 12000f * 360;
        GlStateManager.rotatef(angleX,1,0,0);
        GlStateManager.rotatef(angleY,0,1,0);
        GlStateManager.rotatef(angleZ,0,0,1);

        GlStateManager.scalef(0.6f, 0.6f, 0.6f);
        GlStateManager.translated(-0.5, -0.5, -0.5);

        if(boundTile != null)
            TileEntityRendererDispatcher.instance.render(boundTile, 0, 0, 0, partialTicks);

        if(state != null){
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

            GlStateManager.disableLighting();

            Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

            IModelData data = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state).getModelData(tile.getWorld(), tile.getPos(), state, EmptyModelData.INSTANCE);
            try{
                BlockRendererDispatcher brd = Minecraft.getInstance().getBlockRendererDispatcher();
                IBakedModel model = brd.getModelForState(state);
                brd.getBlockModelRenderer().renderModel(tile.getWorld(), model, state, tile.getPos(), buffer, false, new Random(), 0, data);
            }catch(Exception e){
                e.printStackTrace();
            }

            GlStateManager.translated(-tile.getPos().getX(), -tile.getPos().getY(), -tile.getPos().getZ());

            tessellator.draw();
        }

        GlStateManager.popMatrix();
    }
}
