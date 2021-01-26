package com.supermartijn642.entangled.integration;

import com.supermartijn642.entangled.ClientProxy;
import com.supermartijn642.entangled.EntangledBlock;
import com.supermartijn642.entangled.EntangledBlockTile;
import mcp.mobius.waila.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DimensionType;

import java.util.List;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
@WailaPlugin("entangled")
public class EntangleWailaPlugin implements IWailaDataProvider, IWailaPlugin {
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
                String boundBlock = boundBlockState == null ? "Block" : ClientProxy.translate(boundBlockState.getBlock().getLocalizedName());
                BlockPos boundPos = ((EntangledBlockTile)tile).getBoundBlockPos();
                if(((EntangledBlockTile)tile).getBoundDimension() == accessor.getWorld().provider.getDimensionType().getId())
                    tooltip.add(new TextComponentTranslation("entangled.waila.bound_same_dimension", boundBlock, boundPos.getX(), boundPos.getY(), boundPos.getZ()).getFormattedText());
                else{
                    String dimension = DimensionType.getById(((EntangledBlockTile)tile).getBoundDimension()).getName();
                    dimension = dimension.substring(dimension.lastIndexOf(":") + 1);
                    dimension = Character.toUpperCase(dimension.charAt(0)) + dimension.substring(1);
                    tooltip.add(new TextComponentTranslation("entangled.waila.bound_other_dimension", boundBlock, boundPos.getX(), boundPos.getY(), boundPos.getZ(), dimension).getFormattedText());
                }
            }else
                tooltip.add(new TextComponentTranslation("entangled.waila.unbound").getFormattedText());
        }
        return tooltip;
    }
}
