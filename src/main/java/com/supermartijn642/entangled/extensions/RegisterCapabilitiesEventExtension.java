package com.supermartijn642.entangled.extensions;

import net.neoforged.neoforge.capabilities.BlockCapability;

import java.util.Set;

/**
 * Created 16/01/2025 by SuperMartijn642
 */
public interface RegisterCapabilitiesEventExtension {

    Set<BlockCapability<?,?>> entangled_getRegisteredCapabilities();
}
