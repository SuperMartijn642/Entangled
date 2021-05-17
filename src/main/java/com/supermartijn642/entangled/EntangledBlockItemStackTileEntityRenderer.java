package com.supermartijn642.entangled;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.Random;

/**
 * Created 5/17/2021 by SuperMartijn642
 */
public class EntangledBlockItemStackTileEntityRenderer extends ItemStackTileEntityRenderer {

    public static final EntangledBlockItemStackTileEntityRenderer INSTANCE = new EntangledBlockItemStackTileEntityRenderer();

    private EntangledBlockItemStackTileEntityRenderer(){
    }

    @Override
    public void render(ItemStack stack, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay){
        if(!stack.hasTag() || !stack.getTag().contains("tileData") || !stack.getTag().getCompound("tileData").getBoolean("bound")){
            IBakedModel model = ClientUtils.getMinecraft().getItemRenderer().getItemModelMesher().getItemModel(stack);
            renderDefaultItem(stack, matrixStack, buffer, combinedLight, combinedOverlay, model);
            return;
        }

        EntangledBlockTile tile = new EntangledBlockTile();
        tile.setWorldAndPos(ClientUtils.getMinecraft().world, BlockPos.ZERO);
        tile.readData(stack.getTag().getCompound("tileData"));

        IBakedModel model = ClientUtils.getMinecraft().getBlockRendererDispatcher().getModelForState(Entangled.block.getDefaultState().with(EntangledBlock.ON, true));
        renderDefaultItem(stack, matrixStack, buffer, combinedLight, combinedOverlay, model);

        TileEntityRendererDispatcher.instance.renderItem(tile, matrixStack, buffer, combinedLight, combinedOverlay);
    }

    private static void renderDefaultItem(ItemStack itemStack, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int combinedLight, int combinedOverlay, IBakedModel model){
        RenderType rendertype = RenderTypeLookup.getRenderType(itemStack);
        RenderType rendertype1;
        if (Objects.equals(rendertype, Atlases.getTranslucentBlockType())) {
            rendertype1 = Atlases.getTranslucentCullBlockType();
        } else {
            rendertype1 = rendertype;
        }

        IVertexBuilder ivertexbuilder = ItemRenderer.getBuffer(renderTypeBuffer, rendertype1, true, itemStack.hasEffect());
        renderModel(model, itemStack, combinedLight, combinedOverlay, matrixStack, ivertexbuilder);
    }

    private static void renderModel(IBakedModel modelIn, ItemStack stack, int combinedLightIn, int combinedOverlayIn, MatrixStack matrixStackIn, IVertexBuilder bufferIn) {
        Random random = new Random();

        for(Direction direction : Direction.values()) {
            random.setSeed(42L);
            ClientUtils.getMinecraft().getItemRenderer().renderQuads(matrixStackIn, bufferIn, modelIn.getQuads(null, direction, random), stack, combinedLightIn, combinedOverlayIn);
        }

        random.setSeed(42L);
        ClientUtils.getMinecraft().getItemRenderer().renderQuads(matrixStackIn, bufferIn, modelIn.getQuads(null, null, random), stack, combinedLightIn, combinedOverlayIn);
    }
}
