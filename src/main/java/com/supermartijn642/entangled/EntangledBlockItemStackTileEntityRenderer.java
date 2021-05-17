package com.supermartijn642.entangled;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

/**
 * Created 5/17/2021 by SuperMartijn642
 */
public class EntangledBlockItemStackTileEntityRenderer extends ItemStackTileEntityRenderer {

    public static final EntangledBlockItemStackTileEntityRenderer INSTANCE = new EntangledBlockItemStackTileEntityRenderer();

    private EntangledBlockItemStackTileEntityRenderer(){
    }

    @Override
    public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType cameraTransforms, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay){
        if(!stack.hasTag() || !stack.getTag().contains("tileData") || !stack.getTag().getCompound("tileData").getBoolean("bound")){
            IBakedModel model = ClientUtils.getMinecraft().getItemRenderer().getItemModelMesher().getItemModel(stack);
            renderDefaultItem(stack, matrixStack, cameraTransforms, buffer, combinedLight, combinedOverlay, model);
            return;
        }

        EntangledBlockTile tile = new EntangledBlockTile();
        tile.setWorldAndPos(ClientUtils.getMinecraft().world, BlockPos.ZERO);
        tile.readData(stack.getTag().getCompound("tileData"));

        IBakedModel model = ClientUtils.getMinecraft().getBlockRendererDispatcher().getModelForState(Entangled.block.getDefaultState().with(EntangledBlock.ON, true));
        renderDefaultItem(stack, matrixStack, cameraTransforms, buffer, combinedLight, combinedOverlay, model);

        TileEntityRendererDispatcher.instance.renderItem(tile, matrixStack, buffer, combinedLight, combinedOverlay);
    }

    private static void renderDefaultItem(ItemStack itemStack, MatrixStack matrixStack, ItemCameraTransforms.TransformType cameraTransforms, IRenderTypeBuffer renderTypeBuffer, int combinedLight, int combinedOverlay, IBakedModel model){
        ItemRenderer renderer = ClientUtils.getMinecraft().getItemRenderer();

        matrixStack.push();

        if(model.isLayered()){
            net.minecraftforge.client.ForgeHooksClient.drawItemLayered(renderer, model, itemStack, matrixStack, renderTypeBuffer, combinedLight, combinedOverlay, true);
        }else{
            RenderType rendertype = RenderTypeLookup.func_239219_a_(itemStack, true);
            IVertexBuilder ivertexbuilder;

            ivertexbuilder = ItemRenderer.getEntityGlintVertexBuilder(renderTypeBuffer, rendertype, true, itemStack.hasEffect());

            renderer.renderModel(model, itemStack, combinedLight, combinedOverlay, matrixStack, ivertexbuilder);
        }

        matrixStack.pop();
    }
}
