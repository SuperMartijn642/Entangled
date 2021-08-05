package com.supermartijn642.entangled.integration;

import com.supermartijn642.entangled.EntangledBlock;
import mcp.mobius.waila.api.*;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
@WailaPlugin("entangled")
public class EntangledWailaPlugin implements IComponentProvider, IWailaPlugin {

    @Override
    public void register(IRegistrar registrar){
        registrar.registerComponentProvider(this, TooltipPosition.BODY, EntangledBlock.class);
    }

//    @Override TODO uncomment once hwyla updates
//    public void appendBody(List<Component> tooltip, IDataAccessor accessor, IPluginConfig config){
//        BlockEntity tile = accessor.getTileEntity();
//        if(tile instanceof EntangledBlockTile){
//            if(((EntangledBlockTile)tile).isBound()){
//                BlockState boundBlockState = ((EntangledBlockTile)tile).getBoundBlockState();
//                Component boundBlock = (boundBlockState == null ? TextComponents.string("Block") : TextComponents.blockState(boundBlockState)).color(ChatFormatting.GOLD).get();
//                BlockPos boundPos = ((EntangledBlockTile)tile).getBoundBlockPos();
//                Component x = TextComponents.string(Integer.toString(boundPos.getX())).color(ChatFormatting.GOLD).get();
//                Component y = TextComponents.string(Integer.toString(boundPos.getY())).color(ChatFormatting.GOLD).get();
//                Component z = TextComponents.string(Integer.toString(boundPos.getZ())).color(ChatFormatting.GOLD).get();
//                if(((EntangledBlockTile)tile).getBoundDimension() == accessor.getWorld().dimension())
//                    tooltip.add(TextComponents.translation("entangled.waila.bound_same_dimension", boundBlock, x, y, z).color(ChatFormatting.YELLOW).get());
//                else{
//                    Component dimension = TextComponents.dimension(((EntangledBlockTile)tile).getBoundDimension()).color(ChatFormatting.GOLD).get();
//                    tooltip.add(TextComponents.translation("entangled.waila.bound_other_dimension", boundBlock, x, y, z, dimension).color(ChatFormatting.YELLOW).get());
//                }
//            }else
//                tooltip.add(TextComponents.translation("entangled.waila.unbound").color(ChatFormatting.YELLOW).get());
//        }
//    }
}
