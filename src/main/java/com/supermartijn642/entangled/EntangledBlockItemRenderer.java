package com.supermartijn642.entangled;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.BlockEntityCustomItemRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

/**
 * Created 5/17/2021 by SuperMartijn642
 */
public class EntangledBlockItemRenderer extends BlockEntityCustomItemRenderer<EntangledBlockEntity> {

    public EntangledBlockItemRenderer(){
        super(
            false,
            EntangledBlockEntity::new,
            (stack, entity) -> {
                entity.setLevelAndPosition(ClientUtils.getWorld(), BlockPos.ZERO);
                if(stack.hasTag())
                    entity.readData(stack.getTag().getCompound("tileData"));
            }
        );
    }

    @Override
    public void render(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay){
        if(!itemStack.hasTag() || !itemStack.getTag().contains("tileData") || !itemStack.getTag().getCompound("tileData").getBoolean("bound")){
            this.renderDefaultModel(itemStack, transformType, poseStack, bufferSource, combinedLight, combinedOverlay);
            return;
        }

        IBakedModel model = ClientUtils.getMinecraft().getBlockRenderer().getBlockModel(Entangled.block.defaultBlockState().setValue(EntangledBlock.STATE_PROPERTY, EntangledBlock.State.BOUND_VALID));
        renderItemModel(itemStack, poseStack, bufferSource, combinedLight, combinedOverlay, model);
        super.render(itemStack, transformType, poseStack, bufferSource, combinedLight, combinedOverlay);
    }

    private static void renderItemModel(ItemStack itemStack, MatrixStack poseStack, IRenderTypeBuffer renderTypeBuffer, int combinedLight, int combinedOverlay, IBakedModel model){
        ItemRenderer renderer = ClientUtils.getMinecraft().getItemRenderer();

        RenderType renderType = RenderTypeLookup.getRenderType(itemStack);
        IVertexBuilder vertexConsumer = ItemRenderer.getFoilBuffer(renderTypeBuffer, renderType, true, itemStack.hasFoil());
        renderModel(model, itemStack, combinedLight, combinedOverlay, poseStack, vertexConsumer);
    }

    private static void renderModel(IBakedModel modelIn, ItemStack stack, int combinedLightIn, int combinedOverlayIn, MatrixStack matrixStackIn, IVertexBuilder bufferIn){
        Random random = new Random();

        for(Direction direction : Direction.values()){
            random.setSeed(42);
            ClientUtils.getMinecraft().getItemRenderer().renderQuadList(matrixStackIn, bufferIn, modelIn.getQuads(null, direction, random), stack, combinedLightIn, combinedOverlayIn);
        }

        random.setSeed(42);
        ClientUtils.getMinecraft().getItemRenderer().renderQuadList(matrixStackIn, bufferIn, modelIn.getQuads(null, null, random), stack, combinedLightIn, combinedOverlayIn);
    }
}
