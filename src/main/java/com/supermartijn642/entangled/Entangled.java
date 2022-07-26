package com.supermartijn642.entangled;

import com.google.common.collect.Sets;
import com.supermartijn642.core.item.BaseBlockItem;
import com.supermartijn642.core.item.ItemGroup;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import com.supermartijn642.entangled.integration.TheOneProbePlugin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Set;

@Mod("entangled")
public class Entangled {

    @RegistryEntryAcceptor(namespace = "entangled", identifier = "block", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static EntangledBlock block;
    @RegistryEntryAcceptor(namespace = "entangled", identifier = "tile", registry = RegistryEntryAcceptor.Registry.BLOCK_ENTITY_TYPES)
    public static BlockEntityType<EntangledBlockEntity> tile;
    @RegistryEntryAcceptor(namespace = "entangled", identifier = "item", registry = RegistryEntryAcceptor.Registry.ITEMS)
    public static EntangledBinderItem item;

    public Entangled(){
        FMLJavaModLoadingContext.get().getModEventBus().addListener(TheOneProbePlugin::interModEnqueue);

        register();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> EntangledClient::register);
    }

    private static void register(){
        RegistrationHandler handler = RegistrationHandler.get("entangled");

        // Entangled block
        handler.registerBlock("block", EntangledBlock::new);
        handler.registerItem("block", () -> new BaseBlockItem(block, ItemProperties.create().group(ItemGroup.getDecoration())));
        // Entangled block entity type
        handler.registerBlockEntityType("tile", () -> BlockEntityType.Builder.of(EntangledBlockEntity::new, block).build(null));
        // Entangled binder
        handler.registerItem("item", EntangledBinderItem::new);
    }

    public static final Set<String> RENDER_BLACKLISTED_MODS = Sets.newHashSet();
    public static final Set<ResourceLocation> RENDER_BLACKLISTED_BLOCKS = Sets.newHashSet();
    public static final Set<ResourceLocation> RENDER_BLACKLISTED_TILE_ENTITIES = Sets.newHashSet(new ResourceLocation("tconstruct", "smeltery"), new ResourceLocation("tconstruct", "foundry"));

}
