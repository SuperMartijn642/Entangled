package com.supermartijn642.entangled.integration;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.entangled.EntangledBlockEntity;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

import java.util.function.Function;

/**
 * Created 7/13/2021 by SuperMartijn642
 */
public class TheOneProbePlugin {

    public static void interModEnqueue(InterModEnqueueEvent e){
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> new ProbeInfoProvider()); // Don't use a lambda, it crashes things
    }

    public static class ProbeInfoProvider implements IProbeInfoProvider, Function<ITheOneProbe,Void> {

        @Override
        public Void apply(ITheOneProbe theOneProbe){
            theOneProbe.registerProvider(new ProbeInfoProvider());
            return null;
        }

        @Override
        public String getID(){
            return "entangled";
        }

        @Override
        public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState state, IProbeHitData probeHitData){
            TileEntity tile = world.getBlockEntity(probeHitData.getPos());
            if(tile instanceof EntangledBlockEntity){
                // The One Probe has no way of sending translation components, thus, if this is a server, just send the messages directly and hope players speak English ¯\(o_o)/¯
                if(CommonUtils.getEnvironmentSide().isClient()){
                    if(((EntangledBlockEntity)tile).isBound()){
                        BlockState boundBlockState = ((EntangledBlockEntity)tile).getBoundBlockState();
                        ITextComponent boundBlock = TextComponents.string(TextStyleClass.WARNING.toString()).append(boundBlockState == null ? TextComponents.string("Block").get() : TextComponents.blockState(boundBlockState).get()).append(TextComponents.string(TextStyleClass.INFO.toString()).get()).get();
                        BlockPos boundPos = ((EntangledBlockEntity)tile).getBoundBlockPos();
                        String x = TextStyleClass.WARNING.toString() + boundPos.getX() + TextStyleClass.INFO;
                        String y = TextStyleClass.WARNING.toString() + boundPos.getY() + TextStyleClass.INFO;
                        String z = TextStyleClass.WARNING.toString() + boundPos.getZ() + TextStyleClass.INFO;
                        if(((EntangledBlockEntity)tile).getBoundDimensionType() == world.getDimension().getType())
                            probeInfo.vertical().text(TextComponents.translation("entangled.waila.bound_same_dimension", boundBlock, x, y, z).format());
                        else{
                            String dimension = TextStyleClass.WARNING + TextComponents.dimension(((EntangledBlockEntity)tile).getBoundDimensionType()).format() + TextStyleClass.INFO;
                            probeInfo.vertical().text(TextComponents.translation("entangled.waila.bound_other_dimension", boundBlock, x, y, z, dimension).format());
                        }
                        if(!((EntangledBlockEntity)tile).isBoundAndValid())
                            probeInfo.text(TextStyleClass.WARNING + TextComponents.translation("entangled.waila.invalid_block", boundBlock).color(TextFormatting.RED).format());
                    }else
                        probeInfo.text(TextComponents.translation("entangled.waila.unbound").format());
                }else{
                    if(((EntangledBlockEntity)tile).isBound()){
                        BlockState boundBlockState = ((EntangledBlockEntity)tile).getBoundBlockState();
                        ITextComponent boundBlock = TextComponents.string(TextStyleClass.WARNING.toString()).append(boundBlockState == null ? TextComponents.string("Block").get() : TextComponents.blockState(boundBlockState).get()).append(TextComponents.string(TextStyleClass.INFO.toString()).get()).get();
                        BlockPos boundPos = ((EntangledBlockEntity)tile).getBoundBlockPos();
                        String x = TextStyleClass.WARNING.toString() + boundPos.getX() + TextStyleClass.INFO;
                        String y = TextStyleClass.WARNING.toString() + boundPos.getY() + TextStyleClass.INFO;
                        String z = TextStyleClass.WARNING.toString() + boundPos.getZ() + TextStyleClass.INFO;
                        if(((EntangledBlockEntity)tile).getBoundDimensionType() == world.getDimension().getType())
                            probeInfo.vertical().text(String.format("Bound to %1$s at (%2$s, %3$s, %4$s)", boundBlock, x, y, z));
                        else{
                            String dimension = TextStyleClass.WARNING + TextComponents.dimension(((EntangledBlockEntity)tile).getBoundDimensionType()).format() + TextStyleClass.INFO;
                            probeInfo.vertical().text(String.format("Bound to %1$s in the %5$s at (%2$s, %3$s, %4$s)", boundBlock, x, y, z, dimension));
                        }
                        if(!((EntangledBlockEntity)tile).isBoundAndValid())
                            probeInfo.text(TextStyleClass.WARNING + String.format("Invalid block '%s'!", boundBlock));
                    }else
                        probeInfo.text("Unbound");
                }
            }
        }
    }
}
