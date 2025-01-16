package com.supermartijn642.entangled.mixin.neoforge;

import com.supermartijn642.entangled.extensions.RegisterCapabilitiesEventExtension;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * Mixin to track which block entity capabilities there are.
 * This allows Entangled to automatically register capability implementations which pass on the capability for the entangled block.
 * <p>
 * Created 16/01/2025 by SuperMartijn642
 */
@Mixin(RegisterCapabilitiesEvent.class)
public class RegisterCapabilitiesEventMixin implements RegisterCapabilitiesEventExtension {

    @Unique
    private Set<BlockCapability<?,?>> blockEntityCapabilities;

    @Inject(
        method = "registerBlockEntity",
        at = @At("TAIL")
    )
    private void registerBlockEntity(BlockCapability<?,?> capability, BlockEntityType<?> blockEntityType, ICapabilityProvider<?,?,?> provider, CallbackInfo ci) {
        if(this.blockEntityCapabilities == null)
            this.blockEntityCapabilities = new HashSet<>();
        this.blockEntityCapabilities.add(capability);
    }

    @Override
    public Set<BlockCapability<?,?>> entangled_getRegisteredCapabilities(){
        return this.blockEntityCapabilities;
    }
}
