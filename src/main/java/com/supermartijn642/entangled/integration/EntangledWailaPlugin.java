package com.supermartijn642.entangled.integration;

import com.supermartijn642.entangled.ClientProxy;
import com.supermartijn642.entangled.EntangledBlock;
import com.supermartijn642.entangled.EntangledBlockTile;
import mcp.mobius.waila.api.*;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
@WailaPlugin("entangled")
public class EntangledWailaPlugin implements IComponentProvider, IWailaPlugin {

    @Override
    public void register(IRegistrar registrar){
        registrar.registerComponentProvider(this, TooltipPosition.BODY, EntangledBlock.class);
    }

    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config){
        TileEntity tile = accessor.getTileEntity();
        if(tile instanceof EntangledBlockTile){
            if(((EntangledBlockTile)tile).isBound()){
                BlockState boundBlockState = ((EntangledBlockTile)tile).getBoundBlockState();
                String boundBlock = boundBlockState == null ? "Block" : ClientProxy.translate(boundBlockState.getBlock().getTranslationKey());
                BlockPos boundPos = ((EntangledBlockTile)tile).getBoundBlockPos();
                if(((EntangledBlockTile)tile).getBoundDimension() == accessor.getWorld().getDimensionKey())
                    tooltip.add(new TranslationTextComponent("entangled.waila.bound_same_dimension", boundBlock, boundPos.getX(), boundPos.getY(), boundPos.getZ()));
                else{
                    String dimension = ((EntangledBlockTile)tile).getBoundDimension().getLocation().getPath();
                    dimension = Character.toUpperCase(dimension.charAt(0)) + dimension.substring(1);
                    tooltip.add(new TranslationTextComponent("entangled.waila.bound_other_dimension", boundBlock, boundPos.getX(), boundPos.getY(), boundPos.getZ(), dimension));
                }
            }else
                tooltip.add(new TranslationTextComponent("entangled.waila.unbound"));
        }
    }
}
