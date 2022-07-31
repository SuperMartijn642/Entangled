package com.supermartijn642.entangled;

import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.core.render.CustomRendererBakedModelWrapper;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Created 3/16/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntangledClient {

    public static void register(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get("entangled");

        // Entangled block renderer
        handler.registerCustomBlockEntityRenderer(() -> Entangled.tile, EntangledBlockEntityRenderer::new);
        handler.registerCustomItemRenderer(() -> Entangled.block.asItem(), EntangledBlockItemRenderer::new);
        // Entangled block item model
        handler.registerModelOverwrite("entangled", "block", "inventory", CustomRendererBakedModelWrapper::wrap);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Events {

        @SubscribeEvent
        public static void onDrawPlayerEvent(RenderWorldEvent e){
            ItemStack stack = ClientUtils.getPlayer().getItemInHand(Hand.MAIN_HAND);
            World world = ClientUtils.getWorld();

            if(stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == Entangled.block && stack.hasTag() && stack.getOrCreateTag().contains("tileData")){
                CompoundNBT compound = stack.getOrCreateTag().getCompound("tileData");
                if(compound.getBoolean("bound") && compound.getInt("dimension") == world.getDimension().getType().getId()){
                    BlockPos pos = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));

                    GlStateManager.pushMatrix();
                    Vec3d camera = RenderUtils.getCameraPosition();
                    GlStateManager.translated(-camera.x, -camera.y, -camera.z);
                    GlStateManager.translated(pos.getX(), pos.getY(), pos.getZ());

                    RenderUtils.renderShape(world.getBlockState(pos).getOcclusionShape(world, pos), 86 / 255f, 0 / 255f, 156 / 255f, false);
                    RenderUtils.renderShapeSides(world.getBlockState(pos).getOcclusionShape(world, pos), 86 / 255f, 0 / 255f, 156 / 255f, 30 / 255f, false);

                    GlStateManager.popMatrix();
                }
            }else if(stack.getItem() == Entangled.item){
                CompoundNBT compound = stack.getOrCreateTag();
                if(compound.getBoolean("bound") && compound.getInt("dimension") == world.getDimension().getType().getId()){
                    BlockPos pos = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));

                    GlStateManager.pushMatrix();
                    Vec3d camera = RenderUtils.getCameraPosition();
                    GlStateManager.translated(-camera.x, -camera.y, -camera.z);
                    GlStateManager.translated(pos.getX(), pos.getY(), pos.getZ());

                    RenderUtils.renderShape(world.getBlockState(pos).getOcclusionShape(world, pos), 235 / 255f, 210 / 255f, 52 / 255f, false);
                    RenderUtils.renderShapeSides(world.getBlockState(pos).getOcclusionShape(world, pos), 235 / 255f, 210 / 255f, 52 / 255f, 30 / 255f, false);

                    GlStateManager.popMatrix();
                }
            }
        }

        @SubscribeEvent
        public static void onBlockHighlight(DrawBlockHighlightEvent.HighlightBlock e){
            if(e.getTarget().getType() != RayTraceResult.Type.BLOCK || e.getTarget().getBlockPos() == null || !EntangledConfig.renderBlockHighlight.get())
                return;

            World world = Minecraft.getInstance().level;
            TileEntity tile = world.getBlockEntity(e.getTarget().getBlockPos());
            if(tile instanceof EntangledBlockEntity && ((EntangledBlockEntity)tile).isBound() && ((EntangledBlockEntity)tile).getBoundDimension() == world.getDimension().getType().getId()){
                BlockPos pos = ((EntangledBlockEntity)tile).getBoundBlockPos();

                GlStateManager.pushMatrix();
                Vec3d camera = RenderUtils.getCameraPosition();
                GlStateManager.translated(-camera.x, -camera.y, -camera.z);
                GlStateManager.translated(pos.getX(), pos.getY(), pos.getZ());

                RenderUtils.renderShape(world.getBlockState(pos).getOcclusionShape(world, pos), 86 / 255f, 0 / 255f, 156 / 255f, false);
                RenderUtils.renderShapeSides(world.getBlockState(pos).getOcclusionShape(world, pos), 86 / 255f, 0 / 255f, 156 / 255f, 30 / 255f, false);

                GlStateManager.popMatrix();
            }
        }
    }

}
