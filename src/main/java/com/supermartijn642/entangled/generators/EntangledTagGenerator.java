package com.supermartijn642.entangled.generators;

import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.TagGenerator;
import com.supermartijn642.entangled.Entangled;

/**
 * Created 02/09/2022 by SuperMartijn642
 */
public class EntangledTagGenerator extends TagGenerator {

    public EntangledTagGenerator(ResourceCache cache){
        super("entangled", cache);
    }

    @Override
    public void generate(){
        this.blockMineableWithPickaxe().add(Entangled.block);
    }
}
