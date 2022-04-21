package com.supermartijn642.entangled;

import com.google.common.collect.Sets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

/**
 * Created 18/03/2022 by SuperMartijn642
 */
public class Entangled implements ModInitializer {

    public static EntangledBlock block;
    public static BlockEntityType<EntangledBlockTile> tile;
    public static EntangledBinder item;

    @Override
    public void onInitialize(){
        block = Registry.register(Registry.BLOCK, new ResourceLocation("entangled", "block"), new EntangledBlock());
        Registry.register(Registry.ITEM, new ResourceLocation("entangled", "block"), new BlockItem(block, new Item.Properties().tab(CreativeModeTab.TAB_SEARCH)));
        tile = Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation("entangled", "tile"), FabricBlockEntityTypeBuilder.create(EntangledBlockTile::new, block).build(null));
        item = Registry.register(Registry.ITEM, new ResourceLocation("entangled", "item"), new EntangledBinder());

        EntangledBlockApiProviders.register();
    }

    public static final Set<String> RENDER_BLACKLISTED_MODS = Sets.newHashSet();
    public static final Set<ResourceLocation> RENDER_BLACKLISTED_BLOCKS = Sets.newHashSet();
    public static final Set<ResourceLocation> RENDER_BLACKLISTED_TILE_ENTITIES = Sets.newHashSet();

}
