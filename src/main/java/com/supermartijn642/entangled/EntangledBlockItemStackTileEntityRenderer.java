package com.supermartijn642.entangled;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.supermartijn642.core.ClientUtils;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

/**
 * Created 5/17/2021 by SuperMartijn642
 */
public class EntangledBlockItemStackTileEntityRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    @Override
    public void render(ItemStack stack, ItemTransforms.TransformType cameraTransforms, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay){
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

        ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().renderItem(tile, matrixStack, buffer, combinedLight, combinedOverlay);
    }

    private static void renderDefaultItem(ItemStack itemStack, PoseStack matrixStack, ItemTransforms.TransformType cameraTransforms, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay, BakedModel model){
        ItemRenderer renderer = ClientUtils.getMinecraft().getItemRenderer();

        matrixStack.pushPose();

        RenderType rendertype = ItemBlockRenderTypes.getRenderType(itemStack, true);
        VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(renderTypeBuffer, rendertype, true, itemStack.hasFoil());
        renderer.renderModelLists(model, itemStack, combinedLight, combinedOverlay, matrixStack, vertexConsumer);

        matrixStack.popPose();
    }
}
