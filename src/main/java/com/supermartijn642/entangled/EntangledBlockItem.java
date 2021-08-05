package com.supermartijn642.entangled;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.IItemRenderProperties;

import java.util.function.Consumer;

/**
 * Created 8/5/2021 by SuperMartijn642
 */
public class EntangledBlockItem extends BlockItem {

    public EntangledBlockItem(Block block, Properties properties){
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer){
        consumer.accept(new IItemRenderProperties() {
            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer(){
                return new EntangledBlockItemStackTileEntityRenderer(ClientUtils.getMinecraft().getBlockEntityRenderDispatcher());
            }
        });
    }
}
