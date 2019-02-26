package supermartijn642.entangled;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Lookup {

    @GameRegistry.ObjectHolder(Entangled.MODID + ":block")
    public static EntangledBlock block;

    @GameRegistry.ObjectHolder(Entangled.MODID + ":item")
    public static Item item;

}
