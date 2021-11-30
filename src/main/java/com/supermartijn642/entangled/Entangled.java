package com.supermartijn642.entangled;

import com.google.common.collect.Sets;
import com.supermartijn642.entangled.integration.TheOneProbePlugin;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Set;

@Mod(modid = Entangled.MODID, name = Entangled.NAME, version = Entangled.VERSION, dependencies = Entangled.DEPENDENCIES)
public class Entangled {

    public static final String MODID = "entangled";
    public static final String NAME = "Entangled";
    public static final String VERSION = "1.3.10";
    public static final String DEPENDENCIES = "required-after:supermartijn642configlib@[1.0.9,);required-after:supermartijn642corelib@[1.0.14,)";

    @GameRegistry.ObjectHolder(Entangled.MODID + ":block")
    public static EntangledBlock block;
    @GameRegistry.ObjectHolder(Entangled.MODID + ":item")
    public static Item item;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){
        if(Loader.isModLoaded("theoneprobe"))
            TheOneProbePlugin.interModEnqueue();
    }

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

    public static final Set<String> RENDER_BLACKLISTED_MODS = Sets.newHashSet("fluidtank");
    public static final Set<ResourceLocation> RENDER_BLACKLISTED_BLOCKS = Sets.newHashSet();
    public static final Set<ResourceLocation> RENDER_BLACKLISTED_TILE_ENTITIES = Sets.newHashSet();

}
