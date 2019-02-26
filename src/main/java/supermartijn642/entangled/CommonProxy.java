package supermartijn642.entangled;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber
public class CommonProxy {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> e){
        e.getRegistry().register(new EntangledBlock());
        GameRegistry.registerTileEntity(TEEntangledBlock.class, new ResourceLocation(Entangled.MODID, "teentangledblock"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> e){
        e.getRegistry().register(new ItemBlock(Lookup.block).setRegistryName(Lookup.block.getRegistryName()));
        e.getRegistry().register(new EntangledBinder());
    }

}
