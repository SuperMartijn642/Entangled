package com.supermartijn642.entangled;

import com.supermartijn642.entangled.extensions.BlockCapabilityExtension;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.Set;

/**
 * Created 21/03/2022 by SuperMartijn642
 */
public class EntangledBlockApiProviders {

    public static void registerApiProviders(RegisterCapabilitiesEvent event, Set<BlockCapability<?,?>> capabilities){
        for(BlockCapability<?,?> capability : capabilities){
            //noinspection DataFlowIssue
            if(isApiAllowed(((BlockCapabilityExtension)(Object)capability).entangled_getIdentifier()))
                registerApiProvider(event, capability);
        }
    }

    private static <A, C> void registerApiProvider(RegisterCapabilitiesEvent event, BlockCapability<A,C> api){
        event.registerBlockEntity(api, Entangled.tile, (entity, context) -> entity.getCapability(api, context));
    }

    private static boolean isApiAllowed(ResourceLocation identifier){
        if(identifier == null)
            return false;
        return !identifier.getNamespace().equals("ae2") && !identifier.getNamespace().equals("refinedstorage");
    }
}
