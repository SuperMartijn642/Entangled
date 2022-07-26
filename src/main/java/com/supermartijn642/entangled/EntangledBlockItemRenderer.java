package com.supermartijn642.entangled;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.BlockEntityCustomItemRenderer;
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
public class EntangledBlockItemRenderer extends BlockEntityCustomItemRenderer<EntangledBlockEntity> {

    public EntangledBlockItemRenderer(){
        super(
            false,
            () -> new EntangledBlockEntity(BlockPos.ZERO, Entangled.block.defaultBlockState()),
            (stack, entity) -> {
                entity.setLevel(ClientUtils.getWorld());
                if(stack.hasTag())
                    entity.readData(stack.getTag().getCompound("tileData"));
            }
        );
    }

    @Override
    protected void render(ItemStack itemStack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay){
        if(!itemStack.hasTag() || !itemStack.getTag().contains("tileData") || !itemStack.getTag().getCompound("tileData").getBoolean("bound")){
            this.renderDefaultModel(itemStack, transformType, poseStack, bufferSource, combinedLight, combinedOverlay);
            return;
        }

        BakedModel model = ClientUtils.getMinecraft().getBlockRenderer().getBlockModel(Entangled.block.defaultBlockState().setValue(EntangledBlock.ON, true));
        renderItemModel(itemStack, poseStack, bufferSource, combinedLight, combinedOverlay, model);
        super.render(itemStack, transformType, poseStack, bufferSource, combinedLight, combinedOverlay);
    }

    private static void renderItemModel(ItemStack itemStack, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay, BakedModel model){
        ItemRenderer renderer = ClientUtils.getMinecraft().getItemRenderer();

        RenderType renderType = ItemBlockRenderTypes.getRenderType(itemStack, true);
        VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(renderTypeBuffer, renderType, true, itemStack.hasFoil());
        renderer.renderModelLists(model, itemStack, combinedLight, combinedOverlay, poseStack, vertexConsumer);
    }
}
