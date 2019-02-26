package supermartijn642.entangled;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e){
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Lookup.block), 0, new ModelResourceLocation(Lookup.block.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Lookup.item, 0, new ModelResourceLocation(Lookup.item.getRegistryName(), "inventory"));
    }

}
