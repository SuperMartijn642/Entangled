package com.supermartijn642.entangled.integration;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.entangled.EntangledBlockEntity;
import mcjty.theoneprobe.api.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

import java.util.function.Function;

/**
 * Created 7/13/2021 by SuperMartijn642
 */
public class TheOneProbePlugin {

    public static void interModEnqueue(InterModEnqueueEvent e){
        //noinspection Convert2MethodRef
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> new ProbeInfoProvider()); // Don't use a lambda, it crashes things
    }

    public static class ProbeInfoProvider implements IProbeInfoProvider, Function<ITheOneProbe,Void> {

        @Override
        public Void apply(ITheOneProbe theOneProbe){
            theOneProbe.registerProvider(new ProbeInfoProvider());
            return null;
        }

        @Override
        public ResourceLocation getID(){
            return ResourceLocation.fromNamespaceAndPath("entangled", "entangled_block");
        }

        @Override
        public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, Player player, Level world, BlockState state, IProbeHitData probeHitData){
            BlockEntity tile = world.getBlockEntity(probeHitData.getPos());
            if(tile instanceof EntangledBlockEntity){
                if(((EntangledBlockEntity)tile).isBound()){
                    BlockState boundBlockState = ((EntangledBlockEntity)tile).getBoundBlockState();
                    Component boundBlock = TextComponents.string(TextStyleClass.HIGHLIGHTED.toString()).append(boundBlockState == null ? TextComponents.string("Block").get() : TextComponents.blockState(boundBlockState).get()).append(TextComponents.string(TextStyleClass.INFO.toString()).get()).get();
                    BlockPos boundPos = ((EntangledBlockEntity)tile).getBoundBlockPos();
                    String x = TextStyleClass.HIGHLIGHTED.toString() + boundPos.getX() + TextStyleClass.INFO;
                    String y = TextStyleClass.HIGHLIGHTED.toString() + boundPos.getY() + TextStyleClass.INFO;
                    String z = TextStyleClass.HIGHLIGHTED.toString() + boundPos.getZ() + TextStyleClass.INFO;
                    if(((EntangledBlockEntity)tile).getBoundDimensionIdentifier() == world.dimension())
                        probeInfo.vertical().text(TextComponents.translation("entangled.waila.bound_same_dimension", boundBlock, x, y, z).get());
                    else{
                        String dimension = TextStyleClass.HIGHLIGHTED + TextComponents.dimension(((EntangledBlockEntity)tile).getBoundDimensionIdentifier()).format() + TextStyleClass.INFO;
                        probeInfo.vertical().text(TextComponents.translation("entangled.waila.bound_other_dimension", boundBlock, x, y, z, dimension).get());
                    }
                    if(!((EntangledBlockEntity)tile).isBoundAndValid())
                        probeInfo.text(TextStyleClass.WARNING + TextComponents.translation("entangled.waila.invalid_block", boundBlock).color(ChatFormatting.RED).format());
                }else
                    probeInfo.text(TextComponents.translation("entangled.waila.unbound").get());
            }
        }
    }
}
