package com.supermartijn642.entangled;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

/**
 * Created 5/17/2021 by SuperMartijn642
 */
public class EntangledBlockItemStackTileEntityRenderer extends BlockEntityWithoutLevelRenderer {

    private final BlockEntityRenderDispatcher renderDispatcher;

    public EntangledBlockItemStackTileEntityRenderer(BlockEntityRenderDispatcher renderDispatcher){
        super(renderDispatcher, new EntityModelSet());
        this.renderDispatcher = renderDispatcher;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType cameraTransforms, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay){
        if(!stack.hasTag() || !stack.getTag().contains("tileData") || !stack.getTag().getCompound("tileData").getBoolean("bound")){
            BakedModel model = ClientUtils.getMinecraft().getItemRenderer().getItemModelShaper().getItemModel(stack);
            renderDefaultItem(stack, matrixStack, cameraTransforms, buffer, combinedLight, combinedOverlay, model);
            return;
        }

        EntangledBlockTile tile = new EntangledBlockTile(BlockPos.ZERO, Entangled.block.defaultBlockState());
        tile.setLevel(ClientUtils.getWorld());
        tile.readData(stack.getTag().getCompound("tileData"));

        BakedModel model = ClientUtils.getMinecraft().getBlockRenderer().getBlockModel(Entangled.block.defaultBlockState().setValue(EntangledBlock.ON, true));
        renderDefaultItem(stack, matrixStack, cameraTransforms, buffer, combinedLight, combinedOverlay, model);

        this.renderDispatcher.renderItem(tile, matrixStack, buffer, combinedLight, combinedOverlay);
    }

    private static void renderDefaultItem(ItemStack itemStack, PoseStack matrixStack, ItemTransforms.TransformType cameraTransforms, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay, BakedModel model){
        ItemRenderer renderer = ClientUtils.getMinecraft().getItemRenderer();

        for(BakedModel passModel : model.getRenderPasses(itemStack, true)){
            for(RenderType renderType : passModel.getRenderTypes(itemStack, true)){
                VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(renderTypeBuffer, renderType, true, itemStack.hasFoil());
                renderer.renderModelLists(passModel, itemStack, combinedLight, combinedOverlay, matrixStack, vertexConsumer);
            }
        }
    }
}
