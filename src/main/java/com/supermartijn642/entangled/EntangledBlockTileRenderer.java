package com.supermartijn642.entangled;

import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.gui.ScreenUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
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
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.lwjgl.opengl.GL11;

import java.util.Random;

/**
 * Created 3/16/2020 by SuperMartijn642
 */
public class EntangledBlockTileRenderer extends TileEntityRenderer<EntangledBlockTile> {

    private static int depth = 0;

    @Override
    public void render(EntangledBlockTile tile, double x, double y, double z, float partialTicks, int destroyStage){
        if(!tile.isBound())
            return;

        Block boundBlock = tile.getWorld().getDimension().getType().getId() == tile.getBoundDimension() ? tile.getWorld().getBlockState(tile.getBoundBlockPos()).getBlock() : null;
        TileEntity boundTile = tile.getWorld().getDimension().getType().getId() == tile.getBoundDimension() ? tile.getWorld().getTileEntity(tile.getBoundBlockPos()) : null;
        BlockState state = tile.getBoundBlockState();

        GlStateManager.pushMatrix();

        GlStateManager.translated(x, y, z);

        GlStateManager.translated(0.5, 0.5, 0.5);
        float angleX = System.currentTimeMillis() % 10000 / 10000f * 360;
        float angleY = System.currentTimeMillis() % 11000 / 11000f * 360;
        float angleZ = System.currentTimeMillis() % 12000 / 12000f * 360;
        GlStateManager.rotatef(angleX, 1, 0, 0);
        GlStateManager.rotatef(angleY, 0, 1, 0);
        GlStateManager.rotatef(angleZ, 0, 0, 1);

        GlStateManager.scalef(0.55f, 0.55f, 0.55f);
        GlStateManager.translated(-0.5, -0.5, -0.5);

        if(boundBlock != null && boundTile != null && canRenderTileEntity(boundBlock.getRegistryName())){
            if(!(boundTile instanceof EntangledBlockTile) || depth < 10){
                depth++;
                TileEntityRendererDispatcher.instance.render(boundTile, 0, 0, 0, partialTicks);
                depth--;
            }
        }

        if(state != null && state.getRenderType() == BlockRenderType.MODEL && canRenderBlock(state.getBlock().getRegistryName())){
            GlStateManager.disableLighting();

            ScreenUtils.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

            for(BlockRenderLayer layer : BlockRenderLayer.values()){
                if(state.getBlock().canRenderInLayer(state, layer)){
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder buffer = tessellator.getBuffer();
                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                    buffer.setTranslation(tile.getPos().getX(), tile.getPos().getY() - 300, tile.getPos().getZ());

                    if(layer == BlockRenderLayer.TRANSLUCENT){
                        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                        GlStateManager.enableBlend();
                    }
                    ForgeHooksClient.setRenderLayer(layer);
                    IModelData data = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state).getModelData(tile.getWorld(), tile.getPos(), state, EmptyModelData.INSTANCE);
                    try{
                        BlockRendererDispatcher brd = Minecraft.getInstance().getBlockRendererDispatcher();
                        IBakedModel model = brd.getModelForState(state);
                        brd.getBlockModelRenderer().renderModel(tile.getWorld(), model, state, new BlockPos(0, 300, 0), buffer, false, new Random(), 0, data);
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    GlStateManager.translated(-tile.getPos().getX(), -tile.getPos().getY(), -tile.getPos().getZ());

                    buffer.setTranslation(0, 0, 0);
                    tessellator.draw();

                    if(layer == BlockRenderLayer.TRANSLUCENT){
                        GlStateManager.disableBlend();
                    }
                }
            }
        }

        GlStateManager.popMatrix();
    }

    private static boolean canRenderBlock(ResourceLocation block){
        return !Entangled.RENDER_BLACKLISTED_MODS.contains(block.getNamespace()) && !Entangled.RENDER_BLACKLISTED_BLOCKS.contains(block);
    }

    private static boolean canRenderTileEntity(ResourceLocation tile){
        return !Entangled.RENDER_BLACKLISTED_MODS.contains(tile.getNamespace()) && !Entangled.RENDER_BLACKLISTED_TILE_ENTITIES.contains(tile);
    }
}
