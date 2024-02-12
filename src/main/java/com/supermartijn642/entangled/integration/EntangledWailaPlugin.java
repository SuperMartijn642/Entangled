package com.supermartijn642.entangled.integration;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.entangled.EntangledBlock;
import com.supermartijn642.entangled.EntangledBlockEntity;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
@WailaPlugin("entangled")
public class EntangledWailaPlugin implements IComponentProvider, IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration){
        registration.registerComponentProvider(this, TooltipPosition.BODY, EntangledBlock.class);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config){
        BlockEntity tile = accessor.getBlockEntity();
        if(tile instanceof EntangledBlockEntity){
            if(((EntangledBlockEntity)tile).isBound()){
                BlockState boundBlockState = ((EntangledBlockEntity)tile).getBoundBlockState();
                Component boundBlock = (boundBlockState == null ? TextComponents.string("Block") : TextComponents.blockState(boundBlockState)).color(ChatFormatting.GOLD).get();
                BlockPos boundPos = ((EntangledBlockEntity)tile).getBoundBlockPos();
                Component x = TextComponents.string(Integer.toString(boundPos.getX())).color(ChatFormatting.GOLD).get();
                Component y = TextComponents.string(Integer.toString(boundPos.getY())).color(ChatFormatting.GOLD).get();
                Component z = TextComponents.string(Integer.toString(boundPos.getZ())).color(ChatFormatting.GOLD).get();
                if(((EntangledBlockEntity)tile).getBoundDimensionIdentifier() == accessor.getLevel().dimension())
                    tooltip.add(TextComponents.translation("entangled.waila.bound_same_dimension", boundBlock, x, y, z).get());
                else{
                    Component dimension = TextComponents.dimension(((EntangledBlockEntity)tile).getBoundDimensionIdentifier()).color(ChatFormatting.GOLD).get();
                    tooltip.add(TextComponents.translation("entangled.waila.bound_other_dimension", boundBlock, x, y, z, dimension).get());
                }
                if(!((EntangledBlockEntity)tile).isBoundAndValid())
                    tooltip.add(TextComponents.translation("entangled.waila.invalid_block", boundBlock).color(ChatFormatting.RED).get());
            }else
                tooltip.add(TextComponents.translation("entangled.waila.unbound").get());
        }
    }
}
