package com.supermartijn642.entangled;

import com.google.common.collect.Sets;
import com.supermartijn642.entangled.integration.TheOneProbePlugin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegisterEvent;

import java.util.Objects;
import java.util.Set;

@Mod("entangled")
public class Entangled {

    @ObjectHolder(value = "entangled:block", registryName = "minecraft:block")
    public static EntangledBlock block;
    @ObjectHolder(value = "entangled:tile", registryName = "minecraft:block_entity_type")
    public static BlockEntityType<EntangledBlockTile> tile;
    @ObjectHolder(value = "entangled:item", registryName = "minecraft:item")
    public static EntangledBinder item;

    public Entangled(){
        FMLJavaModLoadingContext.get().getModEventBus().addListener(TheOneProbePlugin::interModEnqueue);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

        @SubscribeEvent
        public static void onRegisterEvent(RegisterEvent e){
            if(e.getRegistryKey().equals(ForgeRegistries.Keys.BLOCKS))
                onBlockRegistry(Objects.requireNonNull(e.getForgeRegistry()));
            else if(e.getRegistryKey().equals(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES))
                onTileRegistry(Objects.requireNonNull(e.getForgeRegistry()));
            else if(e.getRegistryKey().equals(ForgeRegistries.Keys.ITEMS))
                onItemRegistry(Objects.requireNonNull(e.getForgeRegistry()));
        }

        public static void onBlockRegistry(IForgeRegistry<Block> registry){
            registry.register("block", new EntangledBlock());
        }

        public static void onTileRegistry(IForgeRegistry<BlockEntityType<?>> registry){
            registry.register("tile", BlockEntityType.Builder.of(EntangledBlockTile::new, block).build(null));
        }

        public static void onItemRegistry(IForgeRegistry<Item> registry){
            registry.register("block", new EntangledBlockItem(block, new Item.Properties().tab(CreativeModeTab.TAB_SEARCH)));
            registry.register("item", new EntangledBinder());
        }
    }

    public static final Set<String> RENDER_BLACKLISTED_MODS = Sets.newHashSet();
    public static final Set<ResourceLocation> RENDER_BLACKLISTED_BLOCKS = Sets.newHashSet();
    public static final Set<ResourceLocation> RENDER_BLACKLISTED_TILE_ENTITIES = Sets.newHashSet(new ResourceLocation("tconstruct", "smeltery"), new ResourceLocation("tconstruct", "foundry"));

}
