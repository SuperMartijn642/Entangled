package com.supermartijn642.entangled;

import com.google.common.collect.Sets;
import com.supermartijn642.core.item.BaseBlockItem;
import com.supermartijn642.core.item.ItemGroup;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

/**
 * Created 18/03/2022 by SuperMartijn642
 */
public class Entangled implements ModInitializer {

    @RegistryEntryAcceptor(namespace = "entangled", identifier = "block", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static EntangledBlock block;
    @RegistryEntryAcceptor(namespace = "entangled", identifier = "tile", registry = RegistryEntryAcceptor.Registry.BLOCK_ENTITY_TYPES)
    public static BlockEntityType<EntangledBlockEntity> tile;
    @RegistryEntryAcceptor(namespace = "entangled", identifier = "item", registry = RegistryEntryAcceptor.Registry.ITEMS)
    public static EntangledBinderItem item;

    @Override
    public void onInitialize(){
        register();
    }

    private static void register(){
        RegistrationHandler handler = RegistrationHandler.get("entangled");

        // Entangled block
        handler.registerBlock("block", EntangledBlock::new);
        handler.registerItem("block", () -> new BaseBlockItem(block, ItemProperties.create().group(ItemGroup.getDecoration())));
        // Entangled block entity type
        handler.registerBlockEntityType("tile", () -> FabricBlockEntityTypeBuilder.create(EntangledBlockEntity::new, block).build(null));
        // Entangled block api providers
        handler.registerBlockEntityTypeCallback(helper -> EntangledBlockApiProviders.register());
        // Entangled binder
        handler.registerItem("item", EntangledBinderItem::new);
    }

    public static final Set<String> RENDER_BLACKLISTED_MODS = Sets.newHashSet();
    public static final Set<ResourceLocation> RENDER_BLACKLISTED_BLOCKS = Sets.newHashSet();
    public static final Set<ResourceLocation> RENDER_BLACKLISTED_TILE_ENTITIES = Sets.newHashSet();

}
