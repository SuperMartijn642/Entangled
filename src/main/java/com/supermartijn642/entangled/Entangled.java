package com.supermartijn642.entangled;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import java.util.List;

@Mod("entangled")
public class Entangled {

    @ObjectHolder("entangled:block")
    public static EntangledBlock block;
    @ObjectHolder("entangled:tile")
    public static TileEntityType<EntangledBlockTile> tile;
    @ObjectHolder("entangled:item")
    public static EntangledBinder item;

    public Entangled(){
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlockRegistry(final RegistryEvent.Register<Block> e){
            e.getRegistry().register(new EntangledBlock());
        }

        @SubscribeEvent
        public static void onTileRegistry(final RegistryEvent.Register<TileEntityType<?>> e){
            e.getRegistry().register(TileEntityType.Builder.create(EntangledBlockTile::new, block).build(null).setRegistryName("tile"));
        }

        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> e){
            e.getRegistry().register(new BlockItem(block, new Item.Properties().group(ItemGroup.SEARCH).setISTER(() -> EntangledBlockItemStackTileEntityRenderer::new)).setRegistryName("block"));
            e.getRegistry().register(new EntangledBinder());
        }
    }

    public static final List<String> RENDER_BLACKLISTED_MODS = Lists.newArrayList();

}
