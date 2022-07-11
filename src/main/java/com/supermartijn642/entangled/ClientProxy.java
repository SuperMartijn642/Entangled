package com.supermartijn642.entangled;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Created 3/16/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientProxy {

    @SubscribeEvent
    public static void setup(EntityRenderersEvent.RegisterRenderers e){
        e.registerBlockEntityRenderer(Entangled.tile, context -> new EntangledBlockTileRenderer());
    }

    @SubscribeEvent
    public static void onModelBake(ModelEvent.BakingCompleted e){
        // replace the entangled block item model
        ResourceLocation location = new ModelResourceLocation(new ResourceLocation("entangled", "block"), "inventory");
        BakedModel model = e.getModels().get(location);
        if(model != null)
            e.getModels().put(location, new EntangledBlockBakedItemModel(model));
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Events {

        @SubscribeEvent
        public static void onDrawPlayerEvent(RenderWorldEvent e){
            ItemStack stack = ClientUtils.getPlayer().getItemInHand(InteractionHand.MAIN_HAND);
            Level world = ClientUtils.getWorld();

            if(stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == Entangled.block && stack.hasTag() && stack.getOrCreateTag().contains("tileData")){
                CompoundTag compound = stack.getOrCreateTag().getCompound("tileData");
                if(compound.getBoolean("bound") && compound.getString("dimension").equals(world.dimension().location().toString())){
                    BlockPos pos = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));

                    e.getPoseStack().pushPose();
                    Vec3 camera = RenderUtils.getCameraPosition();
                    e.getPoseStack().translate(-camera.x, -camera.y, -camera.z);
                    e.getPoseStack().translate(pos.getX(), pos.getY(), pos.getZ());

                    RenderUtils.disableDepthTest();
                    RenderUtils.renderShape(e.getPoseStack(), world.getBlockState(pos).getOcclusionShape(world, pos), 86 / 255f, 0 / 255f, 156 / 255f);
                    RenderUtils.renderShapeSides(e.getPoseStack(), world.getBlockState(pos).getOcclusionShape(world, pos), 86 / 255f, 0 / 255f, 156 / 255f, 30 / 255f);
                    RenderUtils.resetState();

                    e.getPoseStack().popPose();
                }
            }else if(stack.getItem() == Entangled.item){
                CompoundTag compound = stack.getOrCreateTag();
                if(compound.getBoolean("bound") && compound.getString("dimension").equals(world.dimension().location().toString())){
                    BlockPos pos = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));

                    e.getPoseStack().pushPose();
                    Vec3 camera = RenderUtils.getCameraPosition();
                    e.getPoseStack().translate(-camera.x, -camera.y, -camera.z);
                    e.getPoseStack().translate(pos.getX(), pos.getY(), pos.getZ());

                    RenderUtils.disableDepthTest();
                    RenderUtils.renderShape(e.getPoseStack(), world.getBlockState(pos).getOcclusionShape(world, pos), 235 / 255f, 210 / 255f, 52 / 255f);
                    RenderUtils.renderShapeSides(e.getPoseStack(), world.getBlockState(pos).getOcclusionShape(world, pos), 235 / 255f, 210 / 255f, 52 / 255f, 30 / 255f);
                    RenderUtils.resetState();

                    e.getPoseStack().popPose();
                }
            }
        }

        @SubscribeEvent
        public static void onBlockHighlight(RenderHighlightEvent.Block e){
            if(e.getTarget().getType() != HitResult.Type.BLOCK || e.getTarget().getBlockPos() == null || !EntangledConfig.renderBlockHighlight.get())
                return;

            Level world = Minecraft.getInstance().level;
            BlockEntity tile = world.getBlockEntity(e.getTarget().getBlockPos());
            if(tile instanceof EntangledBlockTile && ((EntangledBlockTile)tile).isBound() && ((EntangledBlockTile)tile).getBoundDimension() == world.dimension()){
                BlockPos pos = ((EntangledBlockTile)tile).getBoundBlockPos();

                e.getPoseStack().pushPose();
                Vec3 camera = RenderUtils.getCameraPosition();
                e.getPoseStack().translate(-camera.x, -camera.y, -camera.z);
                e.getPoseStack().translate(pos.getX(), pos.getY(), pos.getZ());

                RenderUtils.disableDepthTest();
                RenderUtils.renderShape(e.getPoseStack(), world.getBlockState(pos).getOcclusionShape(world, pos), 86 / 255f, 0 / 255f, 156 / 255f);
                RenderUtils.renderShapeSides(e.getPoseStack(), world.getBlockState(pos).getOcclusionShape(world, pos), 86 / 255f, 0 / 255f, 156 / 255f, 30 / 255f);
                RenderUtils.resetState();

                e.getPoseStack().popPose();
            }
        }
    }

}
