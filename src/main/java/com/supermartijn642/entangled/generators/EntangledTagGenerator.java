package com.supermartijn642.entangled.generators;

import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.TagGenerator;
import com.supermartijn642.entangled.Entangled;
import net.minecraft.tags.BlockTags;

/**
 * Created 02/09/2022 by SuperMartijn642
 */
public class EntangledTagGenerator extends TagGenerator {

    public EntangledTagGenerator(ResourceCache cache){
        super("entangled", cache);
    }

    @Override
    public void generate(){
        this.blockTag(BlockTags.MINEABLE_WITH_PICKAXE).add(Entangled.block);
    }
}
