package com.supermartijn642.entangled.mixin.neoforge;

import com.llamalad7.mixinextras.sugar.Local;
import com.supermartijn642.entangled.EntangledBlockApiProviders;
import com.supermartijn642.entangled.extensions.RegisterCapabilitiesEventExtension;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.CapabilityHooks;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/**
 * Automatically register capability implementations for the entangled block.
 * <p>
 * Created 16/01/2025 by SuperMartijn642
 */
@SuppressWarnings("UnstableApiUsage")
@Mixin(CapabilityHooks.class)
public class CapabilityHooksMixin {

    @Inject(
        method = "init",
        at = @At(
            value = "INVOKE",
            target = "Lnet/neoforged/fml/ModLoader;postEventWrapContainerInModOrder(Lnet/neoforged/bus/api/Event;)V",
            shift = At.Shift.AFTER
        )
    )
    private static void init(CallbackInfo ci, @Local RegisterCapabilitiesEvent event){
        try{
            // Get all registered capabilities
            Set<BlockCapability<?,?>> capabilities = ((RegisterCapabilitiesEventExtension)event).entangled_getRegisteredCapabilities();
            // Register a provider for all capabilities
            EntangledBlockApiProviders.registerApiProviders(event, capabilities);
        }catch(Exception e){
            throw new RuntimeException("Entangled failed to register capabilities for the entangled block!", e);
        }
    }
}
