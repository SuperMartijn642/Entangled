package com.supermartijn642.entangled.generators;

import com.supermartijn642.core.generator.LanguageGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.entangled.Entangled;

/**
 * Created 01/09/2022 by SuperMartijn642
 */
public class EntangledLanguageGenerator extends LanguageGenerator {

    public EntangledLanguageGenerator(ResourceCache cache){
        super("entangled", cache, "en_us");
    }

    @Override
    public void generate(){
        this.block(Entangled.block, "Entangled Block");
        this.item(Entangled.item, "Entangled Binder");

        // Interactions
        this.translation("entangled.entangled_block.info.ranged_same_dimension", "Can be bound to blocks in the same dimension up to %d blocks away");
        this.translation("entangled.entangled_block.info.ranged_same_dimension", "Can be bound to blocks in the same dimension up to %d blocks away");
        this.translation("entangled.entangled_block.info.infinite_same_dimension", "Can be bound to blocks in the same dimension");
        this.translation("entangled.entangled_block.info.ranged_other_dimension", "Can be bound to blocks up to %d blocks away or to blocks in other dimensions");
        this.translation("entangled.entangled_block.info.infinite_other_dimension", "Can be bound to other blocks");
        this.translation("entangled.entangled_block.info.bound", "Bound to %1$s in the %5$s at (%2$d, %3$d, %4$d)");
        this.translation("entangled.entangled_block.unbind", "Block unbound!");
        this.translation("entangled.entangled_block.no_selection", "No block selected!");
        this.translation("entangled.entangled_block.self", "Can't bind a block to itself!");
        this.translation("entangled.entangled_block.bind", "Block bound!");
        this.translation("entangled.entangled_block.wrong_dimension", "The targeted block must be in the same dimension!");
        this.translation("entangled.entangled_block.too_far", "The targeted block is too far away!");
        this.translation("entangled.entangled_block.not_in_whitelist", "The targeted block is not in the whitelist!");
        this.translation("entangled.entangled_block.not_in_blacklist", "The targeted block is not in the blacklist!");
        this.translation("entangled.entangled_binder.info", "Can bind entangled blocks to other blocks");
        this.translation("entangled.entangled_binder.info.target.unknown", "Linked to a block in the %4$s at (%1$d, %2$d, %3$d)");
        this.translation("entangled.entangled_binder.info.target.known", "Linked to %1$s in the %5$s at (%2$d, %3$d, %4$d)");
        this.translation("entangled.entangled_binder.select", "Block selected!");
        this.translation("entangled.entangled_binder.clear", "Connection cleared!");
        this.translation("entangled.entangled_binder.unknown_dimension", "Binder is bound to unknown dimension '%s'!");

        // Jade & The One Probe
        this.translation("entangled.waila.bound_same_dimension", "Bound to %1$s at (%2$d, %3$d, %4$d)");
        this.translation("entangled.waila.bound_other_dimension", "Bound to %1$s in the %5$s at (%2$d, %3$d, %4$d)");
        this.translation("entangled.waila.unbound", "Unbound");
        this.translation("entangled.waila.invalid_block", "Invalid block '%s'!");
        this.translation("config.jade.plugin_entangled.entangled_block_component", "Entangled Block");
    }
}
