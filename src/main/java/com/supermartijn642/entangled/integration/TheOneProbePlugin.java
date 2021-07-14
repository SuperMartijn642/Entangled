package com.supermartijn642.entangled.integration;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.entangled.EntangledBlockTile;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Function;

/**
 * Created 7/13/2021 by SuperMartijn642
 */
public class TheOneProbePlugin implements IProbeInfoProvider, Function<ITheOneProbe, Void> {

    @Override
    public Void apply(ITheOneProbe theOneProbe){
        theOneProbe.registerProvider(this);
        return null;
    }

    @Override
    public String getID(){
        return "entangled";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState state, IProbeHitData probeHitData){
        TileEntity tile = world.getTileEntity(probeHitData.getPos());
        if(tile instanceof EntangledBlockTile){
            if(((EntangledBlockTile)tile).isBound()){
                BlockState boundBlockState = ((EntangledBlockTile)tile).getBoundBlockState();
                String boundBlock = TextStyleClass.HIGHLIGHTED + (boundBlockState == null ? "Block" : TextComponents.blockState(boundBlockState).format()) + TextStyleClass.INFO;
                BlockPos boundPos = ((EntangledBlockTile)tile).getBoundBlockPos();
                String x = TextStyleClass.HIGHLIGHTED.toString() + boundPos.getX() + TextStyleClass.INFO;
                String y = TextStyleClass.HIGHLIGHTED.toString() + boundPos.getY() + TextStyleClass.INFO;
                String z = TextStyleClass.HIGHLIGHTED.toString() + boundPos.getZ() + TextStyleClass.INFO;
                if(((EntangledBlockTile)tile).getBoundDimension() == world.func_234923_W_())
                    probeInfo.vertical().text(TextComponents.translation("entangled.waila.bound_same_dimension", boundBlock, x, y, z).get());
                else{
                    String dimension = TextStyleClass.HIGHLIGHTED + TextComponents.dimension(((EntangledBlockTile)tile).getBoundDimension()).format() + TextStyleClass.INFO;
                    probeInfo.text(TextComponents.translation("entangled.waila.bound_other_dimension", boundBlock, x, y, z, dimension).format());
                }
            }else
                probeInfo.text(TextComponents.translation("entangled.waila.unbound").get());
        }
    }
}