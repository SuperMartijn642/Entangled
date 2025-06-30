package com.supermartijn642.entangled;

import com.supermartijn642.entangled.extensions.BlockCapabilityExtension;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Created 21/03/2022 by SuperMartijn642
 */
public class EntangledBlockApiProviders {

    public static void registerApiProviders(RegisterCapabilitiesEvent event){
        // TODO use #getAllProxyable once mods start updating to mark their capabilities as such
        for(BlockCapability<?,?> capability : BlockCapability.getAll()){
            //noinspection DataFlowIssue
            if(!((BlockCapabilityExtension)(Object)capability).entangled_getProxyableState().isFalse())
                registerApiProvider(event, capability);
        }
    }

    private static <A, C> void registerApiProvider(RegisterCapabilitiesEvent event, BlockCapability<A,C> api){
        event.registerBlockEntity(api, Entangled.tile, (entity, context) -> entity.getCapability(api, context));
    }
}
