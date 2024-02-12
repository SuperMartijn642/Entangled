package com.supermartijn642.entangled.integration;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.entangled.EntangledBlock;
import com.supermartijn642.entangled.EntangledBlockEntity;
import mcp.mobius.waila.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
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
        if(tile instanceof EntangledBlockEntity){
            if(((EntangledBlockEntity)tile).isBound()){
                IBlockState boundBlockState = ((EntangledBlockEntity)tile).getBoundBlockState();
                ITextComponent boundBlock = (boundBlockState == null ? TextComponents.string("Block") : TextComponents.string(boundBlockState.getBlock().getLocalizedName())).color(TextFormatting.GOLD).get();
                BlockPos boundPos = ((EntangledBlockEntity)tile).getBoundBlockPos();
                ITextComponent x = TextComponents.string(Integer.toString(boundPos.getX())).color(TextFormatting.GOLD).get();
                ITextComponent y = TextComponents.string(Integer.toString(boundPos.getY())).color(TextFormatting.GOLD).get();
                ITextComponent z = TextComponents.string(Integer.toString(boundPos.getZ())).color(TextFormatting.GOLD).get();
                if(((EntangledBlockEntity)tile).getBoundDimensionIdentifier() == accessor.getWorld().provider.getDimension())
                    tooltip.add(TextComponents.translation("entangled.waila.bound_same_dimension", boundBlock, x, y, z).format());
                else{
                    ITextComponent dimension = TextComponents.dimension(DimensionType.getById(((EntangledBlockEntity)tile).getBoundDimensionIdentifier())).color(TextFormatting.GOLD).get();
                    tooltip.add(TextComponents.translation("entangled.waila.bound_other_dimension", boundBlock, x, y, z, dimension).format());
                }
                if(!((EntangledBlockEntity)tile).isBoundAndValid())
                    tooltip.add(TextComponents.translation("entangled.waila.invalid_block", boundBlock).color(TextFormatting.RED).format());
            }else
                tooltip.add(TextComponents.translation("entangled.waila.unbound").format());
        }
        return tooltip;
    }
}
