package com.supermartijn642.entangled.generators;

import com.supermartijn642.core.generator.LootTableGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.entangled.Entangled;

/**
 * Created 01/09/2022 by SuperMartijn642
 */
public class EntangledLootTableGenerator extends LootTableGenerator {

    public EntangledLootTableGenerator(ResourceCache cache){
        super("entangled", cache);
    }

    @Override
    public void generate(){
        this.dropSelf(Entangled.block);
    }
}
