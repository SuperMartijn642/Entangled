package com.supermartijn642.entangled.generators;

import com.supermartijn642.core.generator.BlockStateGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.entangled.Entangled;
import com.supermartijn642.entangled.EntangledBlock;
import net.minecraft.resources.ResourceLocation;

/**
 * Created 01/09/2022 by SuperMartijn642
 */
public class EntangledBlockStateGenerator extends BlockStateGenerator {

    public EntangledBlockStateGenerator(ResourceCache cache){
        super("entangled", cache);
    }

    @Override
    public void generate(){
        this.blockState(Entangled.block).variantsForProperty(EntangledBlock.ON, (state, builder) -> builder.model(new ResourceLocation("entangled", "block/" + (state.get(EntangledBlock.ON) ? "on" : "off"))));
    }
}
