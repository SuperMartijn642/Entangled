package com.supermartijn642.entangled;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;
import team.reborn.energy.api.EnergyStorage;

import java.util.function.Supplier;

/**
 * Created 21/03/2022 by SuperMartijn642
 */
public class EntangledBlockApiProviders {

    public static void register(){
        tryRegisterApiProvider("fabric-transfer-api-v1", () -> () -> ItemStorage.SIDED);
        tryRegisterApiProvider("fabric-transfer-api-v1", () -> () -> FluidStorage.SIDED);
        tryRegisterApiProvider("team_reborn_energy", () -> () -> EnergyStorage.SIDED);
    }

    private static void tryRegisterApiProvider(String modid, Supplier<Supplier<BlockApiLookup<?,?>>> apiLookup){
        if(FabricLoader.getInstance().isModLoaded(modid))
            registerApiProvider(apiLookup.get().get());
    }

    private static <A, C> void registerApiProvider(BlockApiLookup<A,C> apiLookup){
        apiLookup.registerForBlockEntity(
            (entity, context) -> entity.getCapability(apiLookup, context),
            Entangled.tile
        );
    }
}
