package com.supermartijn642.entangled;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.BlockEntityCustomItemRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeConfig;

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
                entity.setLevel(ClientUtils.getWorld());
                entity.setPosition(BlockPos.ZERO);
                if(stack.hasTag())
                    entity.readData(stack.getTag().getCompound("tileData"));
            }
        );
    }

    @Override
    public void render(ItemStack itemStack){
        if(!itemStack.hasTag() || !itemStack.getTag().contains("tileData") || !itemStack.getTag().getCompound("tileData").getBoolean("bound")){
            this.renderDefaultModel(itemStack);
            return;
        }

        IBakedModel model = ClientUtils.getMinecraft().getBlockRenderer().getBlockModel(Entangled.block.defaultBlockState().setValue(EntangledBlock.STATE_PROPERTY, EntangledBlock.State.BOUND_VALID));
        renderItemModel(itemStack, model);
        super.render(itemStack);
    }

    private static void renderItemModel(ItemStack itemStack, IBakedModel model){
        renderModel(model, -1, itemStack);
        if(itemStack.hasFoil())
            ItemRenderer.renderFoilLayer(ClientUtils.getTextureManager(), () -> renderModel(model, -8372020, ItemStack.EMPTY), 8);
    }

    private static void renderModel(IBakedModel model, int color, ItemStack stack){
        if(ForgeConfig.CLIENT.allowEmissiveItems.get()){
            ForgeHooksClient.renderLitItem(ClientUtils.getMinecraft().getItemRenderer(), model, color, stack);
            return;
        }
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormats.BLOCK_NORMALS);
        Random random = new Random();

        for(Direction direction : Direction.values()){
            random.setSeed(42L);
            ClientUtils.getMinecraft().getItemRenderer().renderQuadList(bufferbuilder, model.getQuads(null, direction, random), color, stack);
        }

        random.setSeed(42L);
        ClientUtils.getMinecraft().getItemRenderer().renderQuadList(bufferbuilder, model.getQuads(null, null, random), color, stack);
        tessellator.end();
    }
}
