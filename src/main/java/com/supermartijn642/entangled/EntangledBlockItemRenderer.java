package com.supermartijn642.entangled;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.BlockEntityCustomItemRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
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
                entity.setWorld(ClientUtils.getWorld());
                entity.setPos(BlockPos.ORIGIN);
                if(stack.hasTagCompound())
                    entity.readData(stack.getTagCompound().getCompoundTag("tileData"));
            }
        );
    }

    @Override
    public void render(ItemStack itemStack){
        if(!itemStack.hasTagCompound() || !itemStack.getTagCompound().hasKey("tileData") || !itemStack.getTagCompound().getCompoundTag("tileData").getBoolean("bound")){
            this.renderDefaultModel(itemStack);
            return;
        }

        IBakedModel model = ClientUtils.getBlockRenderer().getModelForState(Entangled.block.getDefaultState().withProperty(EntangledBlock.STATE_PROPERTY, EntangledBlock.State.BOUND_VALID));
        renderItemModel(itemStack, model);
        super.render(itemStack);
    }

    private static void renderItemModel(ItemStack itemStack, IBakedModel model){
        RenderItem renderer = ClientUtils.getItemRenderer();
        renderer.renderModel(model, itemStack);
        if(itemStack.hasEffect())
            renderer.renderEffect(model);
    }
}
