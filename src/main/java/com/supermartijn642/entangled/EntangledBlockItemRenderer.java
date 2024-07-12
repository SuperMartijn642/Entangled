package com.supermartijn642.entangled;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.render.BlockEntityCustomItemRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemDisplayContext;
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
                CompoundTag data = stack.get(BaseBlock.TILE_DATA);
                if(data != null)
                    entity.readData(data);
            }
        );
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay){
        if(stack.get(BaseBlock.TILE_DATA) == null || !stack.get(BaseBlock.TILE_DATA).getBoolean("bound")){
            this.renderDefaultModel(stack, transformType, poseStack, bufferSource, combinedLight, combinedOverlay);
            return;
        }

        BakedModel model = ClientUtils.getMinecraft().getBlockRenderer().getBlockModel(Entangled.block.defaultBlockState().setValue(EntangledBlock.STATE_PROPERTY, EntangledBlock.State.BOUND_VALID));
        renderItemModel(stack, poseStack, bufferSource, combinedLight, combinedOverlay, model);
        super.render(stack, transformType, poseStack, bufferSource, combinedLight, combinedOverlay);
    }

    private static void renderItemModel(ItemStack itemStack, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay, BakedModel model){
        ItemRenderer renderer = ClientUtils.getMinecraft().getItemRenderer();

        RenderType renderType = ItemBlockRenderTypes.getRenderType(itemStack, true);
        VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(renderTypeBuffer, renderType, true, itemStack.hasFoil());
        renderer.renderModelLists(model, itemStack, combinedLight, combinedOverlay, poseStack, vertexConsumer);
    }
}
