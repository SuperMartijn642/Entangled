package com.supermartijn642.entangled;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ObjectHolder;

import java.util.List;

@Mod("entangled")
public class Entangled {

    public static boolean allowDimensional = true;
    public static int maxDistance = 0;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("entangled", "main"), () -> "1", "1"::equals, "1"::equals);

    @ObjectHolder("entangled:block")
    public static EntangledBlock block;
    @ObjectHolder("entangled:tile")
    public static TileEntityType<EntangledBlockTile> tile;
    @ObjectHolder("entangled:item")
    public static EntangledBinder item;

    public Entangled(){
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EntangledConfig.CONFIG_SPEC);

        CHANNEL.registerMessage(0, EntangledConfigPacket.class, EntangledConfigPacket::encode, EntangledConfigPacket::new, EntangledConfigPacket::handle);
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
            e.getRegistry().register(new BlockItem(block, new Item.Properties().group(ItemGroup.SEARCH)).setRegistryName("block"));
            e.getRegistry().register(new EntangledBinder());
        }
    }

    public static final List<String> RENDER_BLACKLISTED_MODS = Lists.newArrayList();

}
