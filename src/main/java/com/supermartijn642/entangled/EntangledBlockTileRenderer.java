package com.supermartijn642.entangled;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.ScreenUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
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

        GlStateManager.scale(0.55f, 0.55f, 0.55f);
        GlStateManager.translate(-0.5, -0.5, -0.5);

        if(boundBlock != null && boundTile != null && canRenderTileEntity(boundBlock.getRegistryName())){
            if(!(boundTile instanceof EntangledBlockTile) || depth < 10){
                depth++;
                TileEntityRendererDispatcher.instance.render(boundTile, 0, 0, 0, partialTicks);
                depth--;
            }
        }

        if(state != null && state.getRenderType() == EnumBlockRenderType.MODEL && canRenderBlock(state.getBlock().getRegistryName())){
            GlStateManager.disableLighting();

            ScreenUtils.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            BlockRenderLayer initialLayer = MinecraftForgeClient.getRenderLayer();

            for(BlockRenderLayer layer : BlockRenderLayer.values()){
                if(state.getBlock().canRenderInLayer(state, layer)){
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder buffer = tessellator.getBuffer();
                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                    buffer.setTranslation(tile.getPos().getX(), tile.getPos().getY() - 300, tile.getPos().getZ());

                    if(layer == BlockRenderLayer.TRANSLUCENT){
                        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                        GlStateManager.enableBlend();
                    }

                    ForgeHooksClient.setRenderLayer(layer);
                    try{
                        BlockRendererDispatcher brd = ClientUtils.getMinecraft().getBlockRendererDispatcher();
                        IBakedModel model = brd.getModelForState(state);
                        brd.getBlockModelRenderer().renderModel(tile.getWorld(), model, state, new BlockPos(0, 300, 0), buffer, false);
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    GlStateManager.translate(-tile.getPos().getX(), -tile.getPos().getY(), -tile.getPos().getZ());

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
        return !Entangled.RENDER_BLACKLISTED_MODS.contains(block.getResourceDomain()) && !Entangled.RENDER_BLACKLISTED_BLOCKS.contains(block);
    }

    private static boolean canRenderTileEntity(ResourceLocation tile){
        return !Entangled.RENDER_BLACKLISTED_MODS.contains(tile.getResourceDomain()) && !Entangled.RENDER_BLACKLISTED_TILE_ENTITIES.contains(tile);
    }
}
