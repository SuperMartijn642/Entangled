package com.supermartijn642.entangled.generators;

import com.supermartijn642.core.generator.ModelGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.entangled.Entangled;
import net.minecraft.resources.ResourceLocation;

/**
 * Created 02/09/2022 by SuperMartijn642
 */
public class EntangledModelGenerator extends ModelGenerator {

    public EntangledModelGenerator(ResourceCache cache){
        super("entangled", cache);
    }

    @Override
    public void generate(){
        // Entangled Block
        this.cubeAll("block/off", new ResourceLocation("entangled", "blocks/off"));
        this.model("item/block").parent("block/off");

        // Entangled Binder
        this.itemHandheld(Entangled.item, new ResourceLocation("entangled", "items/item"));
    }
}
