package com.supermartijn642.entangled.generators;

import com.supermartijn642.core.generator.RecipeGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.entangled.Entangled;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.world.item.Items;

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
            .input('A', Items.ENDER_PEARL)
            .input('B', Items.OBSIDIAN)
            .input('C', Items.CHEST)
            .unlockedBy(Items.ENDER_PEARL);
        this.shapeless("entangled_clear_nbt", Entangled.block)
            .input(Entangled.block)
            .noAdvancement();

        // Entangled Binder
        this.shaped(Entangled.item)
            .pattern(" AB")
            .pattern(" CA")
            .pattern("C  ")
            .input('A', Items.ENDER_PEARL)
            .input('B', ConventionalItemTags.DIAMONDS)
            .input('C', Items.OBSIDIAN)
            .unlockedBy(Items.ENDER_PEARL);
    }
}
