package com.supermartijn642.entangled;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> e){
        ClientRegistry.bindTileEntitySpecialRenderer(EntangledBlockTile.class, new EntangledBlockTileRenderer());
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e){
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Entangled.block), 0, new ModelResourceLocation(Entangled.block.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Entangled.item, 0, new ModelResourceLocation(Entangled.item.getRegistryName(), "inventory"));
    }

}
