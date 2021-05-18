package com.supermartijn642.entangled;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

/**
 * Created 5/17/2021 by SuperMartijn642
 */
public class EntangledBlockItemStackTileEntityRenderer extends TileEntityItemStackRenderer {

    public static final EntangledBlockItemStackTileEntityRenderer INSTANCE = new EntangledBlockItemStackTileEntityRenderer();

    private EntangledBlockItemStackTileEntityRenderer(){
    }

    @Override
    public void renderByItem(ItemStack stack, float partialTicks){
        if(!stack.hasTagCompound() || !stack.getTagCompound().hasKey("tileData") || !stack.getTagCompound().getCompoundTag("tileData").getBoolean("bound")){
            IBakedModel model = ClientUtils.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
            renderDefaultItem(stack, model);
            return;
        }

        EntangledBlockTile tile = new EntangledBlockTile();
        tile.setWorld(ClientUtils.getMinecraft().world);
        tile.setPos(BlockPos.ORIGIN);
        tile.readData(stack.getTagCompound().getCompoundTag("tileData"));

        IBakedModel model = ClientUtils.getMinecraft().getBlockRendererDispatcher().getModelForState(Entangled.block.getDefaultState().withProperty(EntangledBlock.ON, true));
        renderDefaultItem(stack, model);

        TileEntityRendererDispatcher.instance.render(tile, 0, 0, 0, ClientUtils.getMinecraft().getRenderPartialTicks());
    }

    private static void renderDefaultItem(ItemStack itemStack, IBakedModel model){
        renderModel(model, -1, itemStack);
        if(itemStack.hasEffect()){
            renderEffect(model);
        }
    }

    private static void renderModel(IBakedModel model, int color, ItemStack stack){
        if(net.minecraftforge.common.ForgeModContainer.allowEmissiveItems){
            net.minecraftforge.client.ForgeHooksClient.renderLitItem(ClientUtils.getMinecraft().getRenderItem(), model, color, stack);
            return;
        }
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.ITEM);

        for(EnumFacing enumfacing : EnumFacing.values()){
            ClientUtils.getMinecraft().getRenderItem().renderQuads(bufferbuilder, model.getQuads(null, enumfacing, 0L), color, stack);
        }

        ClientUtils.getMinecraft().getRenderItem().renderQuads(bufferbuilder, model.getQuads(null, null, 0L), color, stack);
        tessellator.draw();
    }

    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    private static void renderEffect(IBakedModel model){
        GlStateManager.depthMask(false);
        GlStateManager.depthFunc(514);
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
        ScreenUtils.bindTexture(RES_ITEM_GLINT);
        GlStateManager.matrixMode(5890);
        GlStateManager.pushMatrix();
        GlStateManager.scale(8.0F, 8.0F, 8.0F);
        float f = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
        GlStateManager.translate(f, 0.0F, 0.0F);
        GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
        renderModel(model, -8372020, ItemStack.EMPTY);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.scale(8.0F, 8.0F, 8.0F);
        float f1 = (float)(Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
        GlStateManager.translate(-f1, 0.0F, 0.0F);
        GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
        renderModel(model, -8372020, ItemStack.EMPTY);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableLighting();
        GlStateManager.depthFunc(515);
        GlStateManager.depthMask(true);
        ScreenUtils.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    }
}
