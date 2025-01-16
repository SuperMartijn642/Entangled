package com.supermartijn642.entangled.extensions;

import net.minecraft.resources.ResourceLocation;

/**
 * Created 16/01/2025 by SuperMartijn642
 */
public interface BlockCapabilityExtension {

    void entangled_storeIdentifier(ResourceLocation identifier);

    ResourceLocation entangled_getIdentifier();
}
