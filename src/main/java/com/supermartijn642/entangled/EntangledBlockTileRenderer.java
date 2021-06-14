package com.supermartijn642.entangled;

import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.gui.ScreenUtils;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
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

        TileEntity boundTile = tile.getWorld().getDimension().getType().getId() == tile.getBoundDimension() ? tile.getWorld().getTileEntity(tile.getBoundBlockPos()) : null;
        BlockState boundState = tile.getBoundBlockState();

        boolean renderTile = boundTile != null && canRenderTileEntity(boundTile.getType().getRegistryName());
        boolean renderBlock = boundState != null && boundState.getRenderType() == BlockRenderType.MODEL && canRenderBlock(boundState.getBlock().getRegistryName());

        // get the bounding box
        AxisAlignedBB bounds = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
        if(renderBlock && tile.getWorld().getDimension().getType().getId() == tile.getBoundDimension()){
            VoxelShape shape = boundState.getRenderShape(tile.getWorld(), tile.getBoundBlockPos());
            if(!shape.isEmpty())
                bounds = shape.getBoundingBox();
        }

        GlStateManager.pushMatrix();

        GlStateManager.translated(x, y, z);

        // rotate and scale
        GlStateManager.translated(0.5, 0.5, 0.5);
        float angleX = System.currentTimeMillis() % 10000 / 10000f * 360f;
        float angleY = System.currentTimeMillis() % 11000 / 11000f * 360f;
        float angleZ = System.currentTimeMillis() % 12000 / 12000f * 360f;
        GlStateManager.rotatef(angleX, 1, 0, 0);
        GlStateManager.rotatef(angleY, 0, 1, 0);
        GlStateManager.rotatef(angleZ, 0, 0, 1);
        float scale = 0.4763f / (float)Math.sqrt((bounds.getXSize() * bounds.getXSize() + bounds.getYSize() * bounds.getYSize() + bounds.getZSize() * bounds.getZSize()) / 4);
        GlStateManager.scalef(scale, scale, scale);
        GlStateManager.translated(-bounds.getCenter().x, -bounds.getCenter().y, -bounds.getCenter().z);

        if(renderTile){
            if(!(boundTile instanceof EntangledBlockTile) || depth < 10){
                depth++;
                TileEntityRendererDispatcher.instance.render(boundTile, 0, 0, 0, partialTicks);
                depth--;
            }
        }

        if(renderBlock){
            GlStateManager.disableLighting();

            ScreenUtils.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

            BlockRenderLayer initialLayer = MinecraftForgeClient.getRenderLayer();

            for(BlockRenderLayer layer : BlockRenderLayer.values()){
                if(boundState.getBlock().canRenderInLayer(boundState, layer)){
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder buffer = tessellator.getBuffer();
                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                    buffer.setTranslation(tile.getPos().getX(), tile.getPos().getY() - 300, tile.getPos().getZ());

                    if(layer == BlockRenderLayer.TRANSLUCENT){
                        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                        GlStateManager.enableBlend();
                    }

                    ForgeHooksClient.setRenderLayer(layer);
                    IModelData data = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(boundState).getModelData(tile.getWorld(), tile.getPos(), boundState, EmptyModelData.INSTANCE);
                    try{
                        BlockRendererDispatcher brd = Minecraft.getInstance().getBlockRendererDispatcher();
                        IBakedModel model = brd.getModelForState(boundState);
                        brd.getBlockModelRenderer().renderModel(tile.getWorld(), model, boundState, new BlockPos(0, 300, 0), buffer, false, new Random(), 0, data);
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

            ForgeHooksClient.setRenderLayer(initialLayer);
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
