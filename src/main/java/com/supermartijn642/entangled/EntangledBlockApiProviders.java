package com.supermartijn642.entangled;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
            (entity, context) -> {
                if(entity.getLevel() == null || !entity.isBound() || entity.callDepth >= 10)
                    return null;

                // Check if the bound block's level is available
                if(entity.getLevel().isClientSide && entity.getLevel().dimension() != entity.getBoundDimensionIdentifier())
                    return null;

                Level level = entity.getLevel().isClientSide ?
                    entity.getLevel().dimension() == entity.getBoundDimensionIdentifier() ? entity.getLevel() : null :
                    entity.getLevel().getServer().getLevel(entity.getBoundDimensionIdentifier());
                if(level != null && level.hasChunkAt(entity.getBoundBlockPos())){
                    BlockPos boundPos = entity.getBoundBlockPos();
                    BlockState boundState = level.getBlockState(boundPos);
                    BlockEntity boundEntity = level.getBlockEntity(boundPos);
                    entity.callDepth++;
                    A apiObject = apiLookup.find(level, boundPos, boundState, boundEntity, context);
                    entity.callDepth--;
                    return apiObject;
                }else
                    entity.shouldUpdateOnceLoaded = true;
                return null;
            },
            Entangled.tile
        );
    }
}
