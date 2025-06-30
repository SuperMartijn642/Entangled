package com.supermartijn642.entangled.mixin.neoforge;

import com.supermartijn642.entangled.extensions.BlockCapabilityExtension;
import net.minecraft.util.TriState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Store the identifier a block capability is registered with.
 * <p>
 * Created 16/01/2025 by SuperMartijn642
 */
@Mixin(BlockCapability.class)
public class BlockCapabilityMixin implements BlockCapabilityExtension {

    @Shadow
    private TriState proxyable;

    @Override
    public TriState entangled_getProxyableState(){
        return this.proxyable;
    }
}
