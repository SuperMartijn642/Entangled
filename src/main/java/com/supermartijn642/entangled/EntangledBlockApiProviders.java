package com.supermartijn642.entangled;

import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Created 21/03/2022 by SuperMartijn642
 */
public class EntangledBlockApiProviders {

    public static void registerApiProviders(RegisterCapabilitiesEvent event){
        for(BlockCapability<?,?> capability : BlockCapability.getAll()){
            if(capability.isProxyable())
                registerApiProvider(event, capability);
        }
    }

    private static <A, C> void registerApiProvider(RegisterCapabilitiesEvent event, BlockCapability<A,C> api){
        event.registerBlockEntity(api, Entangled.tile, (entity, context) -> entity.getCapability(api, context));
    }
}
