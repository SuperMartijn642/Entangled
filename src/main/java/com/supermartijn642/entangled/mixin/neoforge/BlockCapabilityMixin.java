package com.supermartijn642.entangled.mixin.neoforge;

import com.supermartijn642.entangled.extensions.BlockCapabilityExtension;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Store the identifier a block capability is registered with.
 * <p>
 * Created 16/01/2025 by SuperMartijn642
 */
@Mixin(BlockCapability.class)
public class BlockCapabilityMixin implements BlockCapabilityExtension {

    @Unique
    private ResourceLocation identifier;

    @Override
    public void entangled_storeIdentifier(ResourceLocation identifier){
        this.identifier = identifier;
    }

    @Override
    public ResourceLocation entangled_getIdentifier(){
        return this.identifier;
    }

    @Inject(
        method = "create",
        at = @At("RETURN")
    )
    private static void create(ResourceLocation name, Class<?> typeClass, Class<?> contextClass, CallbackInfoReturnable<BlockCapability<?,?>> ci){
        // Store the name on the block capability
        //noinspection DataFlowIssue
        ((BlockCapabilityExtension)(Object)ci.getReturnValue()).entangled_storeIdentifier(name);
    }
}
