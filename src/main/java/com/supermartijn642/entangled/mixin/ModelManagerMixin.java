package com.supermartijn642.entangled.mixin;

import com.supermartijn642.entangled.EntangledClient;
import net.minecraft.client.resources.model.ModelManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 21/03/2022 by SuperMartijn642
 */
@Mixin(ModelManager.class)
public class ModelManagerMixin {

    @Inject(method = "apply",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/BlockModelShaper;rebuildCache()V",
            shift = At.Shift.BEFORE
        )
    )
    public void apply(CallbackInfo ci){
        ModelManager manager = (ModelManager)(Object)this;
        EntangledClient.onModelBake(manager.bakedRegistry);
    }
}
