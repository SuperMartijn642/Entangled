package com.supermartijn642.entangled;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.core.item.BaseBlockItem;
import com.supermartijn642.core.item.CreativeItemGroup;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import com.supermartijn642.entangled.generators.*;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

/**
 * Created 18/03/2022 by SuperMartijn642
 */
public class Entangled implements ModInitializer {

    public static final Logger LOGGER = CommonUtils.getLogger("entangled");

    @RegistryEntryAcceptor(namespace = "entangled", identifier = "block", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static EntangledBlock block;
    @RegistryEntryAcceptor(namespace = "entangled", identifier = "tile", registry = RegistryEntryAcceptor.Registry.BLOCK_ENTITY_TYPES)
    public static BaseBlockEntityType<EntangledBlockEntity> tile;
    @RegistryEntryAcceptor(namespace = "entangled", identifier = "item", registry = RegistryEntryAcceptor.Registry.ITEMS)
    public static EntangledBinderItem item;

    @Override
    public void onInitialize(){
        register();
        registerGenerators();
        EntangledConfig.init();
    }

    private static void register(){
        RegistrationHandler handler = RegistrationHandler.get("entangled");

        // Entangled block
        handler.registerBlock("block", EntangledBlock::new);
        handler.registerItem("block", () -> new BaseBlockItem(block, ItemProperties.create().group(CreativeItemGroup.getFunctionalBlocks())));
        // Entangled block entity type
        handler.registerBlockEntityType("tile", () -> BaseBlockEntityType.create(EntangledBlockEntity::new, block));
        // Entangled block api providers
        handler.registerBlockEntityTypeCallback(helper -> EntangledBlockApiProviders.register());
        // Entangled binder
        handler.registerItem("item", EntangledBinderItem::new);
        // Entangled binder target data
        handler.registerDataComponentType("binder_target", EntangledBinderItem.BINDER_TARGET);
    }

    private static void registerGenerators(){
        GeneratorRegistrationHandler handler = GeneratorRegistrationHandler.get("entangled");

        // Add all the generators
        handler.addGenerator(EntangledBlockStateGenerator::new);
        handler.addGenerator(EntangledModelGenerator::new);
        handler.addGenerator(EntangledLanguageGenerator::new);
        handler.addGenerator(EntangledLootTableGenerator::new);
        handler.addGenerator(EntangledRecipeGenerator::new);
        handler.addGenerator(EntangledTagGenerator::new);
        handler.addGenerator(EntangledAtlasSourceGenerator::new);
    }
}
