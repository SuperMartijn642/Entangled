package com.supermartijn642.entangled.generators;

import com.supermartijn642.core.generator.RecipeGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.entangled.Entangled;
import net.neoforged.neoforge.common.Tags;

/**
 * Created 01/09/2022 by SuperMartijn642
 */
public class EntangledRecipeGenerator extends RecipeGenerator {

    public EntangledRecipeGenerator(ResourceCache cache){
        super("entangled", cache);
    }

    @Override
    public void generate(){
        // Entangled block
        this.shaped(Entangled.block)
            .pattern("ABA")
            .pattern("BCB")
            .pattern("ABA")
            .input('A', Tags.Items.ENDER_PEARLS)
            .input('B', Tags.Items.OBSIDIANS)
            .input('C', Tags.Items.CHESTS_WOODEN)
            .unlockedBy(Tags.Items.ENDER_PEARLS);
        this.shapeless("entangled_clear_nbt", Entangled.block)
            .input(Entangled.block)
            .noAdvancement();

        // Entangled Binder
        this.shaped(Entangled.item)
            .pattern(" AB")
            .pattern(" CA")
            .pattern("C  ")
            .input('A', Tags.Items.ENDER_PEARLS)
            .input('B', Tags.Items.GEMS_DIAMOND)
            .input('C', Tags.Items.OBSIDIANS)
            .unlockedBy(Tags.Items.ENDER_PEARLS);
    }
}
