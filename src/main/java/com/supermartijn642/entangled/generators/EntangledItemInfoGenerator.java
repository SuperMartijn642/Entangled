package com.supermartijn642.entangled.generators;

import com.supermartijn642.core.generator.ItemInfoGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.entangled.Entangled;
import com.supermartijn642.entangled.EntangledBlockItemModel;

/**
 * Created 23/12/2024 by SuperMartijn642
 */
public class EntangledItemInfoGenerator extends ItemInfoGenerator {
    public EntangledItemInfoGenerator(ResourceCache cache){
        super("entangled", cache);
    }

    @Override
    public void generate(){
        this.info(Entangled.block).model(new EntangledBlockItemModel());
        this.simpleInfo(Entangled.item, "item/item");
    }
}
