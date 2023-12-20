package com.supermartijn642.entangled;

import com.supermartijn642.core.CommonUtils;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created 21/03/2022 by SuperMartijn642
 */
public class EntangledBlockApiProviders {

    public static void register(){
        tryRegisterApiProvider("neoforge", () -> () -> Capabilities.ItemHandler.BLOCK);
        tryRegisterApiProvider("neoforge", () -> () -> Capabilities.FluidHandler.BLOCK);
        tryRegisterApiProvider("neoforge", () -> () -> Capabilities.EnergyStorage.BLOCK);
    }

    private static void tryRegisterApiProvider(String modid, Supplier<Supplier<BlockCapability<?,?>>> api){
        if(CommonUtils.isModLoaded(modid))
            registerApiProvider(api.get().get());
    }

    private static <A, C> void registerApiProvider(BlockCapability<A,C> api){
        ModLoadingContext.get().getActiveContainer().getEventBus().addListener(
            (Consumer<RegisterCapabilitiesEvent>)
                event -> event.registerBlockEntity(api, Entangled.tile, (entity, context) -> entity.getCapability(api, context))
        );
    }
}
