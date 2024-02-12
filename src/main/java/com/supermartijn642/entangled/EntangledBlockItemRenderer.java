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
import net.minecraft.util.math.BlockPos;

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

        if(model.isLayered()){
            net.minecraftforge.client.ForgeHooksClient.drawItemLayered(renderer, model, itemStack, poseStack, renderTypeBuffer, combinedLight, combinedOverlay, true);
        }else{
            RenderType renderType = RenderTypeLookup.getRenderType(itemStack, true);
            IVertexBuilder vertexConsumer = ItemRenderer.getFoilBufferDirect(renderTypeBuffer, renderType, true, itemStack.hasFoil());
            renderer.renderModelLists(model, itemStack, combinedLight, combinedOverlay, poseStack, vertexConsumer);
        }
    }
}
