package com.supermartijn642.entangled.generators;

import com.supermartijn642.core.generator.BlockStateGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.entangled.Entangled;
import com.supermartijn642.entangled.EntangledBlock;

/**
 * Created 01/09/2022 by SuperMartijn642
 */
public class EntangledBlockStateGenerator extends BlockStateGenerator {

    public EntangledBlockStateGenerator(ResourceCache cache){
        super("entangled", cache);
    }

    @Override
    public void generate(){
        this.blockState(Entangled.block).variantsForProperty(EntangledBlock.STATE_PROPERTY, (state, variant) -> {
            EntangledBlock.State property = state.get(EntangledBlock.STATE_PROPERTY);
            if(property == EntangledBlock.State.UNBOUND)
                variant.model("entangled", "block/unbound");
            if(property == EntangledBlock.State.BOUND_VALID)
                variant.model("entangled", "block/bound");
            if(property == EntangledBlock.State.BOUND_INVALID)
                variant.model("entangled", "block/bound_invalid");
        });
    }
}
