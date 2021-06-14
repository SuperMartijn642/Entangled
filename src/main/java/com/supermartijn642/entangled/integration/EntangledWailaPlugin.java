package com.supermartijn642.entangled.integration;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.entangled.EntangledBlock;
import com.supermartijn642.entangled.EntangledBlockTile;
import mcp.mobius.waila.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.DimensionType;

import java.util.List;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
@WailaPlugin("entangled")
public class EntangledWailaPlugin implements IWailaDataProvider, IWailaPlugin {

    @Override
    public void register(IWailaRegistrar registrar){
        registrar.registerBodyProvider(this, EntangledBlock.class);
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config){
        TileEntity tile = accessor.getTileEntity();
        if(tile instanceof EntangledBlockTile){
            if(((EntangledBlockTile)tile).isBound()){
                IBlockState boundBlockState = ((EntangledBlockTile)tile).getBoundBlockState();
                ITextComponent boundBlock = (boundBlockState == null ? TextComponents.string("Block") : TextComponents.blockState(boundBlockState)).get();
                BlockPos boundPos = ((EntangledBlockTile)tile).getBoundBlockPos();
                if(((EntangledBlockTile)tile).getBoundDimension() == accessor.getWorld().provider.getDimensionType().getId())
                    tooltip.add(TextComponents.translation("entangled.waila.bound_same_dimension", boundBlock, boundPos.getX(), boundPos.getY(), boundPos.getZ()).format());
                else{
                    ITextComponent dimension = TextComponents.dimension(DimensionType.getById(((EntangledBlockTile)tile).getBoundDimension())).get();
                    tooltip.add(TextComponents.translation("entangled.waila.bound_other_dimension", boundBlock, boundPos.getX(), boundPos.getY(), boundPos.getZ(), dimension).format());
                }
            }else
                tooltip.add(TextComponents.translation("entangled.waila.unbound").format());
        }
        return tooltip;
    }
}
