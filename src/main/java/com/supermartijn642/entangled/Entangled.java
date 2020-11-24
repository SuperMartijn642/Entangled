package com.supermartijn642.entangled;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.List;

@Mod(modid = Entangled.MODID, name = Entangled.NAME, version = Entangled.VERSION, acceptedMinecraftVersions = Entangled.MC_VERSIONS)
public class Entangled {

    public static final String MODID = "entangled";
    public static final String NAME = "Entangled";
    public static final String MC_VERSIONS = "[1.12.2]";
    public static final String VERSION = "1.2.8";

    @GameRegistry.ObjectHolder(Entangled.MODID + ":block")
    public static EntangledBlock block;
    @GameRegistry.ObjectHolder(Entangled.MODID + ":item")
    public static Item item;

    @Mod.EventBusSubscriber
    public static class RegistryEvents {
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> e){
            e.getRegistry().register(new EntangledBlock());
            GameRegistry.registerTileEntity(EntangledBlockTile.class, new ResourceLocation(Entangled.MODID, "teentangledblock"));
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> e){
            e.getRegistry().register(new ItemBlock(Entangled.block).setRegistryName(Entangled.block.getRegistryName()));
            e.getRegistry().register(new EntangledBinder());
        }
    }

    public static final List<String> RENDER_BLACKLISTED_MODS = Lists.newArrayList("fluidtank");

}
