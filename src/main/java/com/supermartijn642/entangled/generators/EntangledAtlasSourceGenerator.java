package com.supermartijn642.entangled.generators;

import com.supermartijn642.core.generator.AtlasSourceGenerator;
import com.supermartijn642.core.generator.ResourceCache;

/**
 * Created 12/07/2024 by SuperMartijn642
 */
public class EntangledAtlasSourceGenerator extends AtlasSourceGenerator {
    public EntangledAtlasSourceGenerator(ResourceCache cache){
        super("entangled", cache);
    }

    @Override
    public void generate(){
        this.blockAtlas().texturesFromModel("block/bound_invalid");
    }
}
