package com.supermartijn642.entangled;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

/**
 * Created 5/17/2021 by SuperMartijn642
 */
public class EntangledBlockItemStackTileEntityRenderer extends ItemStackTileEntityRenderer {

    @Override
    public void renderByItem(ItemStack stack){
        if(!stack.hasTag() || !stack.getTag().contains("tileData") || !stack.getTag().getCompound("tileData").getBoolean("bound")){
            IBakedModel model = ClientUtils.getMinecraft().getItemRenderer().getItemModelMesher().getItemModel(stack);
            renderDefaultItem(stack, model);
            return;
        }

        EntangledBlockTile tile = new EntangledBlockTile();
        tile.setWorld(ClientUtils.getMinecraft().world);
        tile.setPos(BlockPos.ZERO);
        tile.readData(stack.getTag().getCompound("tileData"));

        IBakedModel model = ClientUtils.getMinecraft().getBlockRendererDispatcher().getModelForState(Entangled.block.getDefaultState().with(EntangledBlock.ON, true));
        renderDefaultItem(stack, model);

        TileEntityRendererDispatcher.instance.renderAsItem(tile);
    }

    private static void renderDefaultItem(ItemStack itemStack, IBakedModel model){
        renderModel(model, -1, itemStack);
        if(itemStack.hasEffect()){
            ItemRenderer.renderEffect(ClientUtils.getTextureManager(), () -> {
                renderModel(model, -8372020, ItemStack.EMPTY);
            }, 8);
        }
    }

    private static void renderModel(IBakedModel model, int color, ItemStack stack){
        if(net.minecraftforge.common.ForgeConfig.CLIENT.allowEmissiveItems.get()){
            net.minecraftforge.client.ForgeHooksClient.renderLitItem(ClientUtils.getMinecraft().getItemRenderer(), model, color, stack);
            return;
        }
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.ITEM);
        Random random = new Random();

        for(Direction direction : Direction.values()){
            random.setSeed(42L);
            ClientUtils.getMinecraft().getItemRenderer().renderQuads(bufferbuilder, model.getQuads(null, direction, random), color, stack);
        }

        random.setSeed(42L);
        ClientUtils.getMinecraft().getItemRenderer().renderQuads(bufferbuilder, model.getQuads(null, null, random), color, stack);
        tessellator.draw();
    }
}
